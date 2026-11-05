# Auditoria de performance

## Evidência e correções

| Área | Evidência, causa e impacto | Ação, risco e validação |
| --- | --- | --- |
| Startup Windows | O corpo de `application` construía serviços, repositórios e dados de demonstração diretamente durante a composição. Recompor esse escopo podia reconstruir o estado em memória. | `remember` agora mantém as instâncias durante a vida da composição. Verificar que o redimensionamento e recomposições preservam um paciente sintético. A mudança tem baixo risco e reduz o trabalho estrutural de uma construção por execução desse corpo para uma por composição retida. |
| Startup Windows e Web | Os dois pontos de entrada instalavam um provider para cada serviço e contêiner que `App` já fornece. | Removido um nível de provider de cada entrada. O mesmo par de instâncias chega a `App`. O ganho observado é a remoção dos dois providers redundantes por entrada. |
| Web e tráfego inicial | O smoke Web carregou um módulo da aplicação de 6.412.294 bytes na base e de 6.414.750 bytes na revisão (+2.456 bytes, cerca de 0,04%). O Skiko ficou em 8.648.883 bytes e `fonolab.js` em 532.425 bytes. São tamanhos sem compressão de transporte. | Chrome headless abriu um canvas Compose sem exceções nas duas versões. Medir transferência comprimida, compilação Wasm e primeira interação antes de mudar chunks ou dependências. |
| Navegação e lifecycle | O shell usa `saveState`, `restoreState` e `launchSingleTop`. Telas usam coleta com lifecycle e alguns ViewModels usam `WhileSubscribed(5000)`. | Verificar quantos produtores permanecem ativos depois de sair e voltar. Testar retenção de formulário, contexto de paciente e restauração em cada alvo. |
| Pacientes e listas | A lista era lazy e usava posição como identidade. Filtragem e reordenação podiam deslocar a identidade de linha. Agenda, histórico, sessões e relatórios já usam listas lazy em vários pontos. | Chave estável por ID na lista de pacientes. Validar inserção, filtro e remoção. Sem contagem de recomposições, o efeito confirmado por inspeção é identidade estável. |
| Consultas | `PatientDao.observeFiltered` contém `LIKE '%termo%'` e ordenação por nome. `HistoryManager` combina e ordena eventos de múltiplas fontes. O caminho ativo da UI usa memória. | Medir volumes sintéticos e usar `EXPLAIN QUERY PLAN` no Room antes de propor índices ou paginação. Nenhuma evidência de gargalo SQLite no startup atual. |
| Formulários e exportação | Formulários coletam estado. Anamnese, detalhes e relatórios chamam operações de arquivo, e o adaptador Windows chama `FileWriter` sincronicamente. | Medir digitação e exportação separadamente. Para mover I/O ao dispatcher Windows, testar callback, cancelamento e falha. Preservar ativação do usuário exigida por APIs Web. |
| Jogos | As engines geram estímulos e o ViewModel grava tentativas em `viewModelScope`. O Web chama `speechSynthesis` e possui método de cancelamento. O Windows usa beep. | Medir resposta ao clique e síntese, inspecionar áudio após sair e verificar tentativas pendentes com navegação e falha injetada. Manter critérios clínicos e pontuação. |
| Memória | Repositórios em memória crescem com os registros. Há coletores de tela, estado salvo e coleções de recursos Compose. Não foi capturada retenção em heap. | Comparar heap antes e depois de 20 ciclos de abertura, jogo e retorno. Verificar objetos retidos, coletores, imagens, áudio e URLs de download. |

## Protocolo de medição

1. Use a base `5c89305` e a revisão em cópias separadas com versões idênticas de JDK, SDK, navegador e dispositivo. Prepare o mesmo conjunto sintético, com 100, 1.000 e 10.000 registros nos cenários de lista. Separe processos frios e quentes.
2. **Android**. Use System Trace e contagens de recomposição no Android Studio. Capture da inicialização até a primeira interação, abertura de paciente, digitação e salvamento. Para números repetíveis de startup e frame, prepare Macrobenchmark em dispositivo físico com build comparável a release.
3. **Windows**. Após resolver o runtime, use JFR e o monitor de threads para medir inicialização, alocações, eventos da UI e exportação. Redimensione a janela e confirme que uma edição sintética sobrevive. Capture um heap após os ciclos de navegação.
4. **Web**. Sirva `dist/web` localmente. Use Performance, Network e Memory no DevTools. Registre bytes transferidos com e sem cache, tempo de carregamento e compilação Wasm, long tasks, recarga e saída do jogo. Mantenha condições de rede e CPU constantes.
5. Em cada alvo, repita 10 partidas do mesmo jogo auditivo e 10 do mesmo jogo visual. Registre frames longos, tempo de toque até feedback, início e fim do áudio, contagem de tentativas e limpeza ao sair.
6. Compare pelo menos 20 repetições de cada cenário de latência e reporte mediana e p95 com dispositivo, versão, volume, cache e ferramenta. Salve traces sem dados clínicos. Antes de propor uma otimização, identifique o trabalho caro no trace e valide o mesmo cenário depois da alteração.

Foram medidos tamanhos de build Web e verificados pontos de trabalho redundante por inspeção. A revisão não coletou métricas comparáveis de startup, tempo de frame, recomposição, I/O ou heap por plataforma.

Referências para o protocolo. [Performance em Compose](https://developer.android.com/develop/ui/compose/performance), [Macrobenchmark](https://developer.android.com/topic/performance/benchmarking/macrobenchmark-overview) e [Chrome DevTools Performance](https://developer.chrome.com/docs/devtools/performance/).
