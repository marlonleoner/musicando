# backend

API do Musicando: Java 25 + Spring Boot 4.1, REST (`/room`, `/music`) e STOMP sobre WebSocket (`/ws`). O jogo inteiro
(timer, sorteio das rodadas, pontuação) roda aqui.

- Como rodar, configurar e publicar: [README principal](../README.md).
- Como funciona por dentro (pacotes, dados, protocolo, fluxos e receitas de manutenção): [ARCHITECTURE.md](ARCHITECTURE.md).

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev   # precisa de Redis e de DEEZER_USER_ID
./mvnw verify                                           # testes, Checkstyle e SpotBugs
```
