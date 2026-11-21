# Backend do Musicando: como funciona

Guia para dar manutenção no backend sem precisar ler o código inteiro. Explica o que cada pacote faz, onde os dados
ficam, o protocolo com a TV e os celulares, e o caminho de cada fluxo do jogo até a classe que o implementa.

Os caminhos de classe abaixo são relativos a `src/main/java/com/marlonleoner/musicando/`.

## 1. Visão geral

- Java 25 e Spring Boot 4.1, com Jackson 3 (`tools.jackson`), Lombok e MapStruct.
- Dois canais com os clientes:
  - **REST** (`/room`, `/music`): só para criar sala, entrar na sala e listar playlists e opções.
  - **STOMP sobre WebSocket** (`/ws`): todo o resto, ou seja, comandos dos clientes e eventos do servidor.
- **O jogo roda no servidor.** Timer, sorteio das rodadas, pontuação e placar são decididos aqui; a TV e os celulares
  só exibem o que recebem e mandam comandos.
- Armazenamento:
  - **SQLite**: salas, jogadores e partidas (dados duráveis).
  - **Redis**: o estado da partida em andamento e a música do lobby (dados temporários, com TTL de 6 horas).
  - **Memória**: timers, locks por sala, rate limit e sessões WebSocket abertas.
- Provedor de músicas: **API pública da Deezer** (playlists e prévias de 30 segundos).

```
TV (host-app) ──┐                     ┌──> Deezer (playlists, prévias)
                ├── REST + WebSocket ─┤
Celulares ──────┘     (backend)       ├──> SQLite (salas, jogadores, partidas)
                                      └──> Redis (partida em andamento, música do lobby)
```

> **Premissa importante:** o backend foi feito para rodar com **uma única instância**. Timers, locks, rate limit e
> sessões WebSocket ficam em memória. Duas réplicas atrás de um balanceador quebrariam o jogo.

## 2. Mapa dos pacotes

| Pacote | O que tem |
|---|---|
| `controller/` | Entradas: `RoomController` e `MusicController` (REST) e `SocketController` (comandos STOMP e foto inicial da sala). |
| `aggregation/` | `RoomAggregation`: orquestra criar sala e entrar na sala (chamado pelo `RoomController`). |
| `handler/socket/command/` | Um handler por comando do socket (`StartGameCommandHandler`, `SubmitAnswerCommandHandler`...). |
| `handler/socket/` | Infra do socket: autenticação dos frames, ciclo de vida das sessões, registro de sessões, frame de erro. |
| `handler/` | `ExceptionHandlers` (erros da API REST) e `RateLimitInterceptor` (limite por IP na API). |
| `service/game/` | O jogo: `GameEngine` (ciclo das rodadas), `GameTimer`, `GameLocks`, `GameStateService` (Redis), `RoundGenerator`, `LobbyService`, `LobbyMusicService`, `GameRecoveryListener`. |
| `service/room/`, `service/player/`, `service/match/` | Regras e persistência de cada entidade. `RoomCleanupService` fecha e apaga salas antigas. |
| `service/socket/` | Envio de eventos (`SocketService`), foto da sala (`RoomStateNotifier`), presença (`PresenceService`), autenticação (`SocketAuthenticationService`) e roteamento de comandos (`SocketCommandDispatcher`). |
| `service/music/` + `integration/` + `client/deezer/` | Músicas: `MusicService` usa a interface `MusicProvider`, implementada por `DeezerIntegrator`, que chama a Deezer pelo `DeezerClient` (HTTP interface do Spring). |
| `service/cache/` | `RedisService`: `set`/`get`/`invalidate` genéricos no Redis. |
| `service/ratelimit/` | `RateLimiter`: janela fixa em memória. |
| `domain/` | Entidades JPA (`Room`, `Player`, `Match`), enums, exceções e os registros do jogo em `domain/music/` (`GameState`, `GameRound`, `MusicTrack`...). |
| `dto/` | Contratos com os clientes: `request/`, `response/` (inclui os `*Broadcast` dos eventos) e `socket/` (envelopes). |
| `mapper/` | MapStruct: entidade para DTO. |
| `validator/` | Validações estáticas: apelido (`JoinRoomValidator`) e opções da partida (`MatchSettingsValidator`). |
| `util/` | `SocketDestinations` (todos os destinos STOMP), `ScoreCalculator`, `TitleNormalizer`, `UUID7`, `LogSanitizer`. |
| `config/` | Beans e `config/properties/` (propriedades `musicando.*` tipadas). |
| `repository/` | Spring Data JPA. |

