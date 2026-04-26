-- Refer: https://github.com/spring-projects/spring-integration/blob/v7.0.3/spring-integration-jdbc/src/main/resources/org/springframework/integration/jdbc/schema-postgresql.sql#L29-L36 -- noqa: LT05
CREATE TABLE IF NOT EXISTS int_lock (
    lock_key CHAR(36) NOT NULL,
    region VARCHAR(100) NOT NULL,
    client_id CHAR(36),
    created_date TIMESTAMP NOT NULL,
    expired_after TIMESTAMP NOT NULL,
    CONSTRAINT int_lock_pk PRIMARY KEY (lock_key, region)
);

-- Refer: https://github.com/spring-projects/spring-security/blob/7.0.3/oauth2/oauth2-authorization-server/src/main/resources/org/springframework/security/oauth2/server/authorization/client/oauth2-registered-client-schema.sql -- noqa: LT05
CREATE TABLE IF NOT EXISTS oauth2_registered_client (
    id VARCHAR(100) PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL CONSTRAINT uk_client_id UNIQUE,
    client_id_issued_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    client_secret VARCHAR(200),
    client_secret_expires_at TIMESTAMPTZ,
    client_name VARCHAR(200) NOT NULL,
    client_authentication_methods VARCHAR(1000) NOT NULL,
    authorization_grant_types VARCHAR(1000) NOT NULL,
    redirect_uris VARCHAR(1000),
    post_logout_redirect_uris VARCHAR(1000),
    scopes VARCHAR(1000) NOT NULL,
    client_settings VARCHAR(2000) NOT NULL,
    token_settings VARCHAR(2000) NOT NULL
);

-- users
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT UUIDV7(),
    username VARCHAR(200) NOT NULL,
    password VARCHAR(200)
);
