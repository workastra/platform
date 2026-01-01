CREATE DATABASE keycloak_db;

CREATE USER keycloak_user WITH ENCRYPTED PASSWORD 'keycloak_password';

GRANT ALL PRIVILEGES ON DATABASE keycloak_db TO keycloak_user;

\c keycloak_db

GRANT USAGE ON SCHEMA public TO keycloak_user;
GRANT CREATE ON SCHEMA public TO keycloak_user;