## 3. Onde ficam os dados

### SQLite (`src/main/resources/db/migration`)

O schema é criado pelo **Flyway**. O Hibernate só valida (`ddl-auto=validate`), então **toda mudança de tabela precisa
de uma migration nova** (`V3__...sql`); senão o backend não sobe.

| Tabela | Conteúdo principal |
|---|---|
| `tb_room` | `code` (6 caracteres, único), `secret` da TV, `status`, `connected` (TV conectada), `host_session_id`, `current_match_id`. |
| `tb_player` | `room_id`, `nickname` (único por sala), `avatar` (personagem, único por sala), `secret`, `owner` (é o VIP), `connected`, `session_id`, `score` e `rounds_won` da última partida. |
| `tb_match` | Configuração da partida (playlist, `rounds`, `round_duration`), `status` e `end_time`. Uma sala tem várias partidas ao longo do tempo; a atual é `tb_room.current_match_id`. |

### Redis

| Chave | Conteúdo | Quem usa |
|---|---|---|
| `@MUSICANDO:GAMES:{code}` | `GameState` em JSON: rodadas sorteadas, etapa atual, horários da etapa, respostas da rodada, pontos e acertos acumulados, prévia da rodada, tempo restante quando pausada. | `GameStateService` / `GameEngine` |
| `@MUSICANDO:LOBBY_MUSIC:{code}` | `LobbyMusic` em JSON: playlist e IDs das músicas que tocam no lobby. | `LobbyMusicService` |

O banco lógico do Redis é escolhido por `REDIS_DATABASE` (no homelab, o 2). Para inspecionar:
`redis-cli -n 2 --scan --pattern '@MUSICANDO:*'` e `redis-cli -n 2 get '@MUSICANDO:GAMES:ABC123'`.

### Memória

- `GameTimer`: a próxima transição agendada de cada sala.
- `GameLocks`: um lock por sala.
- `RateLimiter`: as janelas de contagem.
- `SocketSessionRegistry`: as sessões WebSocket abertas.

Tudo isso se perde num reinício; a seção 7.9 explica como o jogo se recupera.

## 4. Estados

### Sala (`RoomStatus`)

```
WAITING ──START_GAME──> IN_PROGRESS ──última rodada──> FINISHED ──PLAY_AGAIN──> WAITING
                         │      ▲
              TV caiu    ▼      │  TV voltou
                         PAUSED

Qualquer estado ──CLOSE_ROOM ou limpeza──> CLOSED
```

A partida (`MatchStatus`) acompanha a sala: `WAITING`, `IN_PROGRESS`, `PAUSED`, `FINISHED`.

### Rodada (`GamePhase`, dentro do `GameState` no Redis)

```
COUNTDOWN (3 s) ──> PLAYING (duração da rodada) ──> REVEAL (5 s) ──> COUNTDOWN da próxima ... ──> FINISHED
                     │
                     └─ todos os conectados responderam: vai direto para REVEAL
```

Cada etapa guarda `phaseStartedAt` e `phaseEndsAt` (relógio do servidor). Pausada, guarda também
`pausedRemainingMs`, e é esse campo que define se a partida está pausada (`GameState.paused()`).

## 5. API REST

| Método e caminho | Faz o quê | Resposta |
|---|---|---|
| `POST /room` | Cria a sala e a primeira partida (TV). | `{ id, code, secret }`: credenciais da TV. |
| `POST /room/join` | Jogador entra: `{ code, nickname }`. | `{ playerId, secret, roomCode }`: credenciais do jogador. |
| `GET /room` | Lista salas (código, status, TV conectada). | `RoomResponse[]` |
| `GET /music/playlists/featured` | Playlists em destaque da Deezer para o VIP. | `PlaylistResponse[]` |
| `GET /music/options` | Opções de rodadas e duração, e os padrões. | `MatchOptionsResponse` |

Erros vêm como `{ status, code, message }` (`ErrorResponse`), em que `code` é um `ErrorCode`. Cada exceção de
`domain/exception/` já carrega o status HTTP e o `ErrorCode`, e o `ExceptionHandlers` só repassa.

## 6. WebSocket (STOMP)

Todos os destinos estão em `util/SocketDestinations.java`.

### Conexão e autenticação

