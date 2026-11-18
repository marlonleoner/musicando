# syntax=docker/dockerfile:1

# Imagem de um dos apps (host-app ou player-app): build com Vite e nginx servindo os arquivos estáticos.
# O nginx também repassa /api e /ws para o backend, então o app fala com o backend na própria origem.

FROM node:22-alpine AS build
ARG APP
ARG VITE_API_BASE_URL=/api
ARG VITE_PLAYER_APP_URL=
WORKDIR /build
RUN corepack enable
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
COPY host-app/package.json host-app/
COPY player-app/package.json player-app/
RUN --mount=type=cache,target=/root/.local/share/pnpm/store \
    pnpm install --frozen-lockfile --filter "${APP}..."
COPY ${APP}/ ${APP}/
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL} \
    VITE_PLAYER_APP_URL=${VITE_PLAYER_APP_URL}
RUN pnpm --filter "${APP}" build

FROM nginxinc/nginx-unprivileged:1.29-alpine
ARG APP
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /build/${APP}/dist /usr/share/nginx/html
ENV BACKEND_URL=http://musicando-backend:8080
EXPOSE 8080
