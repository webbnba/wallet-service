--liquibase formatted sql

--changeset ValeryBezborodov:create-wallet-table
--comment create table wallet.wallet
create table wallet.wallet
(
    id                UUID             PRIMARY KEY,
    balance           NUMERIC(19,2)    NOT NULL CHECK ( balance >= 0 )
);
--rollback drop table wallet.wallet;