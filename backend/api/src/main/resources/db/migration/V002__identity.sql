-- ============================================================================
-- V002 — Identity: users, roles, permissions, tokens, profiles.
-- ADR-003 (authentication), ADR-004 (authorization).
-- ============================================================================

CREATE TABLE users (
    id                    uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    email                 citext NOT NULL,
    username              citext NOT NULL,
    password_hash         text NOT NULL,
    full_name             text NOT NULL,
    status                text NOT NULL DEFAULT 'PENDING'
                          CHECK (status IN ('PENDING','ACTIVE','SUSPENDED','DISABLED')),
    preferred_locale      text NOT NULL DEFAULT 'uz' REFERENCES locales(code),
    phone                 text,
    must_change_password  boolean NOT NULL DEFAULT false,
    failed_login_attempts int NOT NULL DEFAULT 0 CHECK (failed_login_attempts >= 0),
    locked_until          timestamptz,
    last_login_at         timestamptz,
    password_changed_at   timestamptz,
    created_at            timestamptz NOT NULL DEFAULT now(),
    updated_at            timestamptz NOT NULL DEFAULT now(),
    created_by            uuid,
    updated_by            uuid,
    version               bigint NOT NULL DEFAULT 0,
    CONSTRAINT uq_users_email    UNIQUE (email),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT ck_users_email_shape CHECK (email ~ '^[^@\s]+@[^@\s]+\.[^@\s]+$')
);
CREATE TRIGGER trg_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE INDEX idx_users_status ON users(status);
COMMENT ON COLUMN users.password_hash IS 'BCrypt cost 12. Plaintext passwords are never stored or logged.';

CREATE TABLE roles (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code        text NOT NULL UNIQUE CHECK (code ~ '^[A-Z][A-Z0-9_]*$'),
    name        text NOT NULL,
    description text,
    is_system   boolean NOT NULL DEFAULT false,
    created_at  timestamptz NOT NULL DEFAULT now(),
    updated_at  timestamptz NOT NULL DEFAULT now()
);
CREATE TRIGGER trg_roles_updated_at BEFORE UPDATE ON roles
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
COMMENT ON TABLE roles IS 'ADR-004: adding CURATOR/ASSISTANT_TEACHER/SUPER_ADMIN is DATA, not code.';

CREATE TABLE permissions (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code        text NOT NULL UNIQUE CHECK (code ~ '^[A-Z][A-Z0-9_]*$'),
    description text NOT NULL,
    domain      text NOT NULL,
    created_at  timestamptz NOT NULL DEFAULT now(),
    updated_at  timestamptz NOT NULL DEFAULT now()
);
CREATE TRIGGER trg_permissions_updated_at BEFORE UPDATE ON permissions
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE role_permissions (
    role_id       uuid NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id uuid NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);
CREATE INDEX idx_role_permissions_permission ON role_permissions(permission_id);

CREATE TABLE user_roles (
    user_id    uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id    uuid NOT NULL REFERENCES roles(id) ON DELETE RESTRICT,
    granted_at timestamptz NOT NULL DEFAULT now(),
    granted_by uuid REFERENCES users(id) ON DELETE SET NULL,
    PRIMARY KEY (user_id, role_id)
);
CREATE INDEX idx_user_roles_role ON user_roles(role_id);

-- Only a SHA-256 hash of the refresh token is stored. Rotation + reuse detection (ADR-003).
CREATE TABLE refresh_tokens (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash     text NOT NULL UNIQUE,
    family_id      uuid NOT NULL,
    issued_at      timestamptz NOT NULL DEFAULT now(),
    expires_at     timestamptz NOT NULL,
    revoked_at     timestamptz,
    revoked_reason text CHECK (revoked_reason IN
                   ('ROTATED','LOGOUT','LOGOUT_ALL','REUSE_DETECTED','PASSWORD_CHANGED','ADMIN_REVOKED',
                    'ACCOUNT_DISABLED','ROLES_CHANGED','EXPIRED')),
    replaced_by_id uuid REFERENCES refresh_tokens(id) ON DELETE SET NULL,
    user_agent     text,
    ip_address     inet,
    created_at     timestamptz NOT NULL DEFAULT now(),
    updated_at     timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_refresh_expiry CHECK (expires_at > issued_at)
);
CREATE TRIGGER trg_refresh_tokens_updated_at BEFORE UPDATE ON refresh_tokens
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE INDEX idx_refresh_tokens_user   ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_family ON refresh_tokens(family_id);
CREATE INDEX idx_refresh_tokens_active ON refresh_tokens(user_id, expires_at)
    WHERE revoked_at IS NULL;
COMMENT ON COLUMN refresh_tokens.family_id IS
    'Rotation family. Presenting a revoked token revokes the whole family (theft detection).';

CREATE TABLE departments (
    id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code       text NOT NULL UNIQUE,
    name       text NOT NULL,
    name_en    text,
    head_user_id uuid REFERENCES users(id) ON DELETE SET NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE TRIGGER trg_departments_updated_at BEFORE UPDATE ON departments
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
COMMENT ON TABLE departments IS 'AIFU "kafedra". Both supplied syllabi belong to "Aniq fanlar".';

CREATE TABLE academic_programmes (
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code                text NOT NULL UNIQUE,              -- e.g. 60410100, 60610600, 70540101
    name                text NOT NULL,
    knowledge_area_code text,                              -- e.g. 400000, 600000
    education_area_code text,                              -- e.g. 410000, 610000
    degree_level        text NOT NULL DEFAULT 'BACHELOR'
                        CHECK (degree_level IN ('BACHELOR','MASTER','PHD')),
    created_at          timestamptz NOT NULL DEFAULT now(),
    updated_at          timestamptz NOT NULL DEFAULT now()
);
CREATE TRIGGER trg_programmes_updated_at BEFORE UPDATE ON academic_programmes
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE student_profiles (
    user_id         uuid PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    student_number  text NOT NULL UNIQUE,
    programme_id    uuid REFERENCES academic_programmes(id) ON DELETE RESTRICT,
    group_code      text,
    enrollment_year int CHECK (enrollment_year BETWEEN 1990 AND 2200),
    study_form      text NOT NULL DEFAULT 'FULL_TIME'
                    CHECK (study_form IN ('FULL_TIME','PART_TIME','EVENING','DISTANCE')),
    created_at      timestamptz NOT NULL DEFAULT now(),
    updated_at      timestamptz NOT NULL DEFAULT now()
);
CREATE TRIGGER trg_student_profiles_updated_at BEFORE UPDATE ON student_profiles
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE INDEX idx_student_profiles_programme ON student_profiles(programme_id);
CREATE INDEX idx_student_profiles_group     ON student_profiles(group_code);

CREATE TABLE teacher_profiles (
    user_id         uuid PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    employee_number text UNIQUE,
    department_id   uuid REFERENCES departments(id) ON DELETE RESTRICT,
    academic_title  text,      -- "katta o'qituvchi", "dotsent v.b.", "professor"
    academic_degree text,      -- "f-m.f.d.", "t.f.d."
    office_hours    text,      -- verbatim, e.g. "Seshanba, Payshanba 14:00-16:00"
    created_at      timestamptz NOT NULL DEFAULT now(),
    updated_at      timestamptz NOT NULL DEFAULT now()
);
CREATE TRIGGER trg_teacher_profiles_updated_at BEFORE UPDATE ON teacher_profiles
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE INDEX idx_teacher_profiles_department ON teacher_profiles(department_id);
