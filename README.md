# Musicando

A multiplayer "guess the song" party game. The **TV** creates a room and plays 30-second song previews; players join
from their **phones** by scanning a QR code and pick the right title among four options. Faster correct answers score
more points.

## How a game works

1. Open the TV app and create a room. A room code and a QR code appear on screen.
2. Players scan the QR code (or open the player app and type the code) and choose a nickname.
3. The first player to join is the **VIP**: they pick one of the configured Deezer profile's public playlists (or any
   Deezer playlist, searched by name or pasted as a link), the number of rounds and the round duration, then start the
   game.
4. Each round: a 3-second countdown, the song plays on the TV while players answer on their phones, then the answer,
   the fastest players and the scoreboard are revealed.
5. At the end the TV shows a podium; the VIP can send everyone back to the lobby to play again.

Scoring: a correct answer is worth 500 to 1000 points depending on how much time was left; wrong or missing answers are
worth 0. A round ends early once every connected player has answered. If the TV disconnects (or the backend restarts),
the game pauses and resumes where it stopped.

## Architecture

```
  TV (host-app)            Phones (player-app)
        \                       /
         \  HTTP + STOMP over WebSocket
          \                   /
          +-------------------+
          |      backend      |----> Deezer API (playlists, 30 s previews)
          |  Spring Boot 4.1  |
          +-------------------+
             |            |
           SQLite        Redis
   (rooms, players,   (state of games
     match results)     in progress)
```

| Folder | Description |
|---|---|
| `backend/` | Java 25 + Spring Boot 4.1. REST API (`/room`, `/music`) and STOMP over WebSocket (`/ws`). The game loop (timer, rounds, scoring) runs on the server. |
| `frontend/host-app/` | TV app: lobby with QR code, game screens, audio playback, final ranking. React 19 + Vite + Tailwind. |
| `frontend/player-app/` | Phone app: join, VIP game settings, answer buttons, results. React 19 + Vite + Tailwind. |
| `deploy/` | Kubernetes manifests (Kustomize) applied by Argo CD. |
| `.github/workflows/` | CI: tests, lint, images and deploy tag updates. |

The backend is designed to run as a **single instance**: the round timer, the per-room locks and the rate limiter live
in memory, and the database is a SQLite file.

## Local development

Requirements: Java 25, Node.js 22 with pnpm, and a Redis server.

### Backend

```bash
cd backend
# Set the environment variables below (e.g. in your IDE run configuration), then:
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The `dev` profile uses a local SQLite file (`musicando.db`); the schema is created and updated by Flyway migrations
(`src/main/resources/db/migration`).

Quality gates (also run by the CI):

```bash
./mvnw verify   # tests, Checkstyle and SpotBugs (with FindSecBugs); any violation fails the build
```

### Frontends

```bash
cd frontend
pnpm install
pnpm --filter host-app dev     # TV:    http://localhost:5173
pnpm --filter player-app dev   # Phone: http://localhost:5174
```

Without configuration, both apps call the backend on the same host they were opened from, port 8080. To play from real
phones, open the TV app using your computer's network IP (not `localhost`) so the QR code points to a reachable address.

| Variable | App | Description |
|---|---|---|
| `VITE_API_BASE_URL` | both | Backend URL. A relative path (e.g. `/api`) means the backend is served on the same origin, behind a proxy; the WebSocket then uses `/ws` on the same origin. |
| `VITE_PLAYER_APP_URL` | host-app | Player app URL used in the QR code. |

## Configuration (backend)

| Variable | Default | Description |
|---|---|---|
| `REDIS_HOST` / `REDIS_PORT` | `localhost` / `6379` | Redis server. |
| `REDIS_PASSWORD` | – | Redis password. |
| `REDIS_DATABASE` | `0` | Redis logical database (useful when sharing a Redis server). |
| `CORS_ALLOWED_ORIGINS` | – | Comma-separated origins allowed for REST and WebSocket (the TV and phone app URLs). |
| `DATABASE_PATH` | `/musicando-backend/database/musicando.db` | SQLite file (`prd` profile). |
| `ROOM_MAX_PLAYERS` / `ROOM_MIN_PLAYERS` | `12` / `1` | Players per room / connected players required to start. |
| `GAME_COUNTDOWN` / `GAME_REVEAL` / `GAME_FASTEST_LIMIT` | `3s` / `5s` / `5` | Round countdown, reveal duration and number of fastest players shown. |
| `CLEANUP_ABANDONED_AFTER` / `CLEANUP_CLOSED_RETENTION` / `CLEANUP_INTERVAL` | `30m` / `24h` / `5m` | Rooms without the TV are closed after this time; closed rooms are deleted after the retention. |
| `RATE_LIMIT_ROOM_CREATIONS` / `RATE_LIMIT_ROOM_JOINS` / `RATE_LIMIT_COMMANDS` | `10` / `20` / `20` | Room creations and join attempts per minute per IP; socket commands per second per player. |
| `RATE_LIMIT_MUSIC_SEARCHES` | `60` | Playlist searches and resolved links per minute per IP. |
| `DEEZER_USER_ID` | **required** | Deezer profile whose public playlists are offered to the VIP (the number in `deezer.com/profile/<id>`). The backend does not start without it. |
| `DEEZER_BASE_URL` | `https://api.deezer.com` | Deezer API. |

## Docker Compose

Runs the backend, Redis and both apps:

```bash
cp .env.example .env   # set REDIS_PASSWORD, DEEZER_USER_ID and, to play from phones, PLAYER_APP_URL with your machine's IP
docker compose up --build
```

TV: http://localhost:8081 · Phone: http://localhost:8082. Each app image serves the static build with nginx and
forwards `/api` and `/ws` to the backend, so the apps talk to the backend on their own origin.

## Deployment (Kubernetes + Argo CD)

The app runs in the homelab cluster. Its Kubernetes manifests live in the
[`homelab`](https://github.com/marlonleoner/homelab) repository (`apps/musicando`), next to the other homelab apps, and
are applied by Argo CD:

| Host | Service |
|---|---|
| `http://musicando.home.labz` | TV (host-app) |
| `http://musicando-play.home.labz` | Phone (player-app) – the QR code address |

The setup of the namespace secrets (`musicando-secret`, `ghcr-secret`) is documented in the homelab repository
(`docs/bootstrap.md`).

### Continuous delivery

On every push to `master`, after the tests pass, the CI:

1. builds the three images and pushes them to GHCR (`ghcr.io/marlonleoner/musicando-{backend,host,player}`) tagged
   `sha-<commit>`;
2. commits the new tag to `apps/musicando/kustomization.yaml` in the homelab repository
   (`chore: deploy musicando <sha>`);
3. Argo CD detects the change and rolls out the new version.

Rolling back is a `git revert` of that commit in the homelab repository.

### One-time setup (CI access to the homelab repository)

```bash
# Generate a key pair just for this purpose
ssh-keygen -t ed25519 -C "musicando-ci" -f musicando-ci -N ""
```

1. In the **homelab** repository: *Settings → Deploy keys → Add deploy key*, paste `musicando-ci.pub` and check
   **Allow write access**.
2. In the **musicando** repository: *Settings → Secrets and variables → Actions → New repository secret*, name it
   `HOMELAB_DEPLOY_KEY` and paste the content of `musicando-ci` (the private key).
3. Delete both key files from your machine.

## Credits

Player characters: [Mini Characters](https://kenney.nl/assets/mini-characters) by [Kenney](https://www.kenney.nl)
(CC0). The images are rendered from the 3D models by `tools/avatars` (see its README).
