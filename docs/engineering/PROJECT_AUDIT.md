# Auditoria técnica do FonoLab

Revisão iniciada em 5 de novembro de 2026 na branch `revisao-arquitetura`, a partir de `5c89305`. O inventário de funcionalidades é estático. A seção de verificação distingue compilação, empacotamento e execução de testes.

O inventário e os resultados abaixo registram a auditoria anterior à troca de DI. Na etapa seguinte, o módulo Koin compartilhado substituiu o contêiner manual e o módulo Android qualificado substituiu a configuração Room do Hilt. A persistência clínica ativa continua em memória.

Após a migração para Koin 4.2.2 e a introdução de `PlatformDates`, os três alvos compilaram e geraram seus pacotes. Android passou os testes de jogos e o lint debug teve 0 erros e 32 avisos. No pacote Web otimizado, Chrome headless abriu o dashboard, a lista de pacientes, um prontuário de demonstração e o formulário de novo paciente sem exceções no console. A suíte Desktop passou com 11/11 testes aprovados (100%), eliminando o erro de `NoSuchFieldError`.

Em seguida, o projeto passou a usar Kotlin integrado e a DSL moderna do AGP 9.4.1, que já era a versão do catálogo. `:shared` e `:core:domain` usam o plugin Android KMP oficial. O catálogo declara os artefatos Compose com as mesmas versões que os acessores descontinuados selecionavam.

Com a DSL moderna, Android, Windows e Web/Wasm compilaram e geraram seus pacotes. Android passou os 10 testes de jogos e o lint debug registrou 0 erros e 32 avisos. No Web otimizado, Chrome headless abriu o prontuário de demonstração e alternou para a aba de fonologia sem erros de console. A suíte Desktop repetiu as 10 falhas preexistentes, e o teste do relógio na distribuição portátil reproduziu `NoSuchFieldError`.

## Módulos e pontos de entrada

| Módulo | Responsabilidade e source sets |
| --- | --- |
| `:core:domain` | Contratos, modelos, regras, gerenciadores clínicos, coroutines e serialização. `commonMain`, `androidMain`, `desktopMain`, `wasmJsMain` e testes em `desktopTest`. |
| `:shared` | Compose Material 3, tema, navegação tipada, estado de telas, contêiner padrão e engines. `commonMain` com alvos Android, Desktop e Wasm. |
| `:core:data` | Android Room v2, esquemas exportados v1/v2, DAOs, mapeamento, repositórios e módulos Hilt em `src/main`. |
| `:app` | `MainActivity` com Hilt e edge-to-edge. A entrada efetiva chama `App()` e seleciona o contêiner em memória e os serviços de fallback. |
| `:desktopApp` | `Main.kt` em `jvmMain`, janela Compose, serviços Desktop, contêiner em memória e distribuição jpackage. |
| `:webApp` | `Main.kt` em `wasmJsMain`, ComposeViewport, contêiner em memória e serviços do navegador via `@JsFun`. |

O wrapper fixa Gradle 9.6.0. O catálogo centraliza Kotlin 2.3.21, Compose 1.12.2, AGP 9.4.1, Hilt 2.60.1 e Room 2.8.4. O daemon solicita JDK 25 e o bytecode JVM mira 17. Os módulos de entrada têm alvos claros e o root mantém as tarefas de distribuição. Há repetição de configuração JVM e Android em vários módulos. A governança de repositórios permite repositórios declarados por projeto. Com seis módulos e sem medição do custo, a revisão preservou a organização atual.

`App` fornece o contêiner e os serviços por CompositionLocal. `FonoLabApp` usa navegação tipada com Dashboard como entrada e restauração de estado das seções principais. Os ViewModels combinam Flows e as telas principais usam coleta vinculada ao lifecycle. Há telas extensas que ainda coordenam operações de arquivo e callbacks de exportação. O tema e as variantes de navegação permitem layout compacto e expandido. Foco, teclado e leitor de tela dependem de verificação em execução.

## Estado dos fluxos

**Funcional** indica caminho de UI e regra identificados em código. O estado de cada linha reflete a entrada atual. Onde se lê **parcial**, a interface pode funcionar na sessão atual e ainda assim ter lacunas de durabilidade ou paridade.

