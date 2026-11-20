# MyDeck — Backlog de 100 próximos commits de refinamento

**Situação:** planejamento, não 100 commits já executados. Os commits enviados nesta rodada são independentes deste backlog.

**Critérios:** alterações reais, uma responsabilidade por commit; compilar e rodar testes antes de marcar como concluído; não sobrescrever dados locais; respeitar modo de movimento reduzido e manter separação entre divisão decorativa e sublinhado de input.

**Referências:** Apple Human Interface Guidelines / Motion; Google Material Motion e Compose; Nielsen Norman Group sobre animação funcional.


## 01 — Navegação e hierarquia

- [ ] 001 `refactor(nav): unificar estados de destino professor e aluno`
- [ ] 002 `feat(nav): restaurar scroll por aba ao retornar`
- [ ] 003 `fix(nav): preservar contexto da turma em telas internas`
- [ ] 004 `feat(nav): criar retorno contextual na pilha de aulas`
- [ ] 005 `fix(nav): evitar dupla navegacao por toques rapidos`
- [ ] 006 `feat(nav): indicar carregamento de destino demorado`
- [ ] 007 `refactor(nav): separar tabs e rotas de detalhe`
- [ ] 008 `test(nav): cobrir navegacao ida e volta por perfil`
- [ ] 009 `test(nav): garantir ausencia do menu em telas modais`
- [ ] 010 `docs(nav): registrar mapa de fluxos e destinos`

## 02 — Linguagem de movimento

- [ ] 011 `refactor(motion): centralizar duracoes e curvas`
- [ ] 012 `feat(motion): adicionar transicao de detalhe da turma`
- [ ] 013 `feat(motion): animar revelacao de feedback da aula`
- [ ] 014 `feat(motion): animar recolhimento de filtros de desempenho`
- [ ] 015 `feat(motion): animar abertura de detalhes do deck`
- [ ] 016 `feat(motion): aplicar fade-through no estado vazio`
- [ ] 017 `feat(motion): suavizar mudanca de tema sem flashes`
- [ ] 018 `fix(motion): impedir deslocamentos durante digitacao`
- [ ] 019 `test(motion): verificar preferencia por animacoes reduzidas`
- [ ] 020 `docs(motion): publicar especificacao de duracoes e curvas`

## 03 — Formulários e estados

- [ ] 021 `refactor(forms): unificar campo editavel com linha de base`
- [ ] 022 `feat(forms): destacar foco por cor e espessura`
- [ ] 023 `feat(forms): animar erros inline sem deslocar teclado`
- [ ] 024 `fix(forms): manter rotulo acessivel com hint visivel`
- [ ] 025 `feat(forms): oferecer contador para comentarios longos`
- [ ] 026 `feat(forms): preservar rascunhos ao navegar`
- [ ] 027 `fix(forms): evitar envio duplicado em toques repetidos`
- [ ] 028 `feat(forms): indicar salvamento e sucesso discretamente`
- [ ] 029 `test(forms): validar teclado fonte grande e estados vazios`
- [ ] 030 `docs(forms): catalogar padroes de input editorial`

## 04 — Tags e seleção

- [ ] 031 `refactor(tags): separar chip de selecao e chip informativo`
- [ ] 032 `feat(tags): permitir quebra fluida em varias linhas`
- [ ] 033 `fix(tags): manter altura minima acessivel em cada chip`
- [ ] 034 `feat(tags): animar selecao sem alterar largura`
- [ ] 035 `fix(tags): medir centralizacao optica dos rotulos`
- [ ] 036 `feat(tags): criar estados foco pressionado selecionado`
- [ ] 037 `feat(tags): listar tags por relevancia e uso`
- [ ] 038 `fix(tags): evitar truncamento de tags compridas`
- [ ] 039 `test(tags): verificar acessibilidade sem depender da borda`
- [ ] 040 `docs(tags): registrar dimensoes e variantes`

## 05 — Frequência e análises

- [ ] 041 `feat(stats): permitir alternar semanas e meses`
- [ ] 042 `feat(stats): adicionar resumo de melhor semana`
- [ ] 043 `feat(stats): mostrar dias ativos consecutivos`
- [ ] 044 `feat(stats): adicionar grafico de taxa de acertos`
- [ ] 045 `feat(stats): combinar frequencia e acertos no filtro`
- [ ] 046 `feat(stats): comparar periodo atual e anterior`
- [ ] 047 `feat(stats): permitir selecionar turma no grafico do professor`
- [ ] 048 `feat(stats): mostrar detalhes acessiveis por periodo`
- [ ] 049 `test(stats): validar meses com fuso horario e DST`
- [ ] 050 `docs(stats): explicar formulas e interpretacao`

