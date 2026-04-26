CREATE DATABASE workastra_db;

CREATE USER workastra_user WITH ENCRYPTED PASSWORD 'workastra_password' ;

GRANT ALL PRIVILEGES ON DATABASE workastra_db TO workastra_user ;
