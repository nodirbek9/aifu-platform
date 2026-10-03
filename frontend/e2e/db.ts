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
