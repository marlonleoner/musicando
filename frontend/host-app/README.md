# 🎯 Roadmap: Host App (Web - Musicando)

## 🧱 Fase 1 – Setup e Fundamentos

- [x] Criar projeto com Vite + React + TypeScript
- [x] Configurar Tailwind CSS
- [x] Estruturar pastas: `pages/`, `components/`, `hooks/`, `services/`, `types/`
- [x] Configurar ESLint e Prettier com regras de padronização

---

## 🎮 Fase 2 – Fluxo de Criação e Gerenciamento da Sala

- [ ] Tela inicial para criação da sala:
    - [ ] Formulário com: nome do host + seleção da playlist + número de músicas + tempo por música
    - [ ] Botão “Criar sala” que realiza requisição para o backend via REST (`/rooms`)
    - [ ] Exibir código da sala gerado
- [ ] Armazenar informações da sala e jogador (host) no estado global (`useContext` ou `zustand`)
- [ ] Conectar ao WebSocket após a criação da sala

---

## 🧑‍🤝‍🧑 Fase 3 – Lobby da Sala

- [ ] Exibir lista de jogadores conectados
- [ ] Atualizar a lista em tempo real via socket (`/topic/room.{roomCode}`)
- [ ] Permitir que o host reinicie ou encerre a sala
- [ ] Exibir status da sala: aguardando jogadores / em andamento / finalizada

---

## 🎵 Fase 4 – Início do Jogo

- [ ] Botão “Iniciar Jogo” (ativa a entidade `Game` no backend)
- [ ] Iniciar envio de músicas de forma sincronizada para todos os players via socket
- [ ] Controlar cronômetro da rodada
- [ ] Exibir feedback visual de quem respondeu corretamente (em tempo real)

---

## 🏆 Fase 5 – Finalização e Feedback

- [ ] Exibir ranking final dos jogadores
- [ ] Botão “Reiniciar partida” (mantém jogadores, reseta game)
- [ ] Botão “Nova sala”

---

## 💡 Fase 6 – Extras e Melhorias

- [ ] Implementar loading states e tratamento de erros
- [ ] Melhorar usabilidade e responsividade (mobile/tablet)
- [ ] Tela de confirmação antes de sair da sala
- [ ] Aviso sonoro para início de rodada
- [ ] Componentes animados com Framer Motion (ex: entrada dos jogadores, resultados)

---

## 🛠️ Fase 7 – Integração com Back-End

- [ ] Endpoints REST: criação de sala, conexão do host, envio das configs
- [ ] Conexão WebSocket via STOMP:
    - [ ] `connect` com headers
    - [ ] `subscribe` aos tópicos da sala
    - [ ] `send` de mensagens como iniciar jogo, pular música, etc.
- [ ] Gerenciar reconexão do host automaticamente (em caso de refresh/desconexão)
