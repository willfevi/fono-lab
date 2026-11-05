# Catálogo de Tarefas Gradle — FonoLab Multiplatform

Este documento descreve todas as tarefas customizadas de distribuição e verificação configuradas no arquivo raiz `build.gradle.kts`.

---

## 1. Tarefas do Grupo `distribution`

| Tarefa | Dependências Principais | Destino do Artefato | Descrição |
|---|---|---|---|
| `buildAndroidDebugApk` | `:app:assembleDebug` | `dist/android/debug/fonolab-debug.apk` | Compila o APK de desenvolvimento com símbolos de depuração. |
| `buildAndroidReleaseApk` | `:app:assembleRelease` | `dist/android/release/fonolab-release.apk` | Compila o APK final otimizado via R8. |
| `buildWindowsExe` | `:desktopApp:packageExe` | `dist/windows/exe/fonolab-installer.exe` | Gera o instalador autônomo com JRE integrado para Windows. |
| `buildWindowsMsi` | `:desktopApp:packageMsi` | `dist/windows/msi/fonolab-installer.msi` | Gera o pacote do instalador Windows MSI. |
| `buildWindowsDistribution` | `buildWindowsExe`, `buildWindowsMsi` | `dist/windows/` | Gera a suite de instaladores Windows (.exe e .msi). |
| `buildAllDistributions` | `buildAndroidDebugApk`, `buildAndroidReleaseApk`, `buildWindowsDistribution` | `dist/` | Constrói todos os instaladores Android e Desktop disponíveis. |

---

## 2. Tarefas do Grupo `verification`

| Tarefa | Dependências | Descrição |
|---|---|---|
| `verifyAndroidBuild` | `:core:domain:compileAndroidMain`, `:shared:compileAndroidMain`, `:app:compileDebugSources` | Valida os alvos Android KMP e o aplicativo sem empacotar o APK. |
| `verifyDesktopBuild` | `:shared:compileKotlinDesktop`, `:desktopApp:compileKotlin` | Valida a compilação do código Desktop sem gerar instaladores. |
| `verifyWebBuild` | `:shared:compileKotlinWasmJs`, `:webApp:compileKotlinWasmJs` | Valida a compilação Web/Wasm. |
| `verifySharedTests` | `:core:domain:testAndroidHostTest` | Executa os testes do domínio KMP configurados para o host Android. |
| `verifyAllPlatforms` | `verifyAndroidBuild`, `verifyDesktopBuild`, `verifyWebBuild`, `verifySharedTests` | Compila os três alvos e roda a verificação de testes no host Android. |

Execute `:app:testDebugUnitTest` para os testes Android do aplicativo e `:core:domain:desktopTest` para a suíte Desktop.

Se o empacotamento Windows em modo offline falhar em `:downloadWix` por acesso a `Task.project`, execute a distribuição com `--no-configuration-cache`. A tarefa pertence ao plugin Compose Desktop.

---

## 3. Diretório de Distribuição (`dist/`)

Todos os instaladores finais são unificados na pasta `dist/` na raiz do projeto:

```text
dist/
├── android/
│   ├── debug/
│   │   └── fonolab-debug.apk
│   └── release/
│       └── fonolab-release.apk
└── windows/
    ├── exe/
    │   └── fonolab-installer.exe
    └── msi/
        └── fonolab-installer.msi
```

> **Nota:** A pasta `dist/` está devidamente listada no `.gitignore` para evitar o commit de binários pesados no repositório Git.
