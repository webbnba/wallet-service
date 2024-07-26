--liquibase formatted sql

--changeset ValeryBezborodov:create-wallet-schema
--comment create new schema
create schema wallet;
--rollback drop schema wallet;