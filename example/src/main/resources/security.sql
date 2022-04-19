drop table favourite_book_mapping if exists;

drop table app_user_role_mapping if exists;
drop table app_user if exists;
drop table role if exists;

create table app_user(
    id uuid not null,
    email varchar(50) not null,
    password varchar(100) not null
);
alter table app_user
    add constraint pk_app_user
        primary key(id);
alter table app_user
    add constraint uq_app_user_email
        unique(email);

create table role(
    id uuid not null,
    name varchar(20) not null
);
alter table role
    add constraint pk_role
        primary key(id);
alter table role
    add constraint uq_role_name
        unique(name);

create table app_user_role_mapping(
    app_user_id uuid not null,
    role_id uuid not null
);
alter table app_user_role_mapping
    add constraint pk_app_user_role_mapping
        primary key(app_user_id, role_id);
alter table app_user_role_mapping
    add constraint fk_app_user_role_mapping__app_user
        foreign key(app_user_id)
            references app_user(id);
alter table app_user_role_mapping
    add constraint fk_app_user_role_mapping__role
        foreign key(role_id)
            references role(id);

insert into app_user(id, email, password) values(
    '2ae72a07-f5ac-4fca-847f-7a640dbfc04f',
    'user1@gmail.com',

    -- original password is '123456'
    '{bcrypt}$2a$10$G6iuRSRGOReyaM9mPVNuZu/TVYSy3v/EWqF8sCVvjXfpBnx9Ai40u'
);

insert into app_user(id, email, password) values(
    '74357d4c-7bba-4d11-b079-3f6bffd590a0',
    'user2@gmail.com',

    -- original password is '123456'
    '{bcrypt}$2a$10$.iYY2OJWGpiYCDX.80kNX.CTMEFk5E83ZxNBTw.QR2klxZVY84x3K'
);

insert into app_user(id, email, password) values(
    'dbf7ddf2-b0a0-4f8b-a415-708d9132c44e',
    'admin@gmail.com',

    -- original password is '123456'
    '{bcrypt}$2a$10$fweFV/2ba/LxqnltQQrbbuFnksKSgG0kUwqIzWOJ3HSXSmY4ZFXq.'
);

insert into role(id, name) values(
    'fa219170-330b-4d74-8f06-2edc75cc4356',
    'ADMIN'
);

insert into app_user_role_mapping(app_user_id, role_id) values
    ('dbf7ddf2-b0a0-4f8b-a415-708d9132c44e', 'fa219170-330b-4d74-8f06-2edc75cc4356');