## 06 — Conteúdo e aulas

- [ ] 051 `feat(lessons): exibir roteiro compacto da aula`
- [ ] 052 `feat(lessons): permitir reordenar etapas do planejamento`
- [ ] 053 `feat(lessons): salvar alteracoes como rascunho local`
- [ ] 054 `feat(lessons): avisar sobre saida com mudancas pendentes`
- [ ] 055 `feat(lessons): permitir duplicacao de planejamento`
- [ ] 056 `feat(lessons): vincular multiplos decks quando necessario`
- [ ] 057 `feat(lessons): detalhar status da publicacao`
- [ ] 058 `fix(lessons): recuperar conclusao apos perda de rede`
- [ ] 059 `test(lessons): cobrir repeticao e falhas de publicacao`
- [ ] 060 `docs(lessons): documentar estados e permissoes`

## 07 — Dados fictícios e testes de fluxo

- [ ] 061 `feat(demo): oferecer reinicio explicito do cenario`
- [ ] 062 `feat(demo): incluir dados para turma sem atividade`
- [ ] 063 `feat(demo): simular aluno com historico completo`
- [ ] 064 `feat(demo): simular aluno com dificuldades frequentes`
- [ ] 065 `feat(demo): adicionar aulas sem flashcards`
- [ ] 066 `feat(demo): incluir feedback de diferentes perfis`
- [ ] 067 `feat(demo): disponibilizar materia de apoio por tag`
- [ ] 068 `feat(demo): rotular claramente dados de exemplo`
- [ ] 069 `test(demo): impedir populacao em contas reais`
- [ ] 070 `docs(demo): registrar roteiros de avaliacao`

## 08 — Performance e acessibilidade

- [ ] 071 `fix(a11y): adequar contraste dos chips em ambos temas`
- [ ] 072 `feat(a11y): adicionar descricoes detalhadas dos graficos`
- [ ] 073 `feat(a11y): indicar selecao com texto e nao somente cor`
- [ ] 074 `fix(a11y): garantir ordem de leitura por leitor de tela`
- [ ] 075 `fix(a11y): ajustar quebra de texto em fontes 200 por cento`
- [ ] 076 `fix(perf): reduzir recomposicao desnecessaria dos graficos`
- [ ] 077 `fix(perf): limitar custo de listas de revisoes longas`
- [ ] 078 `test(a11y): verificar tamanhos minimos de toque`
- [ ] 079 `test(a11y): validar navegação por teclado e switch access`
- [ ] 080 `docs(a11y): auditar WCAG e orientacoes Android`

## 09 — Firebase, segurança e sincronização

- [ ] 081 `fix(firebase): manter estado de carregamento coerente`
- [ ] 082 `feat(firebase): apresentar ultima sincronizacao da turma`
- [ ] 083 `fix(firebase): tratar perda de rede durante feedback`
- [ ] 084 `feat(firebase): reprocessar envios pendentes com idempotencia`
- [ ] 085 `fix(firebase): validar roles antes de ações de aula`
- [ ] 086 `test(firebase): verificar permissao por matricula real`
- [ ] 087 `test(firebase): impedir leitura entre turmas`
- [ ] 088 `test(firebase): validar revogacao de acessos`
- [ ] 089 `test(firebase): verificar fallback sem rede`
- [ ] 090 `docs(firebase): atualizar roteiro de deploy das regras`

## 10 — Qualidade, release e dispositivo

- [ ] 091 `test(android): compilar em modo debug e release`
- [ ] 092 `test(android): executar suites em API 24 e 35`
- [ ] 093 `test(android): validar navegacao por gestos do sistema`
- [ ] 094 `test(android): verificar interface no Samsung com barra inferior`
- [ ] 095 `test(android): comparar telas em claro e escuro`
- [ ] 096 `test(android): validar reinstalacao sem perda de dados`
- [ ] 097 `ci(android): publicar diagnostico de compilacao`
- [ ] 098 `ci(android): anexar apk e checksum ao build aprovado`
- [ ] 099 `docs(release): elaborar roteiro de instalacao do apk`
- [ ] 100 `chore(release): preparar marco de avaliacao visual`

**Nota:** novos compromissos de implementação precisam de build e teste; nenhuma linha acima é prova de implementação.
