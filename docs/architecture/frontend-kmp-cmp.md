# Arquitetura Frontend — Kotlin Multiplatform (KMP) & Compose Multiplatform (CMP)

## 1. Visão Geral

O **FonoLab** adota uma estratégia de código unificado baseada em **Kotlin Multiplatform (KMP)** e **Compose Multiplatform (CMP)**. A camada visual e as regras de negócio clínicas residem no módulo compartilhado `:shared`, permitindo que tanto a aplicação Android (`:app`) quanto a versão Desktop Windows (`:desktopApp`) compartilhem 100% da identidade visual, fluxo de navegação e lógica de domínio.

```
                      ┌────────────────────────────────────────┐
                      │             FonoLab App                │
                      │   (Universal Compose Multiplatform)    │
                      └──────────────────┬─────────────────────┘
                                         │
                 ┌───────────────────────┴───────────────────────┐
                 ▼                                               ▼
     ┌────────────────────────┐                     ┌────────────────────────┐
     │      Android (:app)    │                     │  Desktop (:desktopApp) │
     │  - Activity Android    │                     │  - Window AWT/Compose  │
     │  - Room SQLite local   │                     │  - In-Memory / SQLite  │
     │  - TTS & Audio Android │                     │  - Desktop Platform    │
     └────────────────────────┘                     └────────────────────────┘
```

---

## 2. Estrutura Modular

- **`:shared` (KMP/CMP Library)**:
  - `commonMain`: Contém a raiz da UI compartilhada (`App()`), o Design System completo (`com.healthcare.fonolab.core.designsystem.*`), todas as 11 telas clínicas (`Dashboard`, `Patients`, `PatientForm`, `PatientDetails`, `Anamnesis`, `Sessions`, `SessionHub`, `Reports`, `Settings`, `GameCatalog`, `GamePlay`), os 10 motores gamificados de reabilitação fonoaudiológica, gerenciadores de injeção de dependência (`FonoLabContainer`, `DefaultFonoLabContainer`) e abstrações multiplataforma (`PlatformServices`).
  - `androidTarget`: Compilado para bytecode compatível com Android DEX (AAR).
  - `desktop (jvm)`: Compilado para Java Virtual Machine direcionado a Windows, macOS e Linux.

- **`:core:domain` (Pure Kotlin)**:
  - Modelos de domínio imutáveis (`Patient`, `TherapySession`, `ClinicalGoal`, `AnamnesisRecord`, `GameAttempt`).
  - Contratos de repositórios (`PatientRepository`, `TherapySessionRepository`, `ClinicalGoalRepository`, `ClinicalPlatformRepository`).
  - Casos de uso clínicos validados (`SavePatientUseCase`, `PlanSessionUseCase`, `StartSessionUseCase`, etc.).
  - Gerenciadores clínicos com separação de responsabilidades (`PatientManager`, `SessionManager`, `AnamnesisManager`, `GameManager`, etc.).

- **`:core:data` (Android Persistence)**:
  - Implementação local Room SQLite com DAOs reativos (Flows).
  - Conversão bidirecional entre entidades de banco de dados e objetos de domínio.

- **`:desktopApp` (Windows JVM Entrypoint)**:
  - Função principal `main()` configurando `application { Window(...) { App(...) } }`.
  - Serviços de plataforma nativos para Desktop (`DesktopAudioService`, `DesktopPdfExporter`, `DesktopShareManager`, `DesktopClipboardManager`).

- **`:app` (Android Entrypoint)**:
  - `MainActivity` herdando de `ComponentActivity`, configurando `enableEdgeToEdge()` e renderizando `App()`.

---

## 3. Design System Responsivo e Adaptativo

A interface compartilha a identidade visual clínica e se adapta dinamicamente à largura da viewport:

1. **Compact Layout (< 600 dp - Smartphones Android)**:
   - Navegação inferior flutuante em formato de pílula (`Floating Pill Navigation`).
   - Cards em coluna única com suporte a toque e espaçamento ergonômico.
   - Teclado com ajuste automático de foco e rolagem suave.

2. **Medium / Expanded Layout (>= 600 dp - Tablets e Desktop Windows)**:
   - Barra lateral fixa (`NavigationRail` / Sidebar) para alternância instantânea entre módulos.
   - Telas organizadas em múltiplas colunas (ex.: formulários com dados pessoais ao lado de dados clínicos).
   - Largura mínima de janela no Desktop travada em `800x600` e dimensão padrão recomendada de `1280x820`.

---

## 4. Contratos de Plataforma (`PlatformServices`)

Recursos que dependem do sistema operacional são abstraídos por meio de interfaces:

- `PlatformAudioService`: Sintetizador de voz clínico (TTS) e execução de bips/pistas auditivas.
- `PlatformPdfExporter`: Exportação de relatórios clínicos e prontuários de anamnese.
- `PlatformShareManager`: Compartilhamento de documentos e tarefas para cuidadores/família.
- `PlatformClipboardManager`: Cópia de códigos, resumos e encaminhamentos para a área de transferência.
- `PlatformClock`: Provedor unificado de data e hora com fuso de Brasília (`America/Sao_Paulo`).
