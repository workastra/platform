CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Function: handle_timestamps()
--
-- Description:
--   Automatically manages 'created_at' and 'updated_at' columns.
--
-- Logic:
--   - INSERT: Sets both 'created_at' and 'updated_at' to current time.
--   - UPDATE: Refreshes only 'updated_at' to current time.
--
-- Performance Note:
--   Uses clock_timestamp() to capture the actual execution time rather than
--   the transaction start time (now()), ensuring accuracy in long-running tasks.
CREATE OR REPLACE FUNCTION handle_timestamps()
RETURNS TRIGGER AS $$
DECLARE
    _executed_at TIMESTAMPTZ := clock_timestamp();
BEGIN
    IF (TG_OP = 'INSERT') THEN
        NEW.created_at = _executed_at;
        NEW.updated_at = _executed_at;
    ELSEIF (TG_OP = 'UPDATE') THEN
        NEW.updated_at = _executed_at;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Refer: https://github.com/spring-projects/spring-integration/blob/v7.0.3/spring-integration-jdbc/src/main/resources/org/springframework/integration/jdbc/schema-postgresql.sql#L29-L36
--
-- Due to this table schema is the copied from Spring Integration, we will ignore the squawk rules for this table.
-- Any update to this table should be carefully reviewed to ensure it does not violate the Spring Integration's requirements.
CREATE TABLE IF NOT EXISTS int_lock (
    -- squawk-ignore ban-char-field
    lock_key CHAR(36) NOT NULL,
    -- squawk-ignore prefer-text-field
    region VARCHAR(100) NOT NULL,
    -- squawk-ignore ban-char-field
    client_id CHAR(36),
    -- squawk-ignore prefer-timestamp-tz
    created_date TIMESTAMP NOT NULL,
    -- squawk-ignore prefer-timestamp-tz
    expired_after TIMESTAMP NOT NULL,

    CONSTRAINT int_lock_pk PRIMARY KEY (lock_key, region)
);

-- Refer: https://github.com/spring-projects/spring-security/blob/7.0.3/oauth2/oauth2-authorization-server/src/main/resources/org/springframework/security/oauth2/server/authorization/client/oauth2-registered-client-schema.sql
--
-- Due to this table schema is the copied from Spring Security, we will ignore the squawk rules for this table.
-- Any update to this table should be carefully reviewed to ensure it does not violate the Spring Security's requirements.
CREATE TABLE IF NOT EXISTS oauth2_registered_client (
    -- squawk-ignore prefer-text-field
    id VARCHAR(100) PRIMARY KEY,
    -- squawk-ignore prefer-text-field
    client_id VARCHAR(100) NOT NULL CONSTRAINT uk_client_id UNIQUE,
    client_id_issued_at TIMESTAMPTZ NOT NULL DEFAULT current_timestamp,
    -- squawk-ignore prefer-text-field
    client_secret VARCHAR(200),
    client_secret_expires_at TIMESTAMPTZ,
    -- squawk-ignore prefer-text-field
    client_name VARCHAR(200) NOT NULL,
    -- squawk-ignore prefer-text-field
    client_authentication_methods VARCHAR(1000) NOT NULL,
    -- squawk-ignore prefer-text-field
    authorization_grant_types VARCHAR(1000) NOT NULL,
    -- squawk-ignore prefer-text-field
    redirect_uris VARCHAR(1000),
    -- squawk-ignore prefer-text-field
    post_logout_redirect_uris VARCHAR(1000),
    -- squawk-ignore prefer-text-field
    scopes VARCHAR(1000) NOT NULL,
    -- squawk-ignore prefer-text-field
    client_settings VARCHAR(2000) NOT NULL,
    -- squawk-ignore prefer-text-field
    token_settings VARCHAR(2000) NOT NULL
);

-- users
CREATE TABLE IF NOT EXISTS users (
    id UUID CONSTRAINT pk_user_id PRIMARY KEY DEFAULT uuidv7(),
    username TEXT CHECK (char_length(username) <= 255) NOT NULL CONSTRAINT uk_username UNIQUE,
    password TEXT CHECK (char_length(password) <= 200),
    family_name TEXT CHECK (char_length(family_name) <= 64),
    middle_name TEXT CHECK (char_length(middle_name) <= 64),
    given_name TEXT CHECK (char_length(given_name) <= 64),
    gender TEXT CHECK (gender IN ('male', 'female', 'other')) NOT NULL DEFAULT 'other',
    email TEXT CHECK (char_length(email) <= 255) NOT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    email_verified_at TIMESTAMPTZ,
    locale TEXT CHECK (char_length(locale) <= 35) NOT NULL DEFAULT 'en-US',
    timezone_id TEXT CHECK (char_length(timezone_id) <= 64) NOT NULL DEFAULT 'Etc/UTC',
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    created_by UUID NOT NULL CONSTRAINT fk_created_by REFERENCES users (id) ON DELETE RESTRICT,
    updated_at TIMESTAMPTZ NOT NULL,
    updated_by UUID NOT NULL CONSTRAINT fk_updated_by REFERENCES users (id) ON DELETE RESTRICT,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID CONSTRAINT fk_deleted_by REFERENCES users (id) ON DELETE RESTRICT
);

CREATE TRIGGER trg_users_handle_timestamps_insert
BEFORE INSERT
ON users
FOR EACH ROW
EXECUTE FUNCTION handle_timestamps();

CREATE TRIGGER trg_users_handle_timestamps_update
BEFORE UPDATE
ON users
FOR EACH ROW
WHEN (old IS DISTINCT FROM new)
EXECUTE FUNCTION handle_timestamps();

CREATE TABLE IF NOT EXISTS authorities (
    id UUID CONSTRAINT pk_authority_id PRIMARY KEY DEFAULT uuidv7(),
    name TEXT CHECK (char_length(name) <= 100) NOT NULL CONSTRAINT uk_name UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT current_timestamp,
    created_by UUID NOT NULL CONSTRAINT fk_created_by REFERENCES users (id) ON DELETE RESTRICT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT current_timestamp,
    updated_by UUID NOT NULL CONSTRAINT fk_updated_by REFERENCES users (id) ON DELETE RESTRICT,
    deleted_at TIMESTAMPTZ,
    deleted_by UUID CONSTRAINT fk_deleted_by REFERENCES users (id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS user_authorities (
    user_id UUID NOT NULL CONSTRAINT fk_user_id REFERENCES users (id) ON DELETE RESTRICT,
    authority_id UUID NOT NULL CONSTRAINT fk_authority_id REFERENCES authorities (id) ON DELETE RESTRICT,

    CONSTRAINT pk_user_authorities PRIMARY KEY (user_id, authority_id)
);

INSERT INTO users (id, username, password, email, created_by, updated_by)
VALUES ('00000000-0000-7000-8000-000000000000', 'system', NULL, 'system@internal.com', '00000000-0000-7000-8000-000000000000', '00000000-0000-7000-8000-000000000000');
