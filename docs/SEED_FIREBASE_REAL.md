# Dados fictícios no Firebase real — MyDeck

Este procedimento cria **contas reais de Authentication com identidades fictícias** e dados de demonstração no **Cloud Firestore verdadeiro** do projeto `mydeck-kotlin`.

> Não é o Firebase Emulator Suite. **Consome cotas reais do projeto** e requer autenticação administrativa no seu computador. Não marque este teste como aplicação validada sem testar login e permissões no Android.

## Conteúdo preparado

- Professor: `professor.teste.mydeck@example.com`, perfil `teacher` autorizado exclusivamente como conta QA
- Aluno: `aluno.teste.mydeck@example.com`, perfil `student`
- **Senhas fortes e diferentes**, geradas automaticamente, nunca salvas em arquivos versionados
- As duas contas vêm com `emailVerified: true` apenas porque não possuem caixa de e-mail; **não usar em produção para pessoas reais**
- 3 turmas: Matemática, Inglês, Ciências
- 5 decks: um compartilhado entre duas turmas e um propositalmente sem turma
- 25 flashcards cobrindo os cinco tipos do aplicativo
- Matrículas, atribuições, concessões de leitura e avaliações históricas fictícias

A turma Matemática recebe código `QA2MATH3`; Inglês `QA2LANG3`; Ciências `QA2NATU3`.

## Passo 1 — requisitos no Console

1. [Firebase Authentication](https://console.firebase.google.com/project/mydeck-kotlin/authentication/users): método de login **Email/Password** habilitado.
2. [Cloud Firestore](https://console.firebase.google.com/project/mydeck-kotlin/firestore/databases/-default-/data): banco padrão criado.
3. Firebase Android configurado com package `com.example.myapplication` e arquivo local `app/google-services.json`.
4. Regras do Firestore revisadas e publicadas **somente após os testes**. O seed usa Admin SDK e não substitui implantação das regras.

## Passo 2 — autenticação administrativa no seu Windows

Instale Node.js e a [Google Cloud CLI](https://cloud.google.com/sdk/docs/install), caso ainda não tenha.

No PowerShell dentro da raiz do projeto:

```powershell
npm install
gcloud auth application-default login
npm run seed:plan
```

O login do `gcloud` deve usar uma conta com permissão de administrar **Authentication e Firestore** do projeto `mydeck-kotlin`. O `firebase login` sozinho não substitui esse login administrativo para o Admin SDK.

Se houver erro de projeto de cobrança/cotas das credenciais, confira se a conta tem permissão e, quando apropriado, use:

```powershell
gcloud auth application-default set-quota-project mydeck-kotlin
```

Não envie credenciais, chaves privadas nem as senhas de teste pelo chat.

## Passo 3 — gravar no projeto REAL

**Este comando grava dados reais do teste e é intencionalmente protegido pela confirmação do nome do projeto.**

```powershell
npm run seed:real
```

Ao terminar, o próprio terminal mostra **os dois e-mails e as duas senhas**.

As senhas ficam também em `.mydeck-test-credentials.local.json` no seu computador; o caminho está no `.gitignore`. Proteja o arquivo, não compartilhe e remova-o quando terminar os testes.

Rodar novamente não duplica turmas ou flashcards. Se o banco já contém este seed e você perdeu as senhas, pode gerar novas senhas apenas para **essas duas contas fictícias**:

```powershell
node scripts/seed-real-mydeck.mjs --apply --project mydeck-kotlin --confirm REAL-mydeck-kotlin --reset-passwords
```

## Passo 4 — conferir no Firebase

- `Authentication → Users`: 2 usuários QA com os e-mails acima e e-mail marcado como verificado
- `Firestore → users`: dois perfis, com `role = teacher` e `role = student`
- `classes`, `classCodes`, `enrollments`: 3 turmas e matrículas do aluno
- `decks`, `classDeckAssignments`, `deckAccess`: atribuições e acesso do aluno
- `decks/{deckId}/cards`: 25 cartões no total
- `reviews`: avaliações históricas, úteis para testar desempenho
- `qaSeedManifests/qa_mydeck_real_v1`: controle de idempotência

O script **nunca reescreve documentos antigos sem um manifesto específico de seed**; se encontrar colisão de IDs, ele interrompe.

## Passo 5 — entrar no Android

- Tela professor → usar e-mail e senha de **PROFESSOR** mostrados no terminal
- Sair → Tela aluno → usar e-mail e senha de **ALUNO**
- Testar acesso aos decks atribuídos, cartões, revisão, histórico e desempenho
- Confirmar que o deck sem turma não aparece ao aluno
- Ações feitas na prévia do Android Studio continuam locais e **não vão para o Firebase**

### Diagnóstico

- `gcloud não é reconhecido`: Google Cloud CLI não instalada ou terminal não reiniciado após instalar
- `Could not load default credentials`: executar `gcloud auth application-default login`
- `PERMISSION_DENIED`: verificar permissões IAM, serviços ativados e regras em vigor
- Login com conta QA falha apesar de as contas existirem: verificar `app/google-services.json`, Authentication Email/Password, configuração de projeto, dados em `users` e regras publicadas
- Não enviar a captura do terminal com as senhas; se precisar de ajuda, remova os valores antes

**Aprovar um professor de verdade exige processo administrativo separado.** Essas contas são exclusivamente para validar o aplicativo e devem ser desativadas/excluídas ao concluir os testes.
