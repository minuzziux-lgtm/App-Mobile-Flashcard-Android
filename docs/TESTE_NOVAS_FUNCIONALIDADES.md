# MyDeck — validar aulas, materiais, recomendacoes e feedback

## Atualizar o projeto no VS Code

Abra o terminal PowerShell na raiz do repositório e confira a branch e alterações locais:

```powershell
git status --short
git branch --show-current
git remote -v
```

Se o repositório de origem for `minuzziux-lgtm/App-Mobile-Flashcard-Android`,
a branch for `main` e não houver alterações locais pendentes:

```powershell
git pull --ff-only origin main
```

Não descarte alterações locais para forçar o pull.

## Compilar no Windows

A configuração Firebase do Android precisa estar em `app/google-services.json`
(arquivo local, não enviado ao Git). Use o projeto Firebase `mydeck-kotlin`
e o pacote `com.example.myapplication`.

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-firebase-apk.ps1
```

O script executa testes, gera o APK e o copia para
`dist/MyDeck-Firebase-Test-debug.apk`.

Para instalar em um Android com depuração USB habilitada:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-firebase-apk.ps1 -Install
```

Alternativamente, o workflow `Auth verification` compila na branch `main`
e, **se o build for bem-sucedido**, disponibiliza o artefato
`mydeck-debug-apk` na aba Actions do GitHub. A presença de commits
não comprova a aprovação desse workflow.

## Publicar regras Firestore

A implementação de aulas, materiais e feedback inclui novas regras em
`firestore.rules`. Fazer git pull **não** publica essas regras. Antes de
testar com duas contas reais, revise e implante as regras no projeto correto:

```powershell
firebase use mydeck-kotlin
firebase deploy --only firestore:rules
```

Se o alias não existir, selecione explicitamente o projeto configurado.
Teste as regras no Emulator Suite antes de publicar. Não use regras abertas.

## Fluxo de aceite

1. Professor autenticado: criar turma e usar o convite para adicionar um deck.
2. Abrir **Turmas > Aulas, materiais e feedback**; criar uma aula sem cartões e concluí-la.
3. Criar outra aula, preparar cartões e concluir; verificar se aparecem uma única vez no deck.
4. Na aba Materiais, associar um link HTTPS de PDF ou vídeo a um deck, aula, tag ou cartão.
5. Aluno autenticado e matriculado: abrir **Turmas > Aulas e materiais**; consultar aulas concluídas.
6. Avaliar uma aula com comentário e enviar sugestão geral; conferir no painel do professor.
7. Responder no mínimo cinco cartões avaliados com uma mesma tag e menos de 60% de acertos;
   conferir se a sugestão aparece na home e indica material relacionado.
8. Testar modo claro/escuro, navegação, tamanhos de tela, sair e entrar novamente.
9. Testar com professor e aluno em contas diferentes para confirmar permissões.
10. Conferir logs, build e testes; corrigir falhas antes de considerar a versão validada.

## Limitações intencionais

- PDF é associado como **link HTTPS**. Upload binário de PDF ao Firebase Storage
  não está incluído; não publicar anexos diretamente no banco.
- Recomendações são calculadas por regras transparentes (tag, quantidade de
  respostas avaliadas e taxa de acertos), sem IA generativa.
- A prévia usa dados locais; contas autenticadas usam Firestore.
- Dados em nuvem requerem regras publicadas e conexão funcional.
- O APK de debug é para testes e não uma versão de distribuição na Play Store.
