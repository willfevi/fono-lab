# FonoLab — Gestão Fonoaudiológica & Terapia Gamificada Multiplataforma

O **FonoLab** é uma plataforma clínica completa voltada para fonoaudiólogos no controle integral de pacientes, anamneses estruturadas, planos terapêuticos personalizados, acompanhamento de sessões clínicas, relatórios gerenciais e reabilitação lúdica através de 10 minijogos gamificados estruturados.

A plataforma foi desenvolvida utilizando **Kotlin Multiplatform (KMP)** e **Compose Multiplatform (CMP)**, compartilhando 100% da identidade visual, regras de negócio clínicas e componentes reativos entre **Android (Mobile)**, **Windows (Desktop)** e **Web (WebAssembly / Wasm)** a partir de uma base de código única e unificada.

---

## 🚀 Arquitetura Modular

O projeto segue os princípios de **Clean Architecture** e **Domain-Driven Design (DDD)** com forte separação de responsabilidades em 6 módulos:

```text
FonoLab/
├── :shared            # Módulo KMP Compose Multiplatform (UI, Design System, telas, navegação e jogos)
├── :desktopApp        # Aplicação Desktop Windows nativa (.exe, .msi e versão portátil com JRE embutido)
├── :webApp            # Aplicação Web moderna compilada em Kotlin/Wasm + Skiko
├── :app               # Aplicação Android nativa (Material 3, Activity, Room local)
├── :core:domain       # Regras puras de negócio, modelos de domínio, casos de uso e abstrações multiplataforma
└── :core:data         # Implementação de persistência local Room SQLite para Android
```

---

## 🛠️ Stack Tecnológico

- **Linguagem:** Kotlin 2.3+ (Kotlin Multiplatform)
- **Interface Declarativa:** Compose Multiplatform (Desktop, Android e Web)
- **Injeção de Dependências:** Koin Multiplatform (`koin-core`, `koin-compose`, `koin-compose-viewmodel`)
- **Navegação:** JetBrains Navigation Compose Multiplatform
- **Motor Gráfico:** Skiko com aceleração Direct3D / DirectX (Windows) e WebGL (Web Wasm)
- **Persistência:** Room SQLite (Android) e repositórios locais em memória/arquivo (Desktop e Web)
- **Concorrência e Reatividade:** Kotlinx Coroutines, StateFlow e SharedFlow
- **Datas e Serialização:** Kotlinx DateTime e Kotlinx Serialization JSON

---

## 💻 Como Executar em Desenvolvimento

### 1. Android (Dispositivo Físico ou Emulador)
Conecte o smartphone via cabo USB com a Depuração USB ativada e execute:
```powershell
.\gradlew.bat :app:installDebug
```

### 2. Windows Desktop
Para iniciar o aplicativo desktop em modo de desenvolvimento com hot-reload sem precisar gerar instalador:
```powershell
.\gradlew.bat :desktopApp:run
```

### 3. Web (WebAssembly no Navegador)
Para iniciar o servidor de desenvolvimento local com Webpack:
```powershell
.\gradlew.bat :webApp:wasmJsBrowserDevelopmentRun
```
O app estará acessível em `http://localhost:8080`.

---

## 📦 Automação de Build e Distribuição

O projeto possui tarefas automatizadas no Gradle para compilar, empacotar e disponibilizar as distribuições organizadas na pasta `dist/`:

### Geração de Pacotes

| Comando Gradle | Descrição | Destino em `dist/` |
|---|---|---|
| `.\gradlew.bat buildAndroidDebugApk` | Gera o APK Android de depuração | `dist/android/debug/fonolab-debug.apk` |
| `.\gradlew.bat buildAndroidReleaseApk` | Gera o APK Android de produção otimizado com R8 | `dist/android/release/fonolab-release.apk` |
| `.\gradlew.bat buildWindowsPortable` | Gera a versão Windows portátil (executável direto sem instalação) | `dist/windows/portable/FonoLab/` |
| `.\gradlew.bat buildWindowsExe` | Gera o instalador assistido `.exe` para Windows com JRE embutido | `dist/windows/exe/fonolab-installer.exe` |
| `.\gradlew.bat buildWindowsMsi` | Gera o pacote padrão corporativo Windows Installer `.msi` | `dist/windows/msi/fonolab-installer.msi` |
| `.\gradlew.bat buildWindowsDistribution` | Constrói a suíte Windows completa (Portátil, `.exe` e `.msi`) | `dist/windows/` |
| `.\gradlew.bat buildWebDistribution` | Compila o bundle web de produção (Wasm + JS + HTML) | `dist/web/` |
| `.\gradlew.bat buildAllDistributions` | Constrói todos os artefatos de todas as plataformas suportadas | `dist/` |

### Verificação de Integridade e Testes

| Comando Gradle | O que valida |
|---|---|
| `.\gradlew.bat verifyAndroidBuild` | Valida compilação dos módulos Android (`:app`, `:shared`, `:core:domain`) |
| `.\gradlew.bat verifyDesktopBuild` | Valida compilação dos módulos Desktop (`:desktopApp` e `:shared`) |
| `.\gradlew.bat verifyWebBuild` | Valida compilação dos módulos Web Wasm (`:webApp` e `:shared`) |
| `.\gradlew.bat verifySharedTests` | Executa os testes do domínio KMP no host Android |
| `.\gradlew.bat :core:domain:desktopTest` | Executa a suíte de testes de domínio no Desktop |
| `.\gradlew.bat verifyAllPlatforms` | Executa compilação cruzada e testes de todas as plataformas |

---

## 🖥️ Requisitos de Sistema

- **Android:** Android 8.0 (API 26) ou superior em processadores ARM64 / x86_64.
- **Windows Desktop:** Windows 10 (64 bits, versão 1809+) ou Windows 11 (64 bits). Não requer instalação prévia do Java no computador do usuário (runtime embutido).
- **Web:** Navegadores modernos com suporte a WebAssembly (Google Chrome, Microsoft Edge, Mozilla Firefox e Safari).

---

## 🎮 Os 10 Jogos Terapêuticos Gamificados

1. **Caça ao Som (Sound Hunt):** Discriminação auditiva, atenção sustentada e fonologia.
2. **Missão Espacial (Space Mission):** Linguagem receptiva, conceitos espaciais e vocabulário.
3. **História Embaralhada (Story Sequence):** Sequenciamento temporal, coerência e narrativa verbal.
4. **Monstrinho Faminto (Hungry Monster):** Nomeação rápida e fala articulatória com pontuação clínica.
5. **Fábrica de Palavras (Word Factory):** Consciência fonológica, rimas e aliteração silábica.
6. **Ritmo das Palavras (Rhythm & Beats):** Prosódia, acentuação silábica e fluência da fala.
7. **Detetive dos Pares Mínimos (Minimal Pairs):** Oposição fonológica sistemática (/p/ vs /b/, /f/ vs /v/, /t/ vs /d/).
8. **Ginástica da Boca (Oral Motor Gym):** Motricidade orofacial e coordenação práxica fonoarticulatória.
9. **Mundo Semântico (Category Express):** Associação semântica, categorização e raciocínio verbal.
10. **Aventuras da Comunicação (Pragmatic Quest):** Habilidades pragmáticas, teoria da mente e interações sociais.

---

## 🔒 Privacidade e Segurança Clínica

- **100% Offline por Padrão:** Não trafega prontuários ou registros de voz em servidores externos não autorizados.
- **Armazenamento Seguro:** Isolamento estrito por paciente e exclusão em cascata completa.
- **Anonimização Clínica:** Relatórios e avaliações gerados preservam dados identificáveis conforme boas práticas éticas.
