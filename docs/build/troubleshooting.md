# Resolução de Problemas — Troubleshooting de Build

## 1. Conflito entre AGP 9.0+ e Kotlin Multiplatform Plugin

- **Sintoma**:
  ```
  The 'com.android.library' plugin is not compatible with 'org.jetbrains.kotlin.multiplatform' plugin since AGP 9.0.
  ```
- **Causa**:
  O Android Gradle Plugin 9.x introduziu novas convenções para projetos KMP (`com.android.kotlin.multiplatform.library`). Quando o projeto utiliza o plugin padrão `com.android.library` integrado ao Kotlin Multiplatform, o AGP bloqueia por padrão.
- **Solução Aplicada**:
  Em `gradle.properties`, as seguintes chaves foram definidas:
  ```properties
  android.builtInKotlin=false
  android.newDsl=false
  android.sync.suppressAgpWarnings=UNSUPPORTED_PROJECT_OPTION_USE,DEPRECATED_DSL
  ```

---

## 2. Erros de Serialização no Configuration Cache do Gradle

- **Sintoma**:
  ```
  Configuration cache problems found in this build.
  Task :buildAllDistributions: cannot serialize Gradle script object references.
  ```
- **Causa**:
  Utilização de referências diretas ao script `Project` (como `layout.projectDirectory`) dentro de blocos de execução tardia `doLast { ... }`.
- **Solução Aplicada**:
  As tarefas foram registradas utilizando `tasks.register<Copy>("...")` e acessando `this.destinationDir` nativo da tarefa `Copy`, evitando a captura indevida da instância do projeto.

---

## 3. Empacotamento de Runtime Desktop Windows (WiX Toolset)

- **Sintoma**:
  Falha ao executar `:desktopApp:packageMsi` informando ausência de `light.exe` ou `candle.exe`.
- **Solução**:
  O plugin `compose.desktop` faz o download e extração automática do `wix311-binaries.zip`. Caso o firewall bloqueie o download, instale o WiX Toolset manualmente ou configure o caminho no `PATH` do sistema operacional.

---

## 4. Teclado Ocultando Campos em Formulários Android

- **Sintoma**:
  Ao focar em campos de texto inferiores em telas de cadastro, o teclado virtual cobre os campos e botões.
- **Solução**:
  Aplicação do `imePadding()` no container do formulário com `rememberScrollState()` e `navigationBarsPadding()`, permitindo rolagem fluida e visibilidade integral do formulário ativo.
