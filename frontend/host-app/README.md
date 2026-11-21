# host-app

App da TV do Musicando: cria a sala, mostra o lobby com o QR code e toca as músicas da partida.

```bash
pnpm install      # na pasta frontend
pnpm dev          # http://<ip-da-máquina>:5173
```

Abra pelo IP da máquina (não `localhost`) para o QR code apontar para um endereço que o celular alcance.

## Telas

| Tela | O que mostra |
|---|---|
| `Home` | Botão de criar sala. |
| `Lobby` | Código da sala, QR code, jogadores com os personagens, resumo da configuração do VIP e música de fundo da playlist escolhida. |
| `Game` | Jumbotron com a barra de tempo, indicador de rodadas, timer redondo, placar no rodapé (com animação de reordenação e "+pontos") e, na revelação, capa, resposta, opções com quem marcou cada uma e os mais rápidos. |
| `FinalRanking` | Pódio dos 5 melhores, demais jogadores e confete. |
| `StatusScreen` | Avisos de estado: conectando, partida pausada, sala aberta em outra tela. |

## Áudio

- A TV é a única que recebe as prévias (30 s, da Deezer). Cada rodada começa num ponto sorteado pelo backend, com um
  fade in curto, e a música continua na revelação até o fade out (`useRoundAudio`).
- No lobby, toca trechos de músicas da playlist escolhida (`useLobbyAudio`).
- O volume é controlado pelo botão no canto inferior esquerdo e vale para músicas e efeitos sonoros.
- O navegador bloqueia som antes do primeiro clique na página; nesse caso a TV mostra um aviso para liberar o áudio.

## Variáveis (`.env`)

| Variável | Para quê |
|---|---|
| `VITE_API_BASE_URL` | URL do backend. Sem valor, usa o endereço da página na porta 8080. Um caminho relativo (ex.: `/api`) indica backend na mesma origem. |
| `VITE_PLAYER_APP_URL` | URL do app do celular usada no QR code. Sem valor, usa o endereço da página na porta 5174. |

As telas são feitas para TVs, que costumam informar uma área pequena ao navegador (cerca de 960×540): tudo usa
`h-dvh` e tamanhos em `vh`/`clamp()` para caber sem rolagem.