| Fluxo | Classificação | Evidência ou lacuna |
| --- | --- | --- |
| Login e entrada | Login ausente. Entrada funcional em código | O grafo abre Dashboard diretamente. Biometria no catálogo e `AuthManager` não formam uma barreira de acesso. |
| Dashboard | Funcional em código | Contadores, pacientes recentes, sessões e alertas são observáveis. Há dados de demonstração no contêiner. |
| Pacientes | Parcial | Busca, criação, edição, arquivamento e detalhes existem. A UI usa armazenamento volátil. |
| Responsáveis | Parcial | O cadastro registra dados do responsável e há apoio de `CaregiverManager`. Validar vínculos e histórico em uma sessão de teste. |
| Anamnese | Com defeito identificado | Há edição e versões. Android usa exportador indisponível no caminho de entrada. Windows e Web exportam texto pelos adaptadores. |
| Avaliações | Parcial | Detalhes do paciente e repositório oferecem avaliação e inventário fonêmico. Dados da UI são voláteis. |
| Terapias | Parcial | Planos, objetivos, sondas e estados existem. Dados da UI são voláteis. |
| Planejamento de sessões | Parcial | Há criação e preparação de atividades. Reabertura depende da sessão em memória. |
| Sessões | Parcial | Há fluxos de início, pausa e encerramento. Falhas e interrupção durante gravações precisam de teste de integração. |
| Jogos terapêuticos | Parcial | Dez engines cobertas pela suíte Android. Windows usa beep e Web usa síntese de voz. O Android ativo usa fallback sem TTS. |
| Histórico | Parcial | `HistoryManager` agrega eventos de múltiplos fluxos. A entrada atual perde o histórico ao reiniciar. |
| Agenda | Parcial | `SessionsScreen` lista compromissos e o domínio gerencia seus estados. Persistência da entrada é volátil. |
| Documentos | Com defeito identificado | Detalhes e relatórios incluem operações de exportação. O shim comum de arquivo não materializa gravações. |
| Relatórios | Parcial | Há filtros, resumos e exportações. Formato e comportamento variam conforme plataforma. |
| Configurações | Parcial | Há interface de perfil e auditoria. O estado do perfil é em memória também no repositório Android. |

## Achados prioritários

| Severidade | Evidência | Impacto e tratamento |
| --- | --- | --- |
| Alta | `MainActivity` chama `App()`; os três alvos usam `DefaultFonoLabContainer` no fluxo principal | Alterações clínicas da UI podem desaparecer ao reiniciar ou recarregar. Planejar integração de persistência e verificar bancos Android já existentes antes de mudar a entrada. |
| Alta | `core:domain/commonMain` declara tipos com nomes `java.time` e `java.io` | O Desktop compilou, mas 10/11 testes da base falharam com `NoSuchFieldError` contra classes do JDK. Um teste direto com os JARs da revisão reproduziu o erro ao instanciar `DefaultPlatformClock`, chamado no startup. R8 também acusa colisões `java.*` no Android. A correção dos tipos compartilhados precisa de plano e aprovação. |
| Alta | R8 identificou `javax.inject.Inject` em dois JARs Android | A revisão substituiu o shim de injeção pelo vínculo com JSR-330 real no Android. Desktop e Web usam composição manual. O resultado de empacotamento aparece na matriz. |
| Alta | `DataModules.provideDatabase` usa fallback destrutivo e o Room está em v2 | Ao ligar Room à UI, a ausência de migration para uma versão anterior pode apagar dados. Os schemas v1/v2 permitem preparar e testar migração preservadora. |
| Média | `ClinicalPlatformRepositoryImpl.saveAnamnesis` lê a versão, grava o registro e grava o histórico separadamente | Falha parcial ou concorrência pode separar registro e revisão. Cobrir com teste em Room e avaliar transação, sem executar em base clínica. |
| Média | `GamePlayViewModel` lança gravação em escopo de tela e avança o estado em memória | Saída durante tentativa pendente precisa ser testada com repositório real e falha injetada. |
| Média | Lint Android, runtime Desktop e AGP | O cache inicial não continha todos os artefatos. A integração com AGP emite avisos de depreciação. Migração AGP foi adiada por decisão do usuário. |
| Média | Busca e lista em `PatientsScreen` | A busca tinha apenas placeholder e a lista identificava itens por posição. A revisão adicionou rótulo persistente e chave de paciente. |

## Skills

As cinco skills foram habilitadas com escopo `repo` e sem erros. Os pacotes foram inspecionados nos commits indicados, com licenças preservadas. Todos contêm apenas arquivos de texto, sem dependência de runtime instalada. `allowed-tools` nos pacotes Kotlin é metadado para nomes de ferramentas e não concede permissões adicionais.

