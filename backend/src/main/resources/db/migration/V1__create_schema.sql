-- Schema inicial, igual ao que o Hibernate criava com ddl-auto=update.
-- Bancos que já existiam antes do Flyway são marcados como versão 1 (baseline) e não executam este script.

create table tb_room (
    id varchar(255) not null,
    code varchar(6) not null unique,
    secret varchar(255) not null,
    status varchar(255),
    connected boolean not null,
    host_session_id varchar(255),
    current_match_id varchar(255) unique,
    created_at timestamp,
    updated_at timestamp,
    primary key (id)
);

create table tb_player (
    id varchar(255) not null,
    room_id varchar(255) not null,
    session_id varchar(255),
    secret varchar(255) not null,
    nickname varchar(255),
    score integer not null,
    rounds_won integer not null,
    owner boolean not null,
    connected boolean not null,
    created_at timestamp,
    updated_at timestamp,
    primary key (id)
);

create table tb_match (
    id varchar(255) not null,
    room_id varchar(255) not null,
    status varchar(255),
    playlist_id varchar(255),
    playlist_name varchar(255),
    playlist_cover varchar(255),
    playlist_track_count integer,
    rounds integer,
    round_duration integer,
    end_time timestamp,
    created_at timestamp,
    updated_at timestamp,
    primary key (id)
);
