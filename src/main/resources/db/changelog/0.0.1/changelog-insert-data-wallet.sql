--liquibase formatted sql

--changeset ValeryBezborodov:fill-data-wallet-table
--comment fill table wallet.wallet
INSERT INTO wallet.wallet (id, balance)
VALUES ('ce3b39d8-1bae-4ed3-b4db-2a74658f0d85', 1000.00),
       ('afafeae5-b2e5-4db8-ab7d-4b8110fdcd31', 500.00),
       ('16714f72-87bf-495f-ba91-f0967727c06d', 750.00);
--rollback truncate table wallet.wallet;
--rollback drop table wallet.wallet;