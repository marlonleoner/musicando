-- Personagem (avatar) escolhido pelo jogador. Único por sala: dois jogadores da mesma sala não usam o mesmo
-- personagem. Jogadores sem personagem (null) não entram na restrição.

alter table tb_player add column avatar varchar(255);

create unique index uk_player_room_avatar on tb_player (room_id, avatar);
