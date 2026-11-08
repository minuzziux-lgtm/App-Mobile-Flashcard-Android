# Estrutura do Firestore — MyDeck Kotlin

Esta etapa cria o modelo de dados e o repositório Kotlin, **não** substitui telas nem ativa a confirmação de e-mail.

## Coleções

| Caminho | Campos principais | Quem cria |
| --- | --- | --- |
| `users/{uid}` | uid, email, name, username, role, createdAt | Aluno / solicitante de professor |
| `usernames/{username}` | uid, createdAt | Cadastro autenticado |
| `classes/{classId}` | name, ownerUid, joinCode, createdAt | Professor aprovado |
| `classCodes/{code}` | classId, ownerUid, createdAt | Professor aprovado, na mesma transação da turma |
| `enrollments/{classId}_{studentUid}` | classId, studentId, joinCode, joinedAt | Aluno com código válido |
| `decks/{deckId}` | classId, ownerUid, title, createdAt | Professor dono da turma |
| `decks/{deckId}/cards/{cardId}` | question, answer, tags, createdAt | Professor dono do deck |
| `reviews/{reviewId}` | studentId, deckId, cardId, correct, reviewedAt | Aluno matriculado |

## Fluxo

1. O administrador aprova a conta de professor no perfil (`role=teacher`). `pending_teacher` não concede acesso.
2. O professor cria uma turma; uma transação cria também seu código único de 8 caracteres.
3. O aluno entra na turma com o código; a matrícula é criada com ID determinístico.
4. O professor cria decks vinculados à turma e adiciona cards.
5. O aluno consulta turmas e decks autorizados e registra revisões.

## Configuração no Firebase Console

Em **Firestore Database → Rules**, publique **o conteúdo atualizado de `firestore.rules`**. O commit no GitHub **não publica as regras** automaticamente. Não use regras abertas.

## Como integrar na interface

`MyDeckDataRepository.create(context)` retorna o repositório quando o JSON do Firebase está configurado. As funções `createClass`, `joinClass`, `listTeacherClasses`, `listStudentClasses`, `createDeck`, `listDecks`, `addCard`, `listCards` e `saveReview` recebem um callback `DataResult`.

As telas atuais de aluno/professor continuam demonstrativas até a integração com essas funções. As respostas de revisão ainda são autorrelatadas pelo cliente; o mecanismo de avaliação, espaçamento de revisões e análises detalhadas serão implementados separadamente.

**Atenção:** as regras são restritivas e a adição de coleções não libera acesso a documentos existentes. Validar no Emulator Suite criação, matrícula, leitura e respostas antes do uso real. Alguns acessos podem exigir índices do Firestore ao criar consultas compostas futuras.
