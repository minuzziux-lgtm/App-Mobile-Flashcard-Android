# MyDeck — auditoria de UX editorial e acessibilidade

**Escopo:** Android nativo, Jetpack Compose, branch `dev/mydeck-private`. Revisão de código, fluxos e telas fornecidas pelo usuário em 09/11/2026. Não é uma certificação de conformidade WCAG; testes com pessoas, leitor de tela e emulador ainda são necessários.

## Objetivo do redesign

Manter a linguagem **editorial** (Inter, títulos com personalidade, muito espaço em branco, linhas finas apenas em fronteiras de seções, navegação flutuante) sem sacrificar clareza, escaneabilidade ou controle. A ordem das ações deve ser **tarefa principal → conteúdo → opções secundárias**. A densidade deve ser reduzida por agrupamento e hierarquia, não simplesmente pela adição de margens indiscriminadas.

## Auditoria por problema / heurística

| Prioridade | Achado na implementação anterior | Critério / risco | Tratamento |
|---|---|---|---|
| P0 | `HomeAluno` enviava “Começar revisão” para a lista de decks, sem iniciar estudo; não havia atalho explícito “Estudar cartões” | Nielsen 1 (status), 3 (saída), 7 (eficiência) | **Corrigido:** escolha determinística do primeiro deck acessível e cartões elegíveis; CTA direto. |
| P0 | Retorno ao início dependia apenas da navegação lateral/inferior; percurso de estudo não deixava claro como voltar | Nielsen 3, 4 | **Corrigido:** navegação HOME pela pilha, ação textual “Voltar ao início”, saída do estudo iniciado pela Home. |
| P1 | Linhas separadoras antes de quase todos os itens em listas do aluno e do professor | Nielsen 8 (minimalismo): ruído visual, grupos pouco diferenciados | **Corrigido parcialmente:** removidas linhas repetidas nas linhas de lista, ampliados intervalos; linhas estruturais permanecem. |
| P1 | Tags e atribuição de turmas usavam semântica de botão de rádio (escolha única) embora fossem seleção múltipla | WCAG 4.1.2 nome, função, valor; Nielsen 4 | **Corrigido:** semântica de checkbox em múltiplas seleções. |
| P1 | Campos sem rótulo programático explícito e ações compactas com alvos pequenos | WCAG 1.3.1, 2.5.8, 4.1.2 | **Corrigido parcialmente:** descrição semântica de campos e ações com altura mínima de 48 dp. |
| P1 | Atribuir decks não apresentava confirmação textual explícita | Nielsen 1 (estado do sistema) | **Corrigido:** aviso após salvar, com transição discreta. |
| P1 | Tamanhos de fonte e blocos fixos podem provocar truncamento com fonte do sistema ampliada | WCAG 1.4.4 / 1.4.10, Android font scaling | **Pendente:** inspeção em 1,3× e 2×, telas estreitas e paisagem. |
| P1 | Barra inferior flutuante pode encobrir campos e foco ao abrir teclado ou TalkBack | WCAG 2.4.7, 2.4.11; Nielsen 3 | **Pendente:** testar foco, rolagem até item visível e IME. |
| P1 | Feedback de erro dos formulários fica como texto de rodapé e pode não ser anunciado pelo leitor de tela | WCAG 3.3.1, 3.3.3, 4.1.3 | **Pendente:** live region, associação campo–erro, foco no primeiro erro. |
| P1 | Nenhuma evidência de teste com TalkBack, navegação externa por teclado, contraste medido nos estados e múltiplas escalas | WCAG 2.1.1, 1.4.3, 1.4.11, 2.4.7 | **Pendente:** testes reais e relatório de achados. |
| P2 | Animações não tinham documentação de preferências de movimento reduzido | Nielsen 8; acessibilidade de movimento | **Parcial:** fades curtos de 90–160 ms; verificar com animações desativadas. |
| P2 | Decks e turmas podiam ser difíceis de relacionar, tornando o resultado de atribuição pouco explícito | Nielsen 6 (reconhecimento vs memória) | **Corrigido em commits anteriores:** cartões por deck, turmas atribuídas e confirmação. |

## Parâmetros para validação

