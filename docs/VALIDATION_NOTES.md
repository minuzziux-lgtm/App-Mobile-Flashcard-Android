# MyDeck — verificação antes da publicação oficial

## Estado

Os commits 10 a 38 estão no **repositório privado**, na branch `dev/mydeck-private`. São implementações gravadas, não funcionalidades já homologadas. O item 39 introduz a definição do processo de CI. Só marcar build como aprovado depois do resultado da execução.

## Comandos no Android Studio (Windows)

```powershell
git status --short
git fetch private
git switch dev/mydeck-private
git pull --ff-only private dev/mydeck-private
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

Não sobrescrever alterações locais nem enviar nada ao `origin` público.

## Verificação manual

1. Criar turma e cinco cartões de cada tipo; estudar, responder, pausar, retomar e concluir.
2. Reiniciar o app e verificar a persistência das avaliações.
3. Testar revisões pendentes antes e depois da data; 0, 40 e 100+ cartões.
4. Conferir filtros de desempenho por turma, aluno, deck, tag, mês e ano.
5. Conferir leitura de tela, acessibilidade, contraste, dark/light e telas pequenas.
6. Testar duas contas distintas no mesmo aparelho. O isolamento do Firestore e a sincronização entre dispositivos dependem dos itens 40–54.

## Limites conhecidos

- Dados de estudo ainda são locais, não sincronizados com o Firebase.
- Reversíveis apresentam frente e verso na direção cadastrada; alternância de direção ainda deve ser revista.
- Tradução EN nas telas principais e recursos de autenticação; partes da interface ainda têm português fixo.
- A CI não substitui testes com emulador e dispositivos reais.
- Não ativar a automação de publicação anterior `schedule.csv`: horários vencidos e fila oficial não preparada.

O repositório público deve permanecer inalterado até a conclusão dos requisitos, build validado e decisão posterior sobre o histórico público.
