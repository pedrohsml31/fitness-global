# Fitness Global 💪

App de treino (estilo Hevy): registre academias, exercícios e séries, com cronômetro
de descanso e acompanhamento de progressão de carga. Funciona no **celular (APK Android)**
e no **PC (PWA no navegador)**, com **sincronização em nuvem** opcional (Firebase).

## Como usar

- **Abrir no PC / celular:** acesse a página publicada (GitHub Pages) e, no Chrome,
  use **⋮ → Instalar app**.
- **Instalar o app Android:** baixe [`dist/fitness-global.apk`](dist/fitness-global.apk)
  no celular e toque para instalar.
- **Guia completo:** veja [`GUIA.md`](GUIA.md) (instalação + sincronização Firebase).
- **Hospedar / recompilar:** veja [`HOSPEDAR.md`](HOSPEDAR.md).

## Recursos

- Academias com exercícios próprios; **exercício global 🌐** (aparece em todas).
- Séries alvo e **tempo de descanso por exercício**, com **−15s / +15s**, vibração e
  notificação ao fim (som opcional).
- **Progressão intuitiva:** diz se sua força está subindo, estável ou caindo, mostra a
  força atual e o recorde, e **comemora 🎉 quando você bate um recorde**.
- Dados no aparelho (offline) + backup em arquivo + sincronização em nuvem.

## Estrutura

| Arquivo | O quê |
|---|---|
| `index.html` | O app inteiro (HTML/CSS/JS, sem dependências) |
| `manifest.webmanifest`, `sw.js`, `icon.svg` | PWA (instalável + offline) |
| `dist/fitness-global.apk` | App Android pronto para instalar |
| `GUIA.md`, `HOSPEDAR.md` | Guias de uso e publicação |