- **Contraste:** medir texto comum ao menos **4,5:1** e texto grande **3:1** (WCAG 1.4.3 AA); componentes e limites informativos **3:1** quando exigível (1.4.11).
- **Alvos de toque:** WCAG 2.5.8 AA exige pelo menos **24 × 24 px CSS** ou exceções previstas no padrão web. No Android, o projeto adota **48 dp como meta de toque**, sem afirmar equivalência direta entre dp e px CSS.
- **Foco e orientação:** todos os controles alcançáveis, estados selecionado/desabilitado anunciados, nenhum foco coberto pelo painel flutuante; sem dependência exclusiva de cor.
- **Tipografia:** largura de 320–393 dp, fonte 1× / 1,3× / 2×; sem cortes de botões, rolagem horizontal não intencional ou sobreposição.
- **Navegação:** Home → Estudar → Avaliar → Concluir → Voltar; Home → Revisão pendente → Estudar; qualquer aba → Início; botão Voltar do Android; troca de abas sem perder dados.
- **Estados:** vazio, carregamento, sem turma, sem deck, sem cartão, revisão zerada, sucesso/erro de formulário, seleção múltipla, tema claro/escuro, português/inglês.
- **Animações:** foco e ação permanecem previsíveis mesmo com animações do sistema desligadas; sem efeito decorativo que distraia da tarefa.

## Checklist de commits UX — privado, sem publicação oficial

- [x] **UX-01** `fix(navigation): abrir estudo da home e garantir retorno ao inicio` — `e82b364`; implementação, validar no emulador.
- [x] **UX-02** `test(navigation): validar escolha direta de estudo e revisoes` — `688c016`; testes unitários adicionados, execução CI pendente.
- [x] **UX-03** `refactor(ui): ampliar respiro editorial e reduzir divisores repetidos` — `fc5d94f`; layout alterado, revisar visualmente.
- [x] **UX-04** `fix(a11y): semantica de selecao campos e alvos de toque` — `de0596f`; validar TalkBack e testes.
- [x] **UX-05** `feat(ui): adicionar transicoes discretas e feedback de atribuicao` — `7fb0f21`; validar movimento reduzido.
- [x] **UX-06** `test(navigation): cobrir acesso direto ao estudo e retorno a home` — `6ec64db`; teste instrumentado adicionado, ainda sem aprovação em emulador.
- [x] **UX-07** `docs(ux): registrar auditoria editorial e checklist de acessibilidade` — este documento.
- [ ] **UX-08** `fix(ui): adaptar listas e formularios para fontes ampliadas` — validar em telas pequenas, 1,3× e 2×.
- [ ] **UX-09** `fix(a11y): anunciar erros e orientar recuperacao de formularios` — avisos de validação por campo e live regions.
- [ ] **UX-10** `fix(a11y): garantir foco visivel e conteudo nao obscurecido` — teclado, barra inferior, navegação assistiva.
- [ ] **UX-11** `fix(ui): revisar contraste de textos e estados por tema` — medir tokens em estados normal, ativo, desabilitado, erro e foco.
- [ ] **UX-12** `feat(ui): refinar feedback contextual da sessao de estudo` — distinção entre avaliação, próxima revisão e conclusão.
- [ ] **UX-13** `test(a11y): validar TalkBack toque fontes grandes e movimentos` — checklist manual + instrumentado quando aplicável.
- [ ] **UX-14** `test(ux): revisar fluxos de ponta a ponta e aprovar UI editorial` — gravação/capturas e aceite no emulador.

**Regra:** marcar como concluída a *implementação de código*, mas não declarar conformidade, aprovação visual nem testes executados sem evidência. O escopo Firebase é separado e permanece no backlog 40–54.

## Fontes e referencial

- Nielsen Norman Group, *10 Usability Heuristics for User Interface Design*: https://www.nngroup.com/articles/ten-usability-heuristics/
- W3C, *Web Content Accessibility Guidelines (WCAG) 2.2*: https://www.w3.org/TR/WCAG22/
- W3C Brasil, tradução da WCAG 2.2: https://www.w3c.br/traducoes/wcag/wcag22-pt-BR/index-03-07-2024.htm

Avaliação heurística fundamentada nas diretrizes, **não** auditoria automatizada conclusiva de cada critério WCAG. Para Android nativo, aplicar os princípios e testar com recursos de acessibilidade da plataforma.
