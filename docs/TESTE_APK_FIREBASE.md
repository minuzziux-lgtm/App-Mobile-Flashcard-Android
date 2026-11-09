# Gerar APK e verificar dados no Firebase real

**Objetivo:** testar o aplicativo Android com o projeto `mydeck-kotlin`, sem confundir a prévia local com gravações reais no Cloud Firestore.

## 1. Preparação obrigatória

- Firebase Console → `mydeck-kotlin` → Configurações do projeto → Seus aplicativos.
- Registrar/selecionar o Android com **package name exato `com.example.myapplication`**.
- Baixar `google-services.json` e copiar para `app/google-services.json` no projeto local. O arquivo deve ficar fora do Git (está em `.gitignore`).
- Em Authentication, verificar se **Email/Password** está habilitado.
- Em Firestore, conferir que o banco foi criado e que as regras de segurança foram revisadas e publicadas. Testes de regras no emulador não publicam regras no projeto real.
- Para testar professor, a conta deve ter `users/{uid}.role = teacher` autorizado; para aluno, `role = student`. Na entrada, use **login**, não **Prévia**.

## 2. Atualizar repositório e gerar APK no Windows

No terminal PowerShell do Android Studio, a partir da raiz do projeto:

```powershell
git status --short
git branch --show-current
git pull --ff-only origin main
powershell -ExecutionPolicy Bypass -File .\scripts\build-firebase-apk.ps1
```

Se `git status --short` exibir alterações locais, revise-as antes de executar `pull`; não descarte alterações. Se `origin` não for o repositório público, confira com `git remote -v`.

O script interrompe se `app/google-services.json` não existir, pertencer a outro projeto ou não tiver o pacote correto. Ele executa a tarefa Google Services, os testes unitários e o build do APK, sem escrever dados remotos.

**APK gerado:** `dist/MyDeck-Firebase-Test-debug.apk` (versão de **teste**, assinada com chave de debug).

Para gerar **e instalar** no emulador/celular conectado:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-firebase-apk.ps1 -Install
```

Em caso de bloqueio por versão antiga instalada, primeiro confirme se ela contém dados locais importantes: desinstalar o app apaga seus dados locais.

## 3. Teste de gravação do professor

1. Entre como professor com uma conta **autenticada**, não no botão de prévia.
2. Crie a turma `[TESTE] Persistencia-<numero>`; aguarde a confirmação de gravação Firebase.
3. Abra [Cloud Firestore → Dados](https://console.firebase.google.com/project/mydeck-kotlin/firestore/databases/-default-/data), coleção `classes`.
4. Encontre documento com esse `name`; anote o `ownerUid` e `joinCode`. Confira `classCodes/{joinCode}`.
5. No app crie deck `[TESTE] Verificacao real`, atribua à turma e crie um flashcard.
6. No Console confira `decks/{deckId}`, `classDeckAssignments/{classId}_{deckId}` e `decks/{deckId}/cards/{cardId}`.
7. Feche o app completamente e abra novamente com a mesma conta. Clique **Atualizar dados do Firebase**. Os itens devem reaparecer.

## 4. Teste de gravação do aluno

1. Saia da conta professor, entre como aluno **autenticado**.
2. Use o código da turma criado no teste, caso ainda não esteja matriculado.
3. Confirme o documento `enrollments/{classId}_{studentUid}` no Firestore.
4. Abra um deck atribuído à turma e conclua a avaliação de um cartão.
5. Confira `reviews/{reviewId}`: `studentId`, `deckId`, `cardId`, `classId`, `correct`, `dueAtEpochMillis`.
6. Feche e reabra o app, atualize os dados e confira o histórico. A revisão deve existir apenas uma vez.

## 5. Diagnóstico

- **App mostra apenas prévia/dados fictícios:** verifique se usou a rota de login real; a prévia não faz acesso ao Firebase.
- **Google Services ausente ou pacote incorreto:** repita o download de `app/google-services.json` do aplicativo correto em `mydeck-kotlin`.
- **Login falha:** confira Authentication, senha, verificação de e-mail e `users/{uid}.role` no Firestore.
- **Permissão negada:** as regras publicadas podem não corresponder às do repositório. Revise com os testes; não abra `allow read, write: if true`.
- **Ação aparece no app mas documento não está no Console:** verifique que selecionou `mydeck-kotlin`, que a conta está autenticada, e consulte os logs do Android Studio (Logcat). Uma sessão sem configuração válida não comprova persistência remota.
- **Revisão pendente:** o app pode guardar a revisão localmente para reenvio; só a existência do documento em `reviews` comprova que foi recebida no servidor.
- **Dados de teste não existentes:** `npm run seed:real` é um comando separado e requer autenticação administrativa via `gcloud`; a geração do APK não o executa.

**Não publique `google-services.json`, arquivos de conta de serviço ou senhas no repositório.** O `gcloud` não é necessário para compilar o APK.
