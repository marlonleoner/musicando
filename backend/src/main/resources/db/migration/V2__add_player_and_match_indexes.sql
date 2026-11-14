-- Apelido único por sala também no banco (protege contra duas entradas simultâneas com o mesmo apelido)
-- e índice para buscar as partidas de uma sala.

create unique index uk_player_room_nickname on tb_player (room_id, nickname);

create index idx_match_room on tb_match (room_id);
