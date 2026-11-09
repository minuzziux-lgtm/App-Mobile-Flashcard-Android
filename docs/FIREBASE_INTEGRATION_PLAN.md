# Firebase do MyDeck — implementação privada

**Projeto confirmado pelo usuário:** `mydeck-kotlin`. **Branch de trabalho:** `dev/mydeck-private`.
Este repositório **não** possui acesso administrativo ao Firebase apenas porque o link do Console foi informado.

## Antes de ativar produção

1. Confirmar o ID definitivo do pacote Android (hoje `com.example.myapplication`) e registrar **esse** aplicativo no projeto `mydeck-kotlin`.
2. Baixar o `google-services.json` no Console e guardar **somente** em `app/google-services.json`, ignorado pelo Git. Não enviar contas de serviço, tokens, chaves privadas ou credenciais pelo chat.
3. Habilitar Authentication **Email/Password**, configurar os domínios e modelos de verificação e habilitar Cloud Firestore. Criar conta de teste de aluno e professor; o professor requer aprovação administrativa.
4. Antes de publicar regras ou migrar dados: executar o Emulator Suite com projeto **`demo-mydeck-kotlin`**, provar regras negativas e positivas e revisar custos/quota do Spark.
5. Testar duas contas e duas instalações: professor cria turma/deck, atribui a várias turmas, aluno ingressa por código, estuda, registra revisão; verificar isolamento de dados e atualização após reinicialização.
6. Publicar apenas após revisão de regras e consentimento explícito para qualquer implantação; sem Storage e sem ativar faturamento.

## Arquitetura e limites

- A biblioteca de demonstração `LocalMyDeckStore` continua existindo até a migração ser validada.
- O repositório remoto usa `FirebaseAuth.currentUser.uid` e Firestore Security Rules como fonte de verdade da autorização: nunca confiar no papel passado pela tela.
- Os decks são recursos reutilizáveis atribuídos a múltiplas turmas, seus flashcards não são duplicados por turma. Matrículas e revisões são identificadas por usuário.
- Revisões devem ter ID idempotente, não gerar eventos duplicados; recuperar intervalos com horários documentados e confirmar eventual conflito entre dispositivos.
- Firestore no Android possui persistência offline por padrão, mas isso não substitui controle de conflito ou migração idempotente.
- Atualmente `firestore.rules` e `MyDeckDataRepository` usam um esquema legado de deck com uma única `classId`; **não implantar** essas regras em produção sem atualizar/testar o esquema novo.
- `app/google-services.json` não está no Git. Builds CI sem o arquivo verificam apenas comportamento em modo local e componentes que compilam sem projeto Firebase configurado.

## Operação local com CLI

Depois de instalar Firebase CLI e Java, na raiz do repositório:

```bash
firebase emulators:start --only firestore --project demo-mydeck-kotlin
```

O arquivo `firebase.json` aponta explicitamente para `firestore.rules`. O projeto demo não deve ler/escrever produção.

## Critérios para marcar os itens 40–54

Cada item só é fechado após commit com código real e testes proporcionais. Regras de segurança só aprovadas após testes com autenticado/desautenticado, aluno de outra turma, professor de outra turma, perfis pendentes e tentativas de alterar papeis. A validação de funcionamento real depende do Firebase conectado e de testes no Android Studio; não marcar somente porque compila.
