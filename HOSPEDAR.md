# Hospedar no PC e recompilar o APK

## A) Usar no PC como app (GitHub Pages — grátis)

Isso dá um endereço `https://...` próprio, funciona offline, instala como app no
Chrome/Edge e faz a sincronização Firebase funcionar igual à do celular.

1. Crie uma conta em **github.com** (se não tiver).
2. Crie um repositório novo (ex.: `fitness-global`), pode ser **público**.
3. Envie estes arquivos para o repositório (arraste na página do GitHub →
   "uploading an existing file"):
   - `index.html`
   - `manifest.webmanifest`
   - `sw.js`
   - `icon.svg`
4. No repositório: **Settings → Pages → Source: Deploy from a branch → branch
   `main` / pasta `/root` → Save**.
5. Em ~1 minuto o site fica no ar em `https://SEU-USUARIO.github.io/fitness-global/`.
6. Abra esse endereço no PC e no celular. No Chrome, menu **⋮ → Instalar app**.

> Depois de qualquer mudança no `index.html`, basta reenviar o arquivo atualizado
> para o GitHub que o site atualiza sozinho.

---

## B) Recompilar o APK depois de editar o app

Toda a base para compilar já está pronta nesta máquina:

- Node: `C:\Users\pedro\AppData\Local\Microsoft\WinGet\Packages\OpenJS.NodeJS.LTS_*\node-v24.18.0-win-x64\`
- JDK 17: `C:\Users\pedro\fitdev\jdk\jdk-17.0.19+10`
- Android SDK: `C:\Users\pedro\fitdev\android`
- Projeto Capacitor: `C:\Users\pedro\fitdev\native`

Para gerar um novo APK depois de editar o `index.html`, rode o script:

    recompilar-apk.ps1

(clique com o botão direito → "Executar com PowerShell", ou rode no terminal).
Ele copia os arquivos web para o projeto, compila e coloca o APK novo em
`dist\fitness-global.apk`.

---

## C) Como o app funciona por dentro

- **Mesmo código** (`index.html`) roda no PC (navegador) e dentro do app Android
  (empacotado com **Capacitor**).
- No **app Android**, o descanso usa **notificações locais nativas** — por isso
  vibra/notifica com a tela apagada, sem depender do navegador.
- No **navegador**, o descanso usa notificação da web + um truque de áudio silencioso
  para o cronômetro não congelar (funciona na maioria dos casos, mas o app nativo é
  o mais confiável — por isso o APK).
- **Sincronização:** Firestore (Firebase). Cada aparelho lê/grava o mesmo "baú"
  identificado pelo seu **código de sincronização**.