1. O cliente abre `/ws` e manda o `CONNECT` com os headers `role` (`host` ou `player`), `code`, `id` e `secret`.
2. O `SocketAuthenticationInterceptor` valida cada frame:
   - `CONNECT`: confere as credenciais no banco (`SocketAuthenticationService.authenticate`) e grava um
     `SocketPrincipal` (papel, id do participante, código da sala) na sessão.
   - `SUBSCRIBE`: só aceita o tópico da própria sala, o tópico do host (só para a TV), as filas privadas e a foto da sala.
   - `SEND`: só aceita `/musicando/command`.
3. Uma falha vira um frame STOMP `ERROR` com o código no header `message` (por exemplo `INVALID_CREDENTIALS`), e a
   conexão é fechada (`SocketErrorHandler`).
4. Conectado, o `SocketSessionHandler` chama o `PresenceService` (seção 7.3).

### Destinos

| Destino | Direção | Quem recebe | Para quê |
|---|---|---|---|
| `/musicando/command` | cliente → servidor | | Todos os comandos (`{ type, payload }`). |
| `/musicando/room` | inscrição | quem se inscreveu | Foto inicial da sala (`ROOM_STATE` com motivo `SNAPSHOT`), respondida na hora pelo `SocketController`. |
| `/topic/room/{code}` | servidor → todos | TV e jogadores | `ROOM_STATE` e eventos da rodada. |
| `/topic/room/{code}/host` | servidor → TV | só a TV | `ROUND_AUDIO` e `LOBBY_AUDIO` (as prévias; os jogadores nunca recebem a música). |
| `/user/queue/events` | servidor → um participante | um participante | Reenvio da etapa a quem reconectou, `KICKED`, foto pedida com `GET_ROOM_STATE`. |
| `/user/queue/errors` | servidor → um participante | um participante | `ERROR` de um comando rejeitado. |

### Comandos (`domain/socket/SocketCommandType`)

O `SocketCommandDispatcher` aplica o rate limit por participante, confere se o papel pode mandar o comando e chama o
handler do tipo.

| Comando | Quem pode | Payload | O que faz |
|---|---|---|---|
| `GET_ROOM_STATE` | TV e jogador | | Reenvia a foto da sala só para quem pediu. |
| `LEAVE_ROOM` | jogador | | Remove o jogador e repassa o VIP se for preciso. |
| `KICK_PLAYER` | TV | `{ playerId }` | Manda `KICKED` ao jogador, remove-o e repassa o VIP se for preciso. |
| `CLOSE_ROOM` | TV | | Para a partida e encerra a sala (`CLOSED`). |
| `UPDATE_SETTINGS` | VIP | `{ playlistId?, rounds?, roundDuration? }` | Só no lobby. Com playlist nova, sorteia a música do lobby. |
| `UPDATE_AVATAR` | jogador | `{ avatar }` | Só no lobby. Troca o personagem, desde que nenhum outro jogador da sala esteja com ele. |
| `START_GAME` | VIP | | Valida, sorteia as rodadas e inicia o `GameEngine`. |
| `SUBMIT_ANSWER` | jogador | `{ round, option }` | Registra a resposta da rodada (`option` é o índice de 0 a 3). |
| `PLAY_AGAIN` | VIP | | Depois do fim, volta todos para o lobby com a mesma configuração. |

"VIP" é um jogador com `owner = true`. Handlers de VIP chamam `playerService.getOwnerOrException` logo no começo.

### Eventos (`domain/socket/SocketEventType`)

Todos vêm no envelope `SocketEvent { type, reason, payload, timestamp }`.

| Evento | Payload (`dto/response/`) | Quando |
|---|---|---|
| `ROOM_STATE` | `RoomStateBroadcast`: código, status, TV conectada, jogadores, configuração | Qualquer mudança na sala. É sempre a **foto completa**; `reason` (`RoomStateReason`) serve só para avisos na tela. |
| `ROUND_COUNTDOWN` | `RoundCountdownBroadcast` | Início da contagem de uma rodada. |
| `ROUND_STARTED` | `RoundStartedBroadcast`: as 4 opções, o tempo restante, quem já respondeu | A música começou. |
| `ROUND_AUDIO` | `RoundAudioBroadcast`: prévia e `offsetMs` (só a TV) | Junto com a contagem, e ao retomar ou reconectar. |
| `ANSWER_RECEIVED` | `AnswerReceivedBroadcast`: quem respondeu e quantos faltam | Cada resposta (não revela se acertou). |
| `ROUND_ENDED` | `RoundEndedBroadcast`: resposta, artista, capa, mais rápidos, resultados e placar | Fim da rodada (revelação). |
| `LOBBY_AUDIO` | `LobbyAudioBroadcast`: lista de prévias (só a TV) | Playlist escolhida ou trocada, "jogar novamente", TV reconectando no lobby. |
| `KICKED` | | O jogador foi expulso. |
| `ERROR` | `SocketErrorPayload`: código, mensagem, comando | Um comando falhou (fila de erros do participante). |

