--liquibase formatted sql

--changeset zaytcev:1
--precondition-sql-check expectedResult:0 SELECT count(*) FROM pg_tables WHERE tablename='notification_task'
create table notification_task
(
    id              bigint primary key,
    id_chat         bigint    not null,
    text_message    text      not null,
    date_time       timestamp not null,
    local_date_time timestamp not null
);

--changeset zaytcev:2
--precondition-sql-check expectedResult:1 SELECT count(*) FROM pg_tables WHERE tablename='notification_task'
--onFail=MARK_RAN
ALTER table notification_task
    add column is_done boolean default false;

--changeset zaytcev:3
--precondition-sql-check expectedResult:1 SELECT count(*) FROM pg_tables WHERE tablename='notification_task'
--onFail=MARK_RAN
ALTER table notification_task
    add column sender text;

--changeset zaytcev:4
--precondition-sql-check expectedResult:0 SELECT count(*) FROM pg_tables WHERE tablename='chat'
--onFail=MARK_RAN
create table chat
(
    id        bigint primary key,
    time_zone int
);
