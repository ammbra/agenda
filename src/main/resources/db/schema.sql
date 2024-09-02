DROP TABLE IF EXISTS image CASCADE;
DROP TABLE IF EXISTS todo CASCADE;
DROP TABLE IF EXISTS url CASCADE;

create sequence todo_seq start with 4 increment by 50;

create table image (id bigint not null,
                    image blob,
                    primary key (id)
                   );

create table todo (created_on date,
                   deadline date,
                   id bigint not null default nextval('t_id_seq'),
                   description varchar(255),
                   title varchar(255),
                   primary key (id)
                  );

create table url (id bigint not null,
                  url varchar(255),
                  primary key (id)
);

alter table if exists image add constraint FK7btcggkk97lk0hvib3tvum52i foreign key (id) references todo;
alter table if exists url add constraint FKayp4tq89p6jai2vdhxkxn2dyf foreign key (id) references todo;