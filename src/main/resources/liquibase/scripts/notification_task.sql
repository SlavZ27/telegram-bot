--liquibase formatted sql

--changeset zaytcev:1
--precondition-sql-check expectedResult:0 SELECT count(*) FROM pg_tables WHERE tablename='notification_task'
create table notification_task
(
    id           bigserial primary key,
    id_chat      bigint    not null,
    text_message text      not null,
    date_time    timestamp not null
)
