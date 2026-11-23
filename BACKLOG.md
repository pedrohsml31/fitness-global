# BACKLOG — Fitness Global (v6.3)
## Em andamento
Objetivo: conectar Fitness ao cofre/backup automático do Pedro Ambiente sem depuração USB.
Feito: conexão cifrada curta RSA-OAEP no conteúdo web; preserva exportação/dados e assinatura do APK.
Próximo: preparar a chave no cofre do PC, publicar a conexão pública e receber o envio cifrado do celular.
## Pendências
- Decidir a assinatura do APK: o atual usa outra chave que não a pedro.keystore; publicação em pedrohsml31/apks continua pendente, sem gerar/trocar chave nesta entrega.
- Recolorir o ícone do app (grafite + verde-limão); exige recompilar o APK no `fitdev`.
- Compilar o APK da v4.3 num PC com o ambiente `fitdev` (este PC não tem); a v4.3 é só web (minNative 41) e chega pela atualização automática.
- Migrar dados do Firebase para o Supabase com login (decisão de 12/11); manter layout e funções.
- Backup automático Firebase autorizado (23/11); helper no Pedro Ambiente pronto, falta transferência inicial cifrada e validação real.
## Ideias
- Redesign: refinar o formulário de exercício e os cartões de Ajustes/Alimentação (restam emojis dentro de textos).
- Melhorias gerais do Fitness (lista a levantar).