Os tempos dos eventos são sempre **"quanto falta" em milissegundos** (`durationMs`), nunca um horário. Cada cliente
converte para o próprio relógio, e assim relógios diferentes entre servidor e aparelhos não importam.

## 7. Os fluxos, passo a passo

### 7.1 Criar a sala

`POST /room` → `RoomAggregation.createRoom`:

1. `RoomService.createAndSave` gera um código de 6 caracteres e cria a sala `WAITING`. Se dois códigos colidirem, a
   constraint `unique` dispara e ele tenta de novo, até 5 vezes.
2. `MatchService.createFor` cria a partida com 10 rodadas de 20 s por padrão.
3. A TV recebe `id`, `code` e `secret` e conecta no socket como `host`.

### 7.2 Entrar na sala

`POST /room/join` → `RoomAggregation.joinRoom`:

1. `JoinRoomValidator.normalizeNickname` tira os espaços extras, converte para caixa alta e exige de 2 a 16
   caracteres: letras, números, espaço, `.`, `_` e `-`.
2. Sob o lock da sala, confere se a sala está `WAITING`, se não está cheia (`ROOM_MAX_PLAYERS`) e se o apelido não está
   em uso, e então cria o jogador com um personagem livre sorteado (`PlayerService.freeAvatar`). Os índices únicos
   `(room_id, nickname)` e `(room_id, avatar)` são a última barreira.
3. `PlayerService.ensureOwner` garante um VIP: o primeiro a entrar vira VIP.
4. `ROOM_STATE` (`PLAYER_JOINED`) para a sala.

### 7.3 Presença e VIP (`PresenceService`)

- **TV conecta:**
  1. Fecha a conexão anterior da mesma TV com o código 4001 (`SESSION_REPLACED`); a TV antiga mostra "sala aberta em
     outra tela".
  2. Marca a sala como conectada.
  3. Se a sala estava `PAUSED`, ela volta a `IN_PROGRESS` e o `GameEngine.resume` retoma a etapa.
- **TV cai:** marca a sala como desconectada. Se havia partida em andamento, a sala vira `PAUSED` e o
  `GameEngine.pause` congela o timer.
- **Jogador conecta ou cai:** marca o jogador e roda o `ensureOwner`. O VIP é mantido enquanto estiver conectado; se
  cair, a coroa passa ao jogador conectado mais antigo. **A queda de um jogador não pausa o jogo.**
- Desconexões de sessões já substituídas por uma conexão mais nova são ignoradas (compara o `sessionId`).
- Ao se inscrever em `/musicando/room`, o participante recebe a foto da sala. Com partida rolando, recebe também a etapa
  atual (`GameEngine.sendCurrentPhase`); a TV no lobby recebe também a música do lobby (`LobbyMusicService.sendCurrent`).

### 7.4 Configurar a partida (lobby)

`UPDATE_SETTINGS` → `UpdateSettingsCommandHandler`:

1. Valida contra as opções fixas de `MatchSettingsValidator`: rodadas 5/10/15/20 e duração 10/15/20/30 s.
2. Busca a playlist na Deezer e grava na partida.
3. `ROOM_STATE` (`SETTINGS_UPDATED`) para a sala.
4. Com playlist nova, `LobbyMusicService.refresh`:
   - sorteia 8 músicas tocáveis e guarda os IDs no Redis;
   - busca as prévias atuais e manda `LOBBY_AUDIO` à TV.

   Falhas da Deezer aqui só deixam o lobby sem música, sem quebrar o comando.

### 7.5 Iniciar a partida

`START_GAME` → `StartGameCommandHandler`:

1. Confere se quem mandou é o VIP, se a sala está `WAITING`, se há playlist e se há jogadores conectados suficientes
   (`ROOM_MIN_PLAYERS`).
