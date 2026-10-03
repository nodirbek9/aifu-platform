import { execFileSync } from 'node:child_process'

/**
 * The development database, through the Compose Postgres container. The flows use it for the
 * few things the API — rightly — cannot do: undoing the demo student's own work before a run,
 * and bringing the end of a quiz attempt closer so that "time is up" can be seen in seconds.
 * Set E2E_DB_CONTAINER to another container name, or to "none" to skip.
 */
const CONTAINER = process.env.E2E_DB_CONTAINER ?? 'aifu-platform-postgres-1'

export const databaseAvailable = CONTAINER !== 'none'

/** Runs SQL and returns what it printed: unaligned rows, one per line. */
export function runSql(sql: string): string {
  return execFileSync(
    'docker',
    [
      'exec',
      CONTAINER,
      'sh',
      '-c',
      'psql -q -At -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "$0"',
      sql,
    ],
    { stdio: 'pipe', encoding: 'utf8' },
  ).trim()
}

const enrollmentsOf = (username: string) => `
  select e.id from enrollments e join users u on u.id = e.student_user_id where u.username = '${username}'`

/** The id of a quiz by its code: seeded ids differ from one database to the next. */
export function quizIdByCode(code: string): string {
  const id = runSql(`select id from quizzes where code = '${code}'`)
  if (id === '') throw new Error(`No quiz with the code ${code} in the development database`)
  return id
}

/** Removes the student's attempts at one quiz, so a flow always starts from "no attempts yet". */
export function clearQuizAttempts(username: string, quizCode: string): void {
  runSql(`
    delete from quiz_attempts
    where quiz_id = (select id from quizzes where code = '${quizCode}')
      and enrollment_id in (${enrollmentsOf(username)})`)
}

/** Moves the end of the student's open attempt to `seconds` from now. */
export function endOpenAttemptIn(username: string, quizCode: string, seconds: number): void {
  runSql(`
    update quiz_attempts set expires_at = now() + interval '${seconds} seconds'
    where status = 'IN_PROGRESS'
      and quiz_id = (select id from quizzes where code = '${quizCode}')
      and enrollment_id in (${enrollmentsOf(username)})`)
}

/** What every run starts from: no lesson progress, and no submissions to the task the coding flow uses. */
export function resetDemoStudent(username: string): void {
  runSql(`
    delete from lesson_progress where enrollment_id in (${enrollmentsOf(username)});
    delete from submissions
    where coding_task_id = (select id from coding_tasks where code = 'DAS-S1-A8')
      and enrollment_id in (${enrollmentsOf(username)});`)
}

/**
 * Puts the student's notifications back to how the seed left them — the timetable changes, the
 * grade and the group's announcement unread — clears the phone number a flow may have saved, and
 * drops notifications of announcements that no longer exist.
 */
export function resetNotificationsAndPhone(username: string): void {
  runSql(`
    update notifications set status = 'SENT', read_at = null
    where recipient_user_id = (select id from users where username = '${username}')
      and (type in ('TIMETABLE_CHANGED', 'GRADE_PUBLISHED') or payload ? 'groupId');
    update users set phone = null where username = '${username}';
    -- announcements a teacher flow posted and took back leave their notification behind
    delete from notifications
    where recipient_user_id = (select id from users where username = '${username}')
      and type = 'ANNOUNCEMENT'
      and not exists (select 1 from announcements a where a.id::text = payload ->> 'announcementId');`)
}

/** Removes a class's attendance, lesson, topic and notes: the class as if never opened. */
export function clearClassSession(entryId: string, date: string): void {
  runSql(`
    delete from attendance_records where session_id in
      (select id from class_sessions where entry_id = '${entryId}' and on_date = '${date}');
    delete from class_session_groups where session_id in
      (select id from class_sessions where entry_id = '${entryId}' and on_date = '${date}');
    delete from class_sessions where entry_id = '${entryId}' and on_date = '${date}';`)
}

/**
 * Removes what the authoring flows created in a course: modules, coding tasks and quizzes whose
 * code starts with the E2E marker the flows give their titles.
 */
export function clearAuthoringLeftovers(): void {
  runSql(`
    delete from course_modules where code like 'M-E2E-%';
    delete from coding_tasks where code like 'T-E2E-%';
    delete from quizzes where code like 'Q-E2E-%';`)
}

/**
 * Takes a student of the review flow back to no work and no grades in the course: their
 * submissions to the task, the feedback on them, their grade entries.
 */
export function resetReviewedStudent(username: string, taskCode: string): void {
  runSql(`
    delete from feedback where target_type = 'SUBMISSION' and target_id in (
      select s.id from submissions s where s.enrollment_id in (${enrollmentsOf(username)}));
    delete from submissions
    where coding_task_id = (select id from coding_tasks where code = '${taskCode}')
      and enrollment_id in (${enrollmentsOf(username)});
    -- grades are append-only (ADR-011): only a test reset of the development database may
    -- remove them, with the guard trigger switched off for this one session
    set session_replication_role = replica;
    delete from grade_entries where enrollment_id in (${enrollmentsOf(username)});
    set session_replication_role = origin;
    update enrollments set final_score = null, final_grade_symbol = null, admitted_to_final = null,
                           academic_debt = false, final_grade_at = null
    where id in (${enrollmentsOf(username)});
    delete from notifications
    where recipient_user_id = (select id from users where username = '${username}')
      and type = 'GRADE_PUBLISHED';`)
}

/** Removes what the admin flows created: E2E groups, rooms, holidays and imported students. */
export function clearAdminLeftovers(): void {
  runSql(`
    delete from users where username like 'e2e.%';
    delete from student_groups where code like 'E2E-%';
    delete from rooms where number like 'E2E%';
    delete from holidays where name like 'E2E %';`)
}

/**
 * Undoes the builder flow: its draft timetables go, its groups and room go, and the demo
 * timetable the flow's publishing archived is published again (one published timetable per term).
 */
export function restoreDemoTimetable(): void {
  runSql(`
    delete from timetables where name like 'E2E%';
    delete from student_groups where code like 'E2E-T%';
    delete from rooms where number like 'E2E-T%';
    update timetables set status = 'PUBLISHED'
    where name = '2026/2027, 1-semestr'
      and not exists (select 1 from timetables other where other.status = 'PUBLISHED' and other.term_id = timetables.term_id);`)
}
