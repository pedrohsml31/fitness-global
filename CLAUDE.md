@AGENTS.md

## Só para o Claude Code

- Antes de compilar, extrair o `<script>` do `index.html` e validar com
  `new Function(src)` no node. Erro de sintaxe derruba o arquivo inteiro em silêncio.
- Depois de publicar, conferir o APK que está no ar (baixar e checar `APP_VERSION`
  dentro de `assets/public/index.html`), não só o build local.
- Rodar `node tests/regras-grupo.js` antes de publicar (regras de grupo muscular).
