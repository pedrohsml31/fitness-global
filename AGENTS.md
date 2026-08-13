# Regras do projeto

Regras do Pedro para **qualquer app** (celular, PC ou navegador). Valem para este
repositório e devem ser seguidas em toda release.

## Versionamento

- Versão visível no formato **`X.Y`** — `1.0`, `1.1`, `1.2`…
- **Mudança grande sobe o primeiro número**: `1.9` → `2.0`. Correção ou funcionalidade
  pequena sobe o segundo: `2.0` → `2.1`.
- A versão mora em **um lugar só** (`APP_NAME_VERSION` no `index.html`) e o script de
  build a escreve nos outros. Nunca editar a versão em dois arquivos à mão.
- Junto dela existe um **número de build inteiro e crescente** (`APP_VERSION`). Ele não
  é para o usuário: serve para o Android (`versionCode`, que exige inteiro) e para o app
  instalado comparar se há atualização. Trocar essa comparação por texto quebraria a
  atualização de quem já tem a versão antiga instalada.

## Nome com a versão

O nome do app aparece **sempre com a versão junto**, no formato `Nome vX.Y`:

- ícone na tela inicial do celular / área de trabalho do PC;
- aba e título do navegador (`<title>`);
- nome do app instalado (Android `app_name` e `title_activity_main`);
- cabeçalho dentro do próprio app;
- tela de Ajustes.

Neste projeto o script `recompilar-apk.ps1` escreve isso automaticamente em
`strings.xml` e `build.gradle`; `index.html` e `manifest.webmanifest` acompanham.

## Ícone

- Todo app tem **ícone próprio, com cara do que ele faz** — nada de ícone genérico.
- **O mesmo ícone em todo lugar**: tela inicial do celular, área de trabalho, barra de
  tarefas, aba do navegador, tela de instalação. Um único desenho de origem, exportado
  nos tamanhos necessários.
- Neste projeto: `icon.svg` é a origem; `icon-192.png` / `icon-512.png` para o PWA e os
  `mipmap-*/ic_launcher*` do Android.

## Instalação

- App de celular precisa de **atalho na tela inicial**.
- App de PC / executável precisa de **atalho na área de trabalho**.
- Se for PWA, o site tem que ser instalável (manifest válido + ícones + `display:
  standalone`).

## Dados e sincronização

- **Nunca** depender de backup manual (exportar/importar) como forma de sincronizar.
  Os dados sincronizam na nuvem e o app funciona em qualquer celular ou PC.
- Peso **nunca é arredondado** — `71,38` não vira `71,4`.
- Nunca apontar `server.url` do Capacitor para outro domínio: isso troca a origem do
  WebView e apaga o `localStorage` (já causou perda de dados aqui).
- Campo novo no `db` **tem** que entrar no `seed()` e no `Sync.data()`, senão ele nunca
  chega nos outros aparelhos.

## Antes de compilar

- Rodar a checagem de sintaxe do script do `index.html`. Um erro de sintaxe derruba o
  arquivo **inteiro** em silêncio: nenhuma função é definida e o console não mostra nada.
- Nunca inventar dado de referência (nutrição, tabela, medida). Ou vem de fonte citável,
  ou o app mostra que não sabe e pede o valor.
