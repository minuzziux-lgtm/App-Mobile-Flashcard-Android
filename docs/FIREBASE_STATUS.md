# Firebase — estado dos commits e validações

Projeto do usuário: **mydeck-kotlin**. Fonte exclusiva: `minuzziux-lgtm/Private`, branch `dev/mydeck-private`. 
Nenhuma implantação real ou publicação no repositório público foi executada.

## Implementado em código / testado

| Item | Situação | Evidência |
|---|---|---|
| 40. Projeto Spark e ambiente | **Código/documentação concluídos** | `a134df7`; `firebase.json` aponta a `firestore.rules` e usa `demo-mydeck-kotlin` no emulador |
| 41. Contratos de persistência | **Parcial** | `523e2fd`: contrato e adaptador local; integração remota completa às telas pendente |
| 42. Papéis/sessão | **Parcial** | Auth já existia; `9ca1bde` isola preferências por UID; sessão cloud ainda não é fonte de verdade das telas |
| 43. Turmas/matrículas | **Parcial** | `b108361`: username vinculado ao perfil no servidor, lista de matrículas, validar conta real |
| 44. Decks multiclasses | **Parcial** | `57daee3`: biblioteca canônica, atribuições e grants; UI ainda local |
| 45. Cinco tipos de cartões | **Parcial** | `0b4085b`, `890d0bd`: mapeamento e CRUD no repositório Firebase |
| 46. Tags/descrições/links | **Parcial** | Incluso no mapeamento dos cartões; validar campos e links com conta real |
| 47. Revisões e agenda | **Parcial** | `cde82bc`, `7085471`: mapeamento idempotente e leitura/gravação; UI ainda local |
| 48. Desempenho | **Parcial** | `89823cf`: agregador por usuário/turma/deck/tag; falta apresentar dados remotos |
| 49. Offline/conflitos | **Parcial** | `aa962c3`, `5d471ea`: detecção conservadora; sem aplicação/mesclagem automática |
| 50. Regras de segurança | **Testadas no emulador, não implantadas** | `12951d8`, `57daee3`, testes Firestore Suite |
| 51. Testes Emulator Suite | **Regras automatizadas funcionando** | `08e7f98`, `87deeb1`; testar fim a fim Android ainda pendente |
| 52. Aprovação professor | **Script preparado, não executado** | `469aff7` + `957b5b8` |
| 53. Migração sem duplicatas | **Pré-validação implementada** | `1cb707c`, `9ca17b8`; escrita remota ainda bloqueada por segurança |
| 54. Validação final e release | **Pendente** | Exige Firebase configurado, testes reais, revisão de custos e build release |

## Bloqueios concretos antes da ativação

1. Registrar app Android no Firebase com o package ID definitivo. Atualmente `com.example.myapplication`.
2. Colocar `app/google-services.json` **somente no Android Studio**; está ignorado pelo Git.
3. Habilitar **Authentication Email/Password** e criar **Cloud Firestore** no Console; conferir plano Spark.
4. Não publicar `firestore.rules` até validar o conjunto completo de cenários no emulador.
5. Integrar as chamadas do backend na UI sem sobrescrever `LocalMyDeckStore`; converter e migrar dados existentes mediante confirmação e registro idempotente.
6. Testar professor+aluno distintos, decks compartilhados, reatribuição/revogação, todas as formas de resposta e persistência entre dispositivos.
7. Aprovar perfis docentes **somente** por operador autorizado. O script `scripts/approve-teacher.mjs` usa Application Default Credentials e exige `GOOGLE_CLOUD_PROJECT=mydeck-kotlin`; nunca subir chaves de conta de serviço ao Git.

## Regras de proteção

- Não sincronizar automaticamente dados locais de contas diferentes.
- Não assumir que o projeto online está configurado apenas porque seu URL foi informado.
- Não executar alteração administrativa sem conexão autorizada e validação explícita.
- Não fazer deploy, release nem publicar no repositório público até aprovação dos testes.
- Firestore no Android possui cache offline padrão; isso não equivale à reconciliação sem conflitos.

## Testes disponíveis

- Android: `./gradlew :app:testDebugUnitTest :app:assembleDebug`.
- Instrumentados: `./gradlew :app:connectedDebugAndroidTest` no emulador Android.
- Regras locais: `npm install && npm run test:rules` usa o projeto de demonstração `demo-mydeck-kotlin`, não produção.
- GitHub Actions valida o Android e regras na branch privada. Uma execução cancelada por commit subsequente **não** comprova sucesso ou falha.

## Critério de conclusão

O aplicativo só estará integrado quando os dados de professores e alunos forem criados, lidos e atualizados realmente no Firestore, a UI não depender do armazenamento demonstrativo para sessões autenticadas, as regras forem publicadas e os testes de duas contas/dispositivos passarem sem exposição cruzada. 