2. `RoundGenerator.generate` sorteia as rodadas (seção 7.7).
3. Apaga a música do lobby do Redis, marca partida e sala como `IN_PROGRESS` e manda `ROOM_STATE` (`GAME_STARTED`).
4. `GameEngine.start` salva o `GameState` e começa a contagem da rodada 1.

### 7.6 O ciclo da rodada (`GameEngine`)

Toda mudança no `GameState` acontece **sob o lock da sala** (`GameLocks`). Cada etapa:

1. Grava a etapa e os horários no Redis (`enterPhase` + `gameStateService.save`).
2. Manda o evento da etapa para a sala (`phaseEvent`).
3. Agenda no `GameTimer` a chamada de `advance(sala, etapaEsperada, rodadaEsperada)` para o fim da etapa.

O `advance` só age se o estado ainda for exatamente o esperado. Assim, um timer atrasado ou duplicado não pula
etapas: se todos responderam e a rodada já foi para `REVEAL`, o timer antigo de `PLAYING` não faz nada.

| Etapa | Método | O que acontece |
|---|---|---|
| `COUNTDOWN` | `startCountdown` | Zera as respostas, **busca a prévia da música na Deezer** (os links expiram, por isso a busca é na hora), manda `ROUND_COUNTDOWN` e `ROUND_AUDIO` à TV. |
| `PLAYING` | `startPlaying` | Manda `ROUND_STARTED` com as opções. O tempo de resposta conta a partir daqui. |
| `REVEAL` | `endRound` | Soma pontos e acertos e manda `ROUND_ENDED`. Chamado pelo timer ou quando todos os conectados responderam. |
| Próxima ou fim | `nextRoundOrFinish` | Próxima contagem; na última rodada, grava pontos e acertos nos jogadores (SQLite), finaliza partida e sala (`FINISHED`) e manda `ROOM_STATE` (`GAME_FINISHED`). |

**Resposta** (`SUBMIT_ANSWER` → `GameEngine.submitAnswer`):

1. Só é aceita na etapa `PLAYING` da rodada certa, sem pausa, e uma vez por jogador.
2. O tempo de resposta é `agora - phaseStartedAt`.
3. A pontuação vem do `ScoreCalculator`: acerto vale de 500 a 1000 pontos, proporcional ao tempo que sobrava;
   erro vale 0.
4. Manda `ANSWER_RECEIVED`; se todos os conectados já responderam, encerra a rodada na hora.

Os tempos de contagem e revelação e quantos "mais rápidos" aparecem vêm de `musicando.game.*` (`GameProperties`).

### 7.7 Sorteio das rodadas (`RoundGenerator`)

1. Pega só as músicas tocáveis (com prévia) e **normaliza os títulos** (`TitleNormalizer`): tira "(Remastered)",
   "- Ao Vivo", "[feat. X]" e converte para caixa alta. Músicas com o mesmo título normalizado contam uma vez só.
2. Exige pelo menos `max(rodadas, 4)` títulos distintos, senão lança `PlaylistTooSmallException`.
3. Embaralha e escolhe as respostas, **deixando por último as músicas que tocaram no lobby**. Elas só viram resposta
   se faltar música.
4. Cada rodada tem a resposta e mais 3 títulos errados da mesma playlist, em ordem aleatória. A rodada guarda
   `trackId`, título, artista e capa (`GameRound`).

O sorteio usa o bean `RandomGenerator` (`RandomConfiguration`). Nos testes, ele é trocado por um `Random` com semente
fixa.

### 7.8 Jogar novamente

`PLAY_AGAIN` → `LobbyService.backToLobby`:

1. Para o motor e apaga o `GameState`.
2. Finaliza a partida antiga e cria uma nova com a mesma configuração (`MatchService.createNext`).
3. Zera pontos e acertos dos jogadores e reabre a sala (`WAITING`).
4. Faz um novo sorteio da música do lobby e manda `ROOM_STATE` (`PLAY_AGAIN`).

### 7.9 Queda da TV e reinício do backend

- **TV caiu:** `GameEngine.pause` cancela o timer e grava `pausedRemainingMs`. Ao voltar, `resume` recalcula
  `phaseStartedAt` e `phaseEndsAt` a partir do que faltava, reenvia a etapa e a prévia (com `offsetMs`) e reagenda.
