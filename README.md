# FonoLab — Plataforma de Gestão Fonoaudiológica & Terapia Gamificada (KMP & CMP)

O **FonoLab** é uma plataforma clínica completa voltada para fonoaudiólogos no controle integral de pacientes, anamneses estruturadas, planos terapêuticos, sessões clínicas, relatórios gerenciais e reabilitação lúdica com 10 minijogos gamificados estruturados.

A plataforma foi desenvolvida utilizando **Kotlin Multiplatform (KMP)** e **Compose Multiplatform (CMP)**, compartilhando 100% da identidade visual, regras de negócio clínicas e componentes entre **Android (Mobile)** e **Windows (Desktop)** em uma base de código unificada.

---

## 🚀 Arquitetura Multiplataforma

O projeto segue Clean Architecture e Domain-Driven Design (DDD) modularizado:

```text
FonoLab/
├── :shared            # Módulo KMP com Compose Multiplatform (UI, telas, Design System, jogos)
├── :desktopApp        # Aplicação Desktop Windows nativa (.exe e .msi com jpackage)
├── :app               # Aplicação Android (Material 3, Hilt, Room Database local)
├── :core:domain       # Regras de negócio puras, modelos de domínio, casos de uso e gerenciadores clínicos
└── :core:data         # Implementação de persistência local Room SQLite para Android
```

---

## 📦 Automação de Build e Distribuição

O projeto conta com tarefas Gradle integradas para compilação, validação contínua e geração automática de instaladores unificados no diretório `dist/`:

### 1. Geração de Pacotes de Distribuição

| Comando Gradle | Descrição | Saída em `dist/` |
|---|---|---|
| `.\gradlew.bat buildAndroidDebugApk` | Gera o APK Android de depuração | `dist/android/debug/fonolab-debug.apk` |
| `.\gradlew.bat buildAndroidReleaseApk` | Gera o APK Android de produção otimizado com R8 | `dist/android/release/fonolab-release.apk` |
| `.\gradlew.bat buildWindowsExe` | Gera o instalador autônomo `.exe` do Windows com JRE embutido | `dist/windows/exe/fonolab-installer.exe` |
| `.\gradlew.bat buildWindowsMsi` | Gera o pacote de instalação padrão Windows Installer `.msi` | `dist/windows/msi/fonolab-installer.msi` |
| `.\gradlew.bat buildWindowsDistribution` | Gera a suite completa Windows (`.exe` e `.msi`) | `dist/windows/` |
| `.\gradlew.bat buildAllDistributions` | Constrói todos os artefatos suportados no ambiente (Android e Windows) | `dist/` |

### 2. Verificação de Integridade e Testes

| Comando Gradle | O que valida |
|---|---|
| `.\gradlew.bat verifyAndroidBuild` | Compilação de todos os alvos Android (`:app` e `:shared`) |
| `.\gradlew.bat verifyDesktopBuild` | Compilação dos alvos Desktop Windows (`:desktopApp` e `:shared`) |
| `.\gradlew.bat verifySharedTests` | Bateria completa de testes unitários clínicos em `:core:domain` |
| `.\gradlew.bat verifyAllPlatforms` | Verificação global de compilação cruzada e testes em uma única passada |

---

## 💻 Como Executar em Desenvolvimento

### Android (Dispositivo ou Emulador)
1. Conecte seu dispositivo via USB com Depuração USB ativada.
2. Execute:
   ```powershell
   .\gradlew.bat installDebug
   ```

### Windows Desktop
Para iniciar o aplicativo desktop em tempo de desenvolvimento sem gerar os instaladores:
```powershell
.\gradlew.bat :desktopApp:run
```

---

## 🎮 Os 10 Jogos Terapêuticos Gamificados

1. **Caça ao Som (Sound Hunt):** Discriminação auditiva e fonologia.
2. **Missão Espacial (Space Mission):** Linguagem receptiva e vocabulário.
3. **História Embaralhada (Story Sequence):** Sequenciamento temporal e narrativa.
4. **Monstrinho Faminto (Hungry Monster):** Nomeação rápida e fala articulatória com avaliação pelo clínico.
5. **Fábrica de Palavras (Word Factory):** Consciência fonológica, rimas e aliteração.
6. **Ritmo das Palavras (Rhythm & Beats):** Prosódia, acentuação silábica e fluência.
7. **Detetive dos Pares Mínimos (Minimal Pairs):** Oposição fonológica sistemática (/p/ vs /b/, /f/ vs /v/, /t/ vs /d/).
8. **Ginástica da Boca (Oral Motor Gym):** Motricidade orofacial e coordenação práxica fonoarticulatória.
9. **Mundo Semântico (Category Express):** Associação semântica, categorização e raciocínio verbal.
10. **Aventuras da Comunicação (Pragmatic Quest):** Habilidades pragmáticas, teoria da mente e interações sociais.

---

## 🔒 Privacidade e Segurança Clínica

- **100% Offline por Padrão:** Não trafega dados sensíveis de prontuários em servidores não autorizados.
- **Armazenamento Seguro:** Isolamento por paciente e exclusão em cascata completa com auditoria.
- **Tratamento Adequado de Dados:** Anonimização e máscaras em relatórios exportados.

---

## 📚 Documentação Técnica

Para guias aprofundados sobre a arquitetura e processos de compilação, consulte a pasta `docs/`:
- [Arquitetura Frontend KMP & CMP](docs/architecture/frontend-kmp-cmp.md)
- [Arquitetura da Plataforma Android](docs/architecture/platform-android.md)
- [Arquitetura da Plataforma Windows Desktop](docs/architecture/platform-windows.md)
- [Guia de Build Android](docs/build/build-android.md)
- [Guia de Build Windows](docs/build/build-windows.md)
- [Catálogo de Tarefas Gradle](docs/build/gradle-tasks.md)
- [Troubleshooting & Soluções de Erros](docs/build/troubleshooting.md)
