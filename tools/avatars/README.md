# Avatares (personagens dos jogadores)

As imagens dos personagens ficam em `frontend/host-app/public/avatars` e `frontend/player-app/public/avatars` e são
geradas por `render_avatars.py` a partir do pacote **Mini Characters** da Kenney (licença CC0: uso livre, inclusive
comercial; o crédito à Kenney é opcional, mas está no README principal).

Para cada personagem são gerados três arquivos:

| Arquivo | Conteúdo | Onde aparece |
|---|---|---|
| `<id>-idle.webp` | Animação parada em loop (16 quadros, 12 fps), corpo inteiro, 256 px | Lobby da TV e do celular |
| `<id>-cheer.webp` | Pose de comemoração (quadro da animação `jump`), 256 px | Mais rápidos e pódio da TV, com o pulinho feito em CSS |
| `<id>-face.webp` | Só o rosto, 128 px | Placar, listas de jogadores e escolha de personagem |

A lista de personagens (`CHARACTERS` no script) é a mesma do backend (`AvatarValidator.AVATARS`) e do app do celular
(`src/utils/avatar.utils.ts`). Para adicionar ou remover um personagem, mude as três listas.

## Gerar as imagens

1. Baixe o pacote em <https://kenney.nl/assets/mini-characters> e coloque o zip na raiz do projeto (zips são ignorados
   pelo Git).
2. Instale o Blender como biblioteca Python e o Pillow. O pacote `bpy` exige a versão de Python que o Blender usa
   (3.13 para o `bpy` 5.x):

   ```bash
   pip install bpy==5.3.0 pillow
   ```

3. Na pasta `tools/avatars`, rode:

   ```bash
   python render_avatars.py --pack ../../kenney_mini-characters.zip \
       --out ../../frontend/host-app/public/avatars \
       --out ../../frontend/player-app/public/avatars
   ```

   Use `--only robot zombie` para gerar só alguns personagens. A renderização é feita pela CPU (Cycles) e leva cerca de
   40 segundos por personagem.

Ângulo da câmera, tamanho das imagens, quantidade de quadros e a animação da comemoração ficam nas constantes do começo
do script.
