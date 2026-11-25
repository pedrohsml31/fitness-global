# Fitness Global

App de celular (Android/Capacitor) e PWA. Vale o padrão das regras globais (versão, nome com versão, ícone, atalho, sincronização, nunca arredondar peso); abaixo, só o que é deste projeto.

## Versão e build
- A versão visível mora em um lugar só (`APP_NAME_VERSION` no `index.html`); `recompilar-apk.ps1` a escreve em `strings.xml`, `build.gradle`, `index.html` e `manifest.webmanifest`. Nunca edite a versão em dois arquivos à mão.
- `APP_VERSION` é o número de build inteiro e crescente (`versionCode` do Android e comparação de atualização do app instalado). Trocar essa comparação por texto quebra a atualização de quem já tem o app.
- Ícone: `icon.svg` é a origem; `icon-192.png`, `icon-512.png` e os `mipmap-*/ic_launcher*` saem dele.
- Antes de compilar, extraia o `<script>` do `index.html` e valide com `new Function(src)` no node: um erro de sintaxe derruba o arquivo inteiro em silêncio.

## Dados e sincronização
- Nunca aponte `server.url` do Capacitor para outro domínio: troca a origem do WebView e apaga o `localStorage` (já causou perda de dados).
- Campo novo no `db` tem que entrar no `seed()` e no `Sync.data()`, senão nunca chega aos outros aparelhos. `Sync._adopt()` e `Sync.dataCount()` leem a lista de campos do próprio `seed()`: não volte a escrever os nomes à mão (campo esquecido volta vazio no aparelho que baixa da nuvem; `dataCount` decide se o aparelho está "vazio" e pode ser sobrescrito).

## Atualização do app
- Toda release precisa do `version.json` correto, senão quem já tem o app não recebe nada.
- `minNative` é o menor build nativo capaz de rodar aquele conteúdo web. Mexeu em Java/Kotlin, manifesto, permissão ou plugin? `minNative` = build desta release. Só HTML/CSS/JS? Mantenha o da última release nativa.
- `files` lista o que a atualização baixa; só texto (UTF-8). Mudou o `taco.json`? Inclua na lista.
- Plugin nativo novo tem que ser registrado no `MainActivity.java` e copiado pelo `recompilar-apk.ps1`, senão não existe no JS, sem erro.

## Testes
- `node tests/regras-grupo.js` confere `GRP_REGRAS` (grupo muscular) com ~110 nomes de exercício. Rode só quando mexer nessas regras; ao achar erro novo, acrescente o nome na tabela do teste.
