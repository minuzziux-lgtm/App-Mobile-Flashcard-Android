# MyDeck — Referências para microinterações e linguagem editorial

## Referências verificadas

1. Apple, **Human Interface Guidelines — Motion**. https://developer.apple.com/design/human-interface-guidelines/motion
   - Movimento deve esclarecer relações e fornecer feedback, não servir como decoração.
2. Apple, **Apple Design Awards 2026 — Interaction**. https://developer.apple.com/design/awards/
   - Referência de qualidade e controles intuitivos, não cópia da aparência de um app premiado.
3. Google, **Material Motion — Building Beautiful Transitions**. https://codelabs.developers.google.com/codelabs/material-motion-android
   - Fade-through em áreas distintas; shared axis em navegação hierárquica; container transform quando houver um elemento compartilhado.
4. Android Developers, **Quick guide to animations in Compose**. https://developer.android.com/develop/ui/compose/animation/quick-guide
   - APIs Compose para transições e mudanças de estado.
5. Nielsen Norman Group, **Executing UX Animations: Duration and Motion Characteristics**. https://www.nngroup.com/articles/animation-duration/
   - Movimentos curtos, percebidos sem causar espera.
6. Nielsen Norman Group, **Animation for Attention and Comprehension**. https://www.nngroup.com/articles/animation-usability/
   - Movimento para continuidade espacial e compreensão.

## Decisões para o MyDeck

- **Navegação entre áreas:** fade-through (aprox. 190–240 ms) e deslocamento vertical mínimo; nunca uma tela inteira passando como slide.
- **Retorno:** tempo semelhante e direção sutilmente inversa, preservando contexto.
- **Abas:** feedback gradual do indicador ativo, sem pulos de layout.
- **Botões:** compressão leve durante toque (0,985×), sem atraso na ação.
- **Frequência:** gráfico de 12 barras semanais com seleção por toque; dados e valores legíveis sem depender exclusivamente das cores.
- **Tags/filtros:** borda identificável nos dois temas; texto centrado e alvos de toque acessíveis.
- **Campos de escrita:** linha inferior obrigatória; **não** equivale a um divider decorativo.
- **Hierarquia:** espaçamento e contraste tipográfico substituem barras de separação.
- **Movimento reduzido:** respeitar as configurações do sistema Android; sem efeitos necessários para compreender informações.
- **Dados fictícios:** apenas rotas de prévia; não contaminar contas autenticadas nem sobrescrever dados locais.

## Critérios antes de aprovar uma versão

- CI deve compilar `assembleDebug` e passar `testDebugUnitTest`.
- Navegação deve ser inspecionada no celular (gestos e botão Voltar).
- Testar com fonte ampliada, modo escuro/claro e animações do sistema desativadas.
- Verificar não só a fluidez, mas a ausência de solavancos, clipping e deslocamento do teclado.
