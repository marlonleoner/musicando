# player-app

App do jogador (celular) do Musicando.

```bash
pnpm install      # na pasta frontend
pnpm dev          # http://<ip-da-máquina>:5174
```

O jogador normalmente chega pelo QR code exibido na TV (`/?code=ABC123`), que já preenche o código da sala.

## Telas

| Tela | O que mostra |
|---|---|
| `Join` | Código da sala e apelido. |
| `Lobby` | Personagem do jogador (com troca), lista de jogadores e, para o VIP, a configuração da partida: playlist (as do perfil configurado no backend ou qualquer uma da Deezer, buscando pelo nome ou colando o link), rodadas e tempo por rodada. |
| `Game` | Botões das 4 opções (com as letras A–D, como na TV) e, na revelação, se acertou, a resposta e a posição no placar. |
| `FinalRanking` | Resultado final; o VIP pode levar todos de volta ao lobby. |
| `StatusScreen` | Avisos de estado: entrando na sala, partida pausada (TV desconectada), jogo aberto em outra aba. |

## Variáveis (`.env`)

| Variável | Para quê |
|---|---|
| `VITE_API_BASE_URL` | URL do backend. Sem valor, usa o endereço da página na porta 8080. Um caminho relativo (ex.: `/api`) indica backend na mesma origem. |