- **Backend reiniciou** (os timers em memória se perderam):
  1. `ConnectionResetListener` marca todas as salas e jogadores como desconectados, porque nenhuma conexão sobreviveu.
  2. `GameRecoveryListener` passa pelas salas `IN_PROGRESS`/`PAUSED`:
     - **com** o `GameState` no Redis: a sala fica `PAUSED` e retoma quando a TV reconectar;
     - **sem** o `GameState` (Redis limpo ou expirado): a sala volta para o lobby.

### 7.10 Limpeza automática (`RoomCleanupService`)

A cada `CLEANUP_INTERVAL` (5 min):

- Encerra salas não encerradas, com a TV desconectada e sem alteração há `CLEANUP_ABANDONED_AFTER` (30 min).
- Apaga do banco as salas encerradas há mais de `CLEANUP_CLOSED_RETENTION` (24 h), com jogadores e partidas.

### 7.11 Encerrar, expulsar e sair

- **`CLOSE_ROOM`:** para o motor, marca a sala `CLOSED` e manda `ROOM_STATE` (`ROOM_CLOSED`). Os clientes voltam para a
  tela inicial.
- **`KICK_PLAYER`:** manda `KICKED` só para o jogador, apaga o jogador e repassa o VIP se for preciso.
- **`LEAVE_ROOM`:** mesma coisa, mas é o próprio jogador saindo.

## 8. Integração com a Deezer

- `DeezerClient` é uma HTTP interface do Spring com 4 chamadas:
  - playlists em destaque (`/chart/0/playlists`);
  - uma playlist (`/playlist/{id}`);
  - as músicas da playlist, paginadas de 100 em 100 (`/playlist/{id}/tracks`);
  - uma música (`/track/{id}`, para a prévia atual).
- `DeezerIntegrator` traduz as respostas para `MusicPlaylist` e `MusicTrack`.
  - Uma música é "tocável" se for legível na Deezer e tiver prévia.
  - O título usa o `title_short`, que vem sem a indicação de versão.
  - Lê no máximo `maxTracks` (300) músicas por playlist.
  - Qualquer falha HTTP vira `MusicProviderException` (`MUSIC_PROVIDER_UNAVAILABLE`, 503).
- Os links das prévias **expiram**. Por isso o jogo guarda só os IDs e busca a prévia na hora de tocar, tanto nas
  rodadas quanto no lobby.
- Para trocar de provedor (iTunes, por exemplo), implemente `MusicProvider` e troque o bean. O resto do backend só
  conhece a interface.

## 9. Concorrência

- **Lock por sala** (`GameLocks`): transições do timer, respostas, entrada de jogadores e troca de VIP mexem no mesmo
  estado e passam pelo mesmo `ReentrantLock` da sala. `run` aceita ações que lançam `BaseException`; `execute`, não.
- **Um timer por sala** (`GameTimer`): agendar uma transição cancela a anterior. O pool tem 4 threads (`game-timer-*`).
- **Ordem das mensagens:** `setPreservePublishOrder(true)` garante que cada cliente recebe os eventos na ordem em que
  foram gerados.
- **Instância única:** locks, timers, rate limit e registro de sessões são locais. Escalar horizontalmente exigiria
  levar tudo isso para o Redis (locks distribuídos, um timer único por sala) e trocar o broker simples do STOMP por
  um broker externo.

## 10. Configuração

Propriedades em `src/main/resources/application.properties`. Os perfis `dev` e `prd` definem banco, Redis, logs e
CORS.

| Variável | Padrão | Para quê |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `prd` na imagem Docker | Perfil. |
| `DATABASE_PATH` | `/musicando-backend/database/musicando.db` (prd) | Arquivo do SQLite. |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` / `REDIS_DATABASE` | `localhost` / `6379` / vazio / `0` | Redis. |
| `CORS_ALLOWED_ORIGINS` | obrigatório em prd | Origens da TV e do celular (REST e WebSocket), separadas por vírgula. |
| `ROOM_MAX_PLAYERS` / `ROOM_MIN_PLAYERS` | `12` / `1` | Limites da sala. |
| `GAME_COUNTDOWN` / `GAME_REVEAL` / `GAME_FASTEST_LIMIT` | `3s` / `5s` / `5` | Tempos da rodada e "mais rápidos" na revelação. |
| `CLEANUP_INTERVAL` / `CLEANUP_ABANDONED_AFTER` / `CLEANUP_CLOSED_RETENTION` | `5m` / `30m` / `24h` | Limpeza de salas. |
| `RATE_LIMIT_ROOM_CREATIONS` / `RATE_LIMIT_ROOM_JOINS` / `RATE_LIMIT_COMMANDS` | `10`/min / `20`/min / `20`/s | Rate limit (por IP na API, por participante no socket). |
| `DEEZER_BASE_URL` | `https://api.deezer.com` | API da Deezer. |

