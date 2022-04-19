drop table favourite_book_mapping if exists;

drop table book_author_mapping if exists;
drop table book if exists;
drop table author if exists;
drop table book_store if exists;

create table book_store(
    id uuid not null,
    name varchar(50) not null,
    website varchar(100)
);
alter table book_store
    add constraint pk_book_store
        primary key(id)
;
alter table book_store
    add constraint uq_book_store
        unique(name)
;

create table book(
    id uuid not null,
    name varchar(50) not null,
    price numeric(10, 2) not null,
    store_id uuid
);
alter table book
    add constraint pk_book
        primary key(id)
;
alter table book
    add constraint uq_book
        unique(name)
;
alter table book
    add constraint fk_book__book_store
        foreign key(store_id)
            references book_store(id)
;

create table author(
    id uuid not null,
    first_name varchar(25) not null,
    last_name varchar(25) not null,
    gender char(1) not null
);
alter table author
    add constraint pk_author
        primary key(id)
;
alter table author
    add constraint uq_author
        unique(first_name, last_name)
;
alter table author
    add constraint ck_author_gender
        check(gender = 'M' or gender = 'F');

create table book_author_mapping(
    book_id uuid not null,
    author_id uuid not null
);
alter table book_author_mapping
    add constraint pk_book_author_mapping
        primary key(book_id, author_id)
;
alter table book_author_mapping
    add constraint fk_book_author_mapping__book
        foreign key(book_id)
            references book(id)
                on delete cascade
;
alter table book_author_mapping
    add constraint fk_book_author_mapping__author
        foreign key(author_id)
            references author(id)
                on delete cascade
;

create table favourite_book_mapping(
    app_user_id uuid not null,
    book_id uuid not null
);
alter table favourite_book_mapping
    add constraint pk_favourite_book_mapping
        primary key(app_user_id, book_id);
alter table favourite_book_mapping
    add constraint fk_favourite_book_mapping__app_user
        foreign key(app_user_id)
            references app_user(id)
                on delete cascade;
alter table favourite_book_mapping
    add constraint fk_favourite_book_mapping__book
        foreign key(book_id)
            references book(id)
                on delete cascade;

insert into book_store(id, name, website) values
    ('d38c10da-6be8-4924-b9b9-5e81899612a0', 'O''REILLY', 'https://www.oreilly.com/'),
    ('2fa3955e-3e83-49b9-902e-0465c109c779', 'MANNING', 'https://www.manning.com/')
;

insert into book(id, name, price, store_id) values
    ('e110c564-23cc-4811-9e81-d587a13db634', 'Learning GraphQL', 50, 'd38c10da-6be8-4924-b9b9-5e81899612a0'),
    ('8f30bc8a-49f9-481d-beca-5fe2d147c831', 'Effective TypeScript', 73, 'd38c10da-6be8-4924-b9b9-5e81899612a0'),
    ('914c8595-35cb-4f67-bbc7-8029e9e6245a', 'Programming TypeScript', 47.5, 'd38c10da-6be8-4924-b9b9-5e81899612a0'),
    ('a62f7aa3-9490-4612-98b5-98aae0e77120', 'GraphQL in Action', 80, '2fa3955e-3e83-49b9-902e-0465c109c779')
;

insert into author(id, first_name, last_name, gender) values
    ('fd6bb6cf-336d-416c-8005-1ae11a6694b5', 'Eve', 'Procello', 'M'),
    ('1e93da94-af84-44f4-82d1-d8a9fd52ea94', 'Alex', 'Banks', 'M'),
    ('c14665c8-c689-4ac7-b8cc-6f065b8d835d', 'Dan', 'Vanderkam', 'M'),
    ('718795ad-77c1-4fcf-994a-fec6a5a11f0f', 'Boris', 'Cherny', 'M'),
    ('eb4963fd-5223-43e8-b06b-81e6172ee7ae', 'Samer', 'Buna', 'M')
;

insert into book_author_mapping(book_id, author_id) values
    ('e110c564-23cc-4811-9e81-d587a13db634', 'fd6bb6cf-336d-416c-8005-1ae11a6694b5'),
    ('e110c564-23cc-4811-9e81-d587a13db634', '1e93da94-af84-44f4-82d1-d8a9fd52ea94'),
    ('8f30bc8a-49f9-481d-beca-5fe2d147c831', 'c14665c8-c689-4ac7-b8cc-6f065b8d835d'),
    ('914c8595-35cb-4f67-bbc7-8029e9e6245a', '718795ad-77c1-4fcf-994a-fec6a5a11f0f'),
    ('a62f7aa3-9490-4612-98b5-98aae0e77120', 'eb4963fd-5223-43e8-b06b-81e6172ee7ae')
;
