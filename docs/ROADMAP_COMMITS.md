# MyDeck — backlog de commits para concluir o aplicativo

> Fonte de trabalho: `dev/mydeck-private` em `minuzziux-lgtm/Private`.
> O repositório público `minuzziux-lgtm/App-Mobile-Flashcard-Android` não deve receber alterações até a conclusão, testes e organização da publicação oficial.

## Como interpretar esta lista

- **Concluído aqui** significa *código enviado à branch privada*, não que a funcionalidade foi aprovada no emulador ou no Firebase.
- **Pendente** significa *tarefa planejada*: não criar commits vazios ou simular implementação para satisfazer a numeração.
- Os números **10–54 são identificadores de backlog**; commits técnicos adicionais, correções e testes podem ser feitos quando necessários. A sequência oficial será reorganizada **depois** do aplicativo completo.
- O cronograma antigo em `Private/main/schedule.csv` contém horários históricos **não mais válidos**; não usá-los para publicação automática.
- A branch `Private/main` é dedicada à automação, não ao código Android. Trabalhar nesta branch de desenvolvimento, com commits reais, revisáveis e testes.
- Não ativar `RELEASE_ENABLED` e não publicar no repositório público sem decisão posterior.

## Resumo

- **45 itens previstos (10–54)**.
- **30 itens com implementação enviada (10–39)**, ainda sujeitos a validação Android/UX.
- **15 itens Firebase (40–54)**: integração em andamento, com pendências registradas em `docs/FIREBASE_STATUS.md`.
- **Próxima prioridade:** integrar as operações Firestore às telas autenticadas, validar migração e configurar `mydeck-kotlin`.

## Estudo e revisão (itens 10–20)
- [x] **10** `feat(study): implementar sessoes de estudo` — implementado em `dbeee9f`; build/emulador ainda devem ser verificados formalmente
- [x] **11** `feat(study): implementar cartoes basicos` — implementado em `506c692`; build/emulador ainda devem ser verificados formalmente
- [x] **12** `feat(study): implementar multipla escolha` — commit `758cd4c`; pendente validar build/aceite
- [x] **13** `feat(study): implementar completar lacunas` — commit `4385628`; pendente validar build/aceite
- [x] **14** `feat(study): implementar cartoes reversiveis` — commit `2aae452`; pendente validar build/aceite
- [x] **15** `feat(study): implementar resposta livre` — commit `2d683cf`; pendente validar build/aceite
- [x] **16** `feat(ui): apresentar materiais complementares` — commit `3354f90`; pendente validar build/aceite
- [x] **17** `feat(study): implementar progresso da sessao` — commit `150a73d`; pendente validar build/aceite
- [x] **18** `feat(review): registrar resultados dos cartoes` — commit `6599330`; pendente validar build/aceite
- [x] **19** `feat(review): implementar repeticao espacada` — commit `9f5658f`; pendente validar build/aceite
- [x] **20** `feat(review): organizar revisoes pendentes` — commit `5575612`; pendente validar build/aceite


## Desempenho do aluno e do professor (itens 21–28)
- [x] **21** `feat(ui): atualizar indicadores de revisao` — commit `ea83575`; pendente validar build/aceite
- [x] **22** `feat(student): implementar historico de estudos` — commit `cfab580`; pendente validar build/aceite
- [x] **23** `feat(student): implementar grafico de frequencia` — commit `2c2d37c`; pendente validar build/aceite
- [x] **24** `feat(student): filtrar desempenho por deck e tag` — commit `c55666b`; pendente validar build/aceite
- [x] **25** `feat(teacher): implementar dashboard de desempenho` — commit `eee38be`; pendente validar build/aceite
- [x] **26** `feat(teacher): detalhar desempenho dos alunos` — commit `0e2f776`; pendente validar build/aceite
- [x] **27** `feat(teacher): analisar cards dificeis por tag` — commit `98014d9`; pendente validar build/aceite
- [x] **28** `feat(teacher): filtrar graficos por periodo` — commit `a55f70d`; pendente validar build/aceite


