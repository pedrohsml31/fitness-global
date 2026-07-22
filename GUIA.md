# Fitness Global — Guia de uso

App de treino (estilo Hevy): registrar academias, exercícios, séries com cronômetro
de descanso e acompanhar sua progressão de carga. Funciona no **celular (app Android)**
e no **PC (navegador / PWA)**, com **sincronização em nuvem** entre os dois.

---

## 1. Instalar no celular (APK Android)

O app instalável fica em:

    Fitness Global\dist\fitness-global.apk

Como instalar:

1. Copie o arquivo `fitness-global.apk` para o celular (WhatsApp para você mesmo,
   cabo USB, Google Drive, ou a pasta do OneDrive no celular).
2. No celular, toque no arquivo `.apk`.
3. O Android vai pedir para permitir "instalar apps de fontes desconhecidas" —
   confirme (é normal para apps fora da Play Store).
4. Instale e abra. O ícone "Fitness Global" 💪 aparece na gaveta de apps.

Na primeira vez que iniciar um descanso, o app pede permissão de **notificações** —
aceite. É isso que faz o celular **vibrar e notificar mesmo com a tela apagada**.

> Dica: para o descanso ser exato mesmo com o celular "dormindo", em
> *Configurações do Android → Apps → Fitness Global → Alarmes e lembretes*,
> deixe permitido (alguns celulares pedem isso).

---

## 2. Ativar a sincronização (celular ↔ PC)

Assim seus treinos ficam iguais nos dois aparelhos. Você cria um projeto gratuito
no Firebase **uma vez**:

### Criar o projeto (uma vez só, ~5 min)

1. Acesse **console.firebase.google.com** e entre com sua conta Google.
2. Clique em **Adicionar projeto** → dê um nome (ex.: `fitness-global`) →
   pode **desativar** o Google Analytics → **Criar projeto**.
3. No menu esquerdo: **Criar** → **Firestore Database** → **Criar banco de dados**
   → escolha o local `southamerica-east1` → comece em **modo de teste** (ou produção,
   ver regras abaixo) → **Ativar**.
4. Ainda no projeto, clique no ícone **`</>`** (Web) para registrar um app Web →
   dê um apelido → **NÃO** marque Firebase Hosting → **Registrar app**.
5. Ele mostra um trecho `const firebaseConfig = { apiKey: "...", ... }`.
   **Copie só o objeto** `{ ... }` (das chaves `{` até `}`).

### Regras do Firestore (para os dados abrirem/salvarem)

No Firestore → aba **Regras**, cole e publique:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /vaults/{code} {
      allow read, write: if true;
    }
  }
}
```

> O "código de sincronização" que você define no app funciona como uma senha do seu
> baú de dados. Use um código **longo e difícil de adivinhar** (ex.:
> `pedro-treino-8x2k9q`). Quem não souber o código não acessa seus dados.

### Ligar no app (nos dois aparelhos)

1. Abra o app → **⚙️ Ajustes** → **Sincronização em nuvem** → **Configurar**.
2. Cole o `firebaseConfig` no campo indicado.
3. Defina um **código de sincronização** (o MESMO no celular e no PC).
4. **Conectar e sincronizar**.

Pronto: ao registrar um treino em um aparelho, ele aparece no outro. O ideal é usar
um aparelho por vez em cada treino (a nuvem resolve pelo mais recente).

---

## 3. Usar no PC

Duas opções:

- **Rápida:** abra o arquivo `index.html` no navegador (Chrome/Edge). Os dados ficam
  salvos no navegador. *(Obs.: abrindo direto do arquivo, a sincronização Firebase
  pode ser bloqueada pelo navegador; para sync no PC, prefira a opção hospedada.)*
- **Recomendada (vira app no PC também):** hospedar de graça no GitHub Pages. Veja
  `HOSPEDAR.md`. Aí você abre por um endereço `https://...`, instala como app
  (ícone na barra do Chrome) e a sincronização funciona igual à do celular.

---

## 4. Recursos do app

- **Academias** e, dentro delas, **exercícios** com nº de séries alvo e tempo de
  descanso próprio.
- **Treino:** confirme cada série (peso × reps). O **descanso inicia sozinho**, com
  botões **−15s / +15s** e cronômetro. No fim: **vibração + notificação** (som só se
  você ligar nos Ajustes — pensado para quando estiver de fone).
- **Exercício global 🌐:** marque no formulário para ele aparecer em **todas** as
  academias. Também dá para **copiar** um exercício para outra academia.
- **Progressão:** o app diz de forma direta se sua **força está subindo, estável ou
  caindo** (com %), mostra sua **força atual** e seu **recorde**. Ao bater um recorde,
  aparece a comemoração **🎉 na hora**. Gráfico com **Força / Peso máx / Volume**.
- **Backup:** Ajustes → Exportar/Importar (arquivo `.json`).

---

## 5. Atualizar o app depois de mudanças

O código-fonte é o `index.html` (mais `manifest.webmanifest`, `sw.js`, `icon.svg`).
Para gerar um novo APK depois de editar, veja `HOSPEDAR.md` (seção "Recompilar o APK").
