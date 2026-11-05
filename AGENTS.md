# FonoLab

Aplicativo local de gestão fonoaudiológica e jogos terapêuticos em Kotlin Multiplatform e Compose Multiplatform. Alvos obrigatórios: Android, Windows Desktop JVM e Web Wasm. Consulte [a auditoria](docs/engineering/PROJECT_AUDIT.md) para a matriz de recursos e lacunas por plataforma.

## Limites do código

- Use `:core:domain` para regras, modelos e contratos compartilhados, `:shared` para Compose, navegação e estado de tela, e `:core:data` para Room no Android. Preserve os pontos de entrada e as implementações de plataforma em `:app`, `:desktopApp` e `:webApp`.
- Prefira contratos, componentes e tokens já existentes. Mantenha UI responsiva, estados de erro claros, semântica acessível, foco previsível e navegação por teclado. Preserve a identidade visual e os jogos.
- Preserve cancelamento de coroutines e vincule coletores ao ciclo de vida apropriado. Meça o cenário afetado antes de otimizar composição, consultas, I/O ou alocação. Registre as condições e os resultados.
- Nesta revisão, mantenha a operação local. Planeje integrações futuras nos limites existentes, sem acrescentar backend, serviço remoto ou autenticação simulada.

## Dados clínicos

- Use somente dados sintéticos em testes, logs, capturas e profiling. Evite registrar identificadores, conteúdo clínico, caminhos de exportação ou segredos.
- Preserve dados, formatos existentes e critérios clínicos. Separe pontuação do jogo de avaliação clínica. Documente diferenças de armazenamento e de exportação entre plataformas.
- Para mudanças de schema ou persistência, teste abertura de versões anteriores, rollback e restauração de backup antes da adoção. Planeje previamente qualquer operação destrutiva ou alteração de critério clínico.

## Verificação

Use o wrapper e o catálogo atuais. Valide a linha de base antes de alterações em Gradle. Para concluir, use os comandos em [verificação de engenharia](docs/engineering/PROJECT_AUDIT.md#verificação). Declare compilação, testes, lint e distribuição separadamente por alvo. Registre casos não executados, falhas preexistentes e a evidência dos artefatos gerados.

## Skills do repositório

As skills ficam em `.agents/skills`. Carregue as que correspondem à tarefa.

| Tarefa | Skills |
| --- | --- |
| Fronteiras, estado, persistência e source sets | `kotlin-project-architecture-review` |
| UI, acessibilidade, adaptação e efeitos Compose | `kotlin-ui-compose-multiplatform` |
| Testes, concorrência e regressões por alvo | `kotlin-testing-kmp` |
| Fronteiras de confiança e dados clínicos | `security-threat-model` |
| Gradle, catálogo e empacotamento | `kotlin-build-kmp-gradle-governance` |

Use [o protocolo de performance](docs/engineering/PERFORMANCE_AUDIT.md) para profiling por alvo. Consulte a [origem das skills](docs/engineering/PROJECT_AUDIT.md#skills) antes de atualizá-las. Execute scripts de terceiros apenas com aprovação explícita.