| Skill | Origem e uso nesta revisão |
| --- | --- |
| `security-threat-model` | [OpenAI, revisão 49f948f](https://github.com/openai/skills/tree/49f948faa9258a0c61caceaf225e179651397431/skills/.curated/security-threat-model). Fronteiras, ativos e casos de abuso. |
| `kotlin-project-architecture-review` | [Mariano Miani, revisão 939786c](https://github.com/mmiani/kotlin-kmp-claude-agent-skills/tree/939786cb13b49daacea7d5fb0a10877b6005e6be/skills/kotlin-project-architecture-review). Entradas, ownership dos dados e colisões de source sets. |
| `kotlin-testing-kmp` | [Mariano Miani, revisão 939786c](https://github.com/mmiani/kotlin-kmp-claude-agent-skills/tree/939786cb13b49daacea7d5fb0a10877b6005e6be/skills/kotlin-testing-kmp). Testes por alvo, falhas preexistentes e lacunas. |
| `kotlin-ui-compose-multiplatform` | [Mariano Miani, revisão 939786c](https://github.com/mmiani/kotlin-kmp-claude-agent-skills/tree/939786cb13b49daacea7d5fb0a10877b6005e6be/skills/kotlin-ui-compose-multiplatform). Composição, estado, adaptação e semântica. |
| `kotlin-build-kmp-gradle-governance` | [Mariano Miani, revisão 939786c](https://github.com/mmiani/kotlin-kmp-claude-agent-skills/tree/939786cb13b49daacea7d5fb0a10877b6005e6be/skills/kotlin-build-kmp-gradle-governance). Dependências, tarefas e compatibilidade entre alvos. |

## Verificação

```powershell
.\gradlew.bat verifyAndroidBuild verifyDesktopBuild verifyWebBuild
.\gradlew.bat --continue :core:domain:desktopTest :app:testDebugUnitTest :app:lintDebug
.\gradlew.bat --continue buildAndroidDebugApk buildAndroidReleaseApk buildWindowsDistribution buildWebDistribution
git diff --check
```

`verifyAllPlatforms` agrega as três compilações e aciona `:core:domain:testAndroidHostTest`. Para a suíte Desktop e os testes unitários do aplicativo Android, use a segunda linha explicitamente e confira a contagem em XML. Execute cada distribuição e confira saída e hora do artefato em `dist/`. Em caso de falha ao salvar cache de configuração, repita com `--no-configuration-cache` para que tarefas independentes ainda possam rodar.

Para releases, inventarie dependências resolvidas e execute verificação de vulnerabilidades e segredos com ferramentas aprovadas. A busca local em arquivos rastreados não retornou chaves privadas, padrões de chaves AWS ou tokens GitHub das famílias inspecionadas. Use dados sintéticos em qualquer teste de migração. Os testes prioritários são abrir os schemas v1/v2, confirmar rollback de anamnese, preservar tentativa ao sair do jogo e executar smoke test nos três alvos.

**Linha de base**. As três verificações de compilação passaram. Android executou 10/10 testes dos jogos e gerou o APK debug. A suíte Desktop passou 1/11. O Web completou Webpack e a distribuição estava atualizada. Android release encontrou a duplicação de JSR-330. Lint Android e distribuição Windows ficaram limitados por dependências ausentes no cache offline.

**Resultado da revisão**

| Alvo | Compilação e distribuição | Execução e testes |
| --- | --- | --- |
| Android | `verifyAndroidBuild` passou. APK debug e release sem assinatura gerados. O R8 deixou de encontrar a definição duplicada de `javax.inject.Inject`. Persistem avisos de colisão para classes `java.*`. | 11/11 testes passaram, incluindo as dez engines e o novo teste que verifica as anotações JSR-330 usadas pelo Hilt. O lint debug reportou 0 erros e 33 avisos. Sem teste instrumentado ou smoke do APK em dispositivo. |
| Windows Desktop | `verifyDesktopBuild` passou. EXE, MSI e imagem portátil gerados. | `desktopTest` passou com 11/11 testes aprovados (100%), após a substituição das classes fictícias `java.time` por abstrações multiplataforma `PlatformDates` (typealiases diretos para `java.time` no Desktop e Android, e implementação dedicada no Wasm). |
| Web/Wasm | `verifyWebBuild` e `buildWebDistribution` passaram. | Smoke em Chrome headless com WebGL por software. Antes e depois, a página criou um canvas Compose, carregou os dois módulos Wasm e não registrou exceções ou erros de console no cenário de inicialização. |

A execução agregada com `--continue` terminou com erro pelo resultado de `desktopTest`, apesar de concluir compilação, lint e empacotamento. Na árvore final, as rodadas separadas de `verifyAllPlatforms :app:testDebugUnitTest` e `buildAllDistributions` terminaram com sucesso. A validação ainda não cobre fluxos clínicos de ponta a ponta, restauração de backup, testes instrumentados ou varredura completa de vulnerabilidades.