Em prd, `server.forward-headers-strategy=native` faz o rate limit usar o IP real do cliente atrás do Ingress.

## 11. Erros

- **Erros de regra** são subclasses de `BaseException` em `domain/exception/`, cada uma com status HTTP e `ErrorCode`.
  - Na API REST viram `ErrorResponse`.
  - No socket, o `SocketController` (`@MessageExceptionHandler`) manda um evento `ERROR` só para quem enviou o comando.
- **Falha de infraestrutura** (banco ou Redis fora, `DataAccessException`) vira `SERVICE_UNAVAILABLE`.
- **Erros inesperados** viram `INTERNAL_ERROR`, sem expor a mensagem interna.
- **Logs:** todo dado vindo de fora passa pelo `LogSanitizer` antes de ir para o log, contra injeção de linhas.

## 12. Receitas de manutenção

**Novo comando do socket**
1. Crie `handler/socket/command/XxxCommandHandler` implementando `SocketCommandHandler`, com `@Component`.
2. Registre em `SocketCommandType` com os papéis autorizados.
3. Valide o payload no handler e lance `SocketCommandException(ErrorCode.INVALID_COMMAND, ...)` se vier errado.
4. No frontend, acrescente o tipo em `SocketCommandType` de `types/protocol.ts` do app que envia.

**Novo evento do servidor**
1. Acrescente o tipo em `SocketEventType` e crie o `XxxBroadcast` em `dto/response/`. Use `List.copyOf` no construtor
   compacto, senão o SpotBugs reclama.
2. Envie com `SocketService.broadcastToRoom`, `sendToHost` ou `sendToParticipant`.
3. No frontend, acrescente o evento ao `types/protocol.ts` e trate no hook do socket. Eventos de jogo precisam entrar
   em `GAME_EVENTS` do `useHostSocket` (TV) ou do `usePlayerSocket` (celular), senão são ignorados.

**Mudar uma regra do jogo**

| Regra | Onde mexer |
|---|---|
| Tempos da contagem e da revelação | Variáveis `GAME_*`, sem mexer no código. |
| Pontuação | `ScoreCalculator`. |
| Opções de rodadas e duração | `MatchSettingsValidator`. O frontend lê de `GET /music/options`. |
| Quantas músicas tocam no lobby | `LobbyMusicService.TRACKS`. |
| Como os títulos são comparados | `TitleNormalizer`. |
| Personagens disponíveis | `AvatarValidator.AVATARS`, `avatar.utils.ts` do celular e `tools/avatars/render_avatars.py`; as imagens são geradas pelo script (veja `tools/avatars/README.md`). |

**Mudar o banco**
1. Altere a entidade em `domain/`.
2. Crie `src/main/resources/db/migration/V{n}__descricao.sql`. Nunca edite uma migration que já rodou.
3. O `ddl-auto=validate` derruba a inicialização se a entidade e o schema não baterem.

**Depurar uma partida travada**
1. Veja o `GameState` no Redis (seção 3): `phase`, `currentRound`, `phaseEndsAt` e `pausedRemainingMs`.
2. Se `pausedRemainingMs` estiver preenchido, a partida espera a TV reconectar.
3. Em `DEBUG`, o `SocketService` loga cada evento enviado.

## 13. Subir localmente com Docker

O `docker-compose.yaml` da raiz sobe quatro containers: backend, Redis, TV (`host-app`) e celular (`player-app`). O
backend não expõe porta: cada app serve o próprio build com nginx e repassa `/api` e `/ws` para o backend, então o
navegador só acessa as portas 8081 e 8082.

### Pré-requisitos

Docker com o Compose v2 (`docker compose version`). Não precisa de Java, Node nem Redis instalados.

### Passo a passo

1. Na raiz do projeto, crie o `.env` a partir do exemplo:

   ```bash
   cp .env.example .env
   ```

