# Fitness Global — Guia de uso

App de treino (estilo Hevy): registrar academias, exercícios, séries com cronômetro
de descanso e acompanhar sua progressão de carga. Funciona no **celular (app Android)**
e no **PC (navegador / PWA)**, com **sincronização em nuvem** entre os dois.

---

## 1. Instalar no celular (APK Android)

**Jeito mais fácil (baixar direto no celular):** abra no navegador do celular:

    https://pedrohsml31.github.io/fitness-global/dist/fitness-global.apk

O download começa. Depois toque no arquivo baixado e siga do passo 3.

Ou copie o arquivo `dist\fitness-global.apk` do PC para o celular (WhatsApp para
você mesmo, cabo USB, Google Drive, OneDrive).

Como instalar:

1. Toque no arquivo `fitness-global.apk` baixado.
2. O Android vai pedir para permitir "instalar apps de fontes desconhecidas" —
   confirme (é normal para apps fora da Play Store; pode aparecer como
   "permitir desta fonte" para o navegador ou gerenciador de arquivos).
3. Instale e abra. O ícone "Fitness Global" 💪 aparece na gaveta de apps.

Na primeira vez que iniciar um descanso, o app pede permissão de **notificações** —
aceite. É isso que faz o celular **vibrar e notificar mesmo com a tela apagada**.

> Dica: para o descanso ser exato mesmo com o celular "dormindo", em
> *Configurações do Android → Apps → Fitness Global → Alarmes e lembretes*,
> deixe permitido (alguns celulares pedem isso).

---

## 2. Ativar a sincronização (celular ↔ PC)

A nuvem (Firebase/Firestore) **já vem configurada dentro do app** — você não precisa
criar projeto nem colar nada. Só definir um **código secreto** e usar o **mesmo** nos
dois aparelhos:

1. Abra o app → **⚙️ Ajustes** → **Sincronização em nuvem** → **Ativar**.
2. Digite um **código de sincronização** — invente um difícil de adivinhar
   (ex.: `pedro-treino-8x2k9q`). Ele funciona como a senha do seu "baú" de dados.
3. No **outro aparelho**, faça o mesmo com o **MESMO código**.

Pronto: ao registrar um treino em um aparelho, ele aparece no outro sozinho. Dá para
editar tanto pelo app quanto pelo navegador — fica tudo igual. O ideal é usar um
aparelho por vez em cada treino (a nuvem resolve pelo registro mais recente).

> **Segurança:** quem não souber o seu código não acessa seus dados (as regras do
> Firestore impedem listar os baús). Use um código longo. As chaves do Firebase que
> ficam no app são públicas por natureza (identificam o projeto) e não dão acesso aos
> seus dados sem o código.

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
- **Reordenar:** dentro de uma academia, toque em **⇅ Reordenar exercícios**, use as
  setas ↑ ↓ para colocar na ordem que quiser e toque em ✓.
- **Observações no exercício 📝:** ao criar/editar um exercício, escreva notas como
  regulagem da máquina, pegada, inclinação. Elas aparecem no topo da tela do
  exercício, na hora do treino (e um 📝 marca na lista).
- **Pesos exatos:** o app guarda e mostra o peso exatamente como você digitou
  (ex.: 21,25 kg), sem arredondar.
- **Progressão:** o app diz de forma direta se sua **força está subindo, estável ou
  caindo** (com %), mostra sua **força atual** e seu **recorde**. Ao bater um recorde,
  aparece a comemoração **🎉 na hora**. Gráfico com **Força / Peso máx / Volume**.
- **Backup:** Ajustes → Exportar/Importar (arquivo `.json`).

O app tem **3 abas** na barra inferior:

### 🏋️ Treino
Tudo acima (academias, exercícios, séries, descanso, progressão).

### 🍽️ Alimentação
- **Adicionar alimento** um a um; cada um puxa o **horário** (editável) e entra numa
  **refeição** (nomeada). Refeições e **lanches** ficam marcados diferente.
- **Planejar** (🗓): monte refeições planejadas com horário; marque como **fixa 📌**
  (todo dia) ou só do dia; **copiar de ontem**; editar/excluir.
- No dia, **marque ✓ o que consumiu** do plano — o que ficar sem marcar conta como
  **não consumido**. Aderência (**% da dieta seguida**) aparece no topo.
- Em cada alimento: marcar **binge 🔴** ou **fora do planejado ⚠️**; editar/excluir.
- **Urges** (vontade de comer): registre a **intensidade (0–10)** e se **cedeu**.
- **Relatório (📊, 7 dias):** aderência, nº de urges, binges que cedeu, intensidade
  média/máxima, alimentos fora do plano, itens em refeições/lanches, e comparação
  com a semana anterior.

### 📊 Evolução
- **Peso** (registre todo dia), **BF %** (bioimpedância) e **medidas** (cintura,
  quadril, peitoral, ombros, bíceps, antebraço, coxa, panturrilha, pescoço).
- **Gráfico de peso**, **média semanal** comparando com semanas anteriores, e
  variação das medidas.
- **Atividade:** marque com um toque se no dia fez **🏋️ Musculação**, **🏃 Cardio**
  (ou os dois), o **tempo de cardio (min)** e os **passos** do dia. Tem uma faixa dos
  últimos 14 dias (toque num dia para editá-lo) e um resumo de 7 dias.
- **Samsung Health (Health Connect):** no app do celular, toque em
  **🔄 Importar do Samsung Health** para puxar **passos e treinos** automaticamente.
  Configuração (uma vez), no app **Health Connect** ("Saúde Connect"):
  1. Em **Permissões de apps / Dados e acesso** → **Samsung Health**: permita
     *gravar* (compartilhar) **Passos** e **Exercício**.
  2. Em **Fitness Global**: permita *ler* **Passos** e **Exercício**.
  Depois é só abrir o Fitness Global → ele sincroniza sozinho. *(Só no APK do celular.)*
- **Editar qualquer dia** pelo histórico ou tocando no dia na faixa.

---

## 5. Atualizar o app

A partir da versão instalada em 24/07, o app **se atualiza sozinho**: o APK carrega o
app direto do site (`server.url`), então toda melhoria que eu publico aparece
**automaticamente ao abrir o app** — sem baixar nem reinstalar nada.

- **Você só precisa reinstalar o APK** quando eu mexer em código **nativo** (novo
  recurso do celular, permissão, ícone). Nesses casos eu aviso.
- **PC/PWA:** também atualiza sozinho ao recarregar.
- **Offline:** depois da primeira abertura com internet, funciona offline (fica em
  cache).

Para recompilar o APK após mudança nativa: `recompilar-apk.ps1` (roda `cap sync` +
build). Mudança só de `index.html` **não** exige rebuild — é só publicar no GitHub.