## Perfis, UI, acessibilidade e testes (itens 29–39)
- [x] **29** `feat(profile): finalizar perfis dos usuarios` — commit `b4bece3`; pendente validar build/aceite
- [x] **30** `refactor(ui): consolidar design system` — commit `0336449`; pendente validar build/aceite
- [x] **31** `fix(ui): corrigir responsividade e acessibilidade` — commit `05d5f58`; pendente validar build/aceite
- [x] **32** `feat(ui): implementar tema claro e escuro` — commit `c96a7b8`; pendente validar build/aceite
- [x] **33** `feat(i18n): adicionar portugues e ingles` — commit `eb3bbb7`; pendente validar build/aceite
- [x] **34** `feat(demo): preparar cenarios completos de teste` — commit `4f21f3c`; pendente validar build/aceite
- [x] **35** `test(cards): validar criacao e edicao de cartoes` — commit `8b60fee`; pendente validar build/aceite
- [x] **36** `test(study): validar sessoes e respostas` — commit `fb4dd87`; pendente validar build/aceite
- [x] **37** `test(review): validar agendamento e indicadores` — commit `ff1e9c2`; pendente validar build/aceite
- [x] **38** `test(data): validar persistencia e isolamento de perfis` — commit `62a934d`; pendente validar build/aceite
- [x] **39** `chore(ci): validar build e testes automatizados` — CI privado registrado em `4d5632b`; testes Android da versão atual ainda precisam de confirmação


## Integração Firebase, segurança e entrega (itens 40–54)

**Importante:** componentes de backend e testes já foram implementados para parte dos itens 41–53, mas permanecem abertos até integração às telas e validação com o Firebase real. Veja `docs/FIREBASE_STATUS.md` para os SHAs e bloqueios específicos.
- [x] **40** `chore(firebase): documentar projeto Spark e ambientes` — commit `a134df7`; plano e emulator config criados
- [ ] **41** `refactor(data): criar interfaces para repositorios locais e remotos`
- [ ] **42** `feat(auth): integrar sessao e papeis reais`
- [ ] **43** `feat(firebase): integrar turmas codigos e matriculas`
- [ ] **44** `feat(firebase): integrar decks gerais e atribuicoes`
- [ ] **45** `feat(firebase): persistir cinco tipos de cartoes`
- [ ] **46** `feat(firebase): persistir tags descricoes e links`
- [ ] **47** `feat(firebase): salvar progresso e agenda de revisoes`
- [ ] **48** `feat(firebase): integrar consultas de desempenho`
- [ ] **49** `feat(data): tratar sincronizacao offline e conflitos`
- [ ] **50** `fix(security): validar regras Firestore por perfil`
- [ ] **51** `test(firebase): testar regras no Emulator Suite`
- [ ] **52** `feat(auth): formalizar aprovacao administrativa`
- [ ] **53** `feat(data): migrar dados locais com controle de duplicatas`
- [ ] **54** `chore(release): validar quotas indexes e build final`


## Critérios de passagem

1. **Estudo:** cada tipo de cartão pode ser respondido; sessão interrompida e retomada sem perder progresso; resultados persistidos e repetição espaçada consistente. Apenas autoavaliação onde não houver correção automática.
2. **Desempenho:** estatísticas derivadas de dados reais, com turma, deck, aluno, tag e período; estados vazios e quantidades acima de 40 revisões testados.
3. **Qualidade:** experiência em telas pequenas, navegação, contraste, tema claro/escuro e idiomas; testes unitários e build debug aprovados.
4. **Firebase:** usar Spark/Authentication/Firestore, sem Storage e sem custos habilitados; testar regras no Emulator Suite, isolamento por conta, sincronização, migração e duas contas/dispositivos.
5. **Publicação oficial:** somente após validação integral. Preparar commits reais e revisados; não reescrever o histórico público nem reativar o cronograma vencido sem novo plano.

## Regra de atualização

Ao finalizar um item, marcar `[x]`, anexar o SHA do commit real e registrar resultados de compilação/testes. Se estiver implementado mas não testado, deixar explícita a pendência da validação. Corrigir defeitos antes de considerar um bloco aprovado.
