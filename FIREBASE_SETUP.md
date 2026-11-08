# MY-DECK — Firebase Authentication

O aplicativo possui Firebase Authentication (e-mail/senha), verificação de e-mail, recuperação de senha e perfis no Cloud Firestore. Sem configuração do projeto Firebase, os formulários exibem mensagem de configuração em vez de criar contas fictícias.

1. Crie ou selecione um projeto no [Firebase Console](https://console.firebase.google.com/).
2. Registre o aplicativo Android com **applicationId `com.example.myapplication`**. Baixe `google-services.json` e coloque-o em `app/google-services.json` (esse arquivo é ignorado pelo Git). O plugin Google Services é ativado automaticamente durante o build quando o arquivo existe.
3. Em Authentication → Sign-in method, habilite **Email/Password**. Configure os modelos de e-mail e os domínios permitidos se necessário.
4. Crie um banco **Cloud Firestore**, depois publique as regras de `firestore.rules` no console (ou faça deploy das regras). **Não use regras abertas em produção.**
5. Inicie o aplicativo e crie uma conta. Será enviado um e-mail para verificação. O primeiro login só será liberado após a confirmação do e-mail.
6. A seleção **Professor** registra uma solicitação `role = "pending_teacher"`, que não libera acesso de professor. Um administrador confiável deverá aprovar o usuário e modificar `role` para `teacher` pelo console/admin SDK. O cliente não pode se conceder esse papel.
7. Para testes locais, use um dispositivo ou emulador com Google Play Services e conexão ativa; teste criação de usuário, e-mail de verificação, erro de login, bloqueio de professor pendente e recuperação de senha.

`google-services.json` contém identificadores do projeto, não credenciais administrativas. **Nunca adicione arquivos de service account ou chaves privadas** ao repositório.

O código de login verifica o papel de usuário no Firestore e a verificação do e-mail, porém **os recursos administrativos futuros** precisam ter suas próprias regras do Firestore ou validação por backend; a checagem de UI não substitui controle de acesso no servidor.