2. Edite o `.env`. Só estas variáveis importam para o Compose (o `REDIS_HOST` é ignorado):

   | Variável | Valor | Por quê |
   |---|---|---|
   | `REDIS_PASSWORD` | qualquer senha, ex.: `musicando` | O Redis do Compose sobe com essa senha e o backend usa a mesma. **Obrigatória.** |
   | `REDIS_DATABASE` | `0` | Banco lógico do Redis. |
   | `CORS_ALLOWED_ORIGINS` | `http://localhost:8081,http://localhost:8082` | Origens aceitas pelo backend, inclusive no WebSocket. Uma origem fora da lista é recusada. |
   | `PLAYER_APP_URL` | `http://localhost:8082` | Endereço do app do celular que vai no QR code da TV. |

3. Suba tudo:

   ```bash
   docker compose up --build
   ```

   O primeiro build demora alguns minutos (Maven e pnpm baixam as dependências). Os seguintes usam cache.

4. Abra a TV em <http://localhost:8081> e crie uma sala. Para simular um jogador no mesmo PC, abra
   <http://localhost:8082> em outra janela ou aba anônima e entre com o código da sala.

### Jogar com celulares na mesma rede

1. Descubra o IP do PC na rede local (`ipconfig` no Windows, `ip addr` no Linux), por exemplo `192.168.0.10`.
2. No `.env`, coloque o IP no QR code e nas origens permitidas:

   ```env
   PLAYER_APP_URL=http://192.168.0.10:8082
   CORS_ALLOWED_ORIGINS=http://localhost:8081,http://localhost:8082,http://192.168.0.10:8081,http://192.168.0.10:8082
   ```

3. Recrie os containers: `docker compose up --build`. O `PLAYER_APP_URL` entra no build da TV, então o `--build` é
   necessário.
4. Libere as portas 8081 e 8082 no firewall do PC, se ele bloquear. No Windows, o Windows Defender costuma perguntar
   na primeira vez.

### Dados, logs e comandos úteis

- **Banco SQLite:** fica em `./database/musicando.db`, na raiz, e sobrevive a `docker compose down`. O Redis do
  Compose não tem volume: ao recriar o container, partidas em andamento voltam para o lobby (seção 7.9).
- **Logs do backend:** `docker compose logs -f backend`.
- **Rebuild só do backend depois de mudar o código:** `docker compose up --build backend`.
- **Ver o estado de uma partida no Redis:**

  ```bash
  docker compose exec redis redis-cli -a "$REDIS_PASSWORD" --scan --pattern '@MUSICANDO:*'
  ```

- **Parar tudo:** `docker compose down`. **Começar do zero** (apaga as salas): `docker compose down` e depois apague a
  pasta `database`.

### Problemas comuns

| Sintoma | Causa provável |
|---|---|
| A TV fica em "Conectando à sala…" | Origem fora de `CORS_ALLOWED_ORIGINS`, por exemplo a TV aberta pelo IP sem o IP na lista. |
| O backend reinicia em loop com erro de Redis | `REDIS_PASSWORD` vazio ou diferente entre os containers. Ajuste o `.env` e rode `docker compose up -d --force-recreate`. |
| O backend não consegue gravar o SQLite (Linux) | A pasta `./database` foi criada pelo Docker como `root`, mas o backend roda com o usuário 10001. Rode `sudo chown 10001:10001 database`. No Windows e no macOS isso não acontece. |
| O QR code abre `localhost` no celular | `PLAYER_APP_URL` sem o IP, ou a TV não foi reconstruída depois da mudança (`--build`). |
| Sem som na TV | O navegador bloqueia áudio antes de um clique na página. Clique no aviso amarelo. |

## 14. Rodar, testar e qualidade

- **Desenvolvimento:** suba um Redis e rode `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`. O SQLite é criado
  em `musicando.db`, na pasta do backend.
- **Ambiente completo:** veja a seção 13.
- **Verificação completa:** `./mvnw verify` roda testes, Checkstyle e SpotBugs/FindSecBugs. É o mesmo que o CI roda,
  e o build falha com qualquer violação.
- **Testes:**
  - JUnit 5 + Mockito + AssertJ, com `Fixture.make` para gerar entidades.
  - `SocketProtocolIntegrationTest` sobe o contexto e testa o protocolo STOMP de ponta a ponta.
  - O `src/test/resources/application.properties` **substitui** o principal nos testes.
- **Checkstyle:** imports em grupos (terceiros, `javax`, `java`, estáticos), ordem de declaração, `HiddenField`, linha
  em branco antes de `if` e `return`, sem linha vazia no começo de bloco, sem `catch (Exception)`.

