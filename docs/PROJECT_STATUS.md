# FonoLab — Plataforma de Gestão Fonoaudiológica e Terapia Gamificada

## 1. Visão Geral da Arquitetura

O **FonoLab** é uma plataforma clínica modular projetada com **Clean Architecture**, **Domain-Driven Design (DDD)** e **Camada de Managers Especializados**, preparada para expansão multiplataforma via **Kotlin Multiplatform (KMP)** e **Compose Multiplatform (CMP)**.

### Módulos Principais
- **`:core:domain`**: Camada pura Kotlin (`java-library`) contendo modelos de domínio, validações clínicas, motores de regras, `DateTimeManager` brasileiro (`DD/MM/AAAA`) e os **36 Managers de Domínio** desacoplados de plataforma.
- **`:core:data`**: Implementação de persistência offline-first via Room (`FonoLabDatabase` v2 com 23 entidades relacionais), DataStore Preferences, repositórios reativos (`Flow`) e sementes clínicas (`DatabaseSeeder`).
- **`:core:designsystem`**: Tokens visuais clínicos (*Modern Healthcare Lavender & Mint*), tipografia, cartões adaptativos, barras de progresso e indicadores de status.
- **`:app`**: Aplicação principal com Jetpack Compose / Material 3, navegação adaptativa (`NavigationBar` em smartphones e `NavigationRail` em tablets/desktops), exportadores nativos de PDF multipágina (`ReportExporter` e `AnamnesisPdfExporter`), síntese de voz (`TextToSpeech`) e **10 jogos terapêuticos interativos**.

---

## 2. Matriz de Compatibilidade Multiplataforma (KMP / CMP)

| Componente / Camada | Android | iOS | Desktop (Windows/macOS/Linux) | Web (Wasm/JS) | Estratégia de Abstração |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Modelos & Regras (`:core:domain`)** | Total | Pronto | Pronto | Pronto | Kotlin puro + `PlatformClock` |
| **Camada de 36 Managers** | Total | Pronto | Pronto | Pronto | Interfaces reativas (`Flow`) sem dependências de UI |
| **`DateTimeManager` (`DD/MM/AAAA`)** | Total | Pronto | Pronto | Pronto | Formatação e validação centralizada |
| **Persistência (`:core:data`)** | Room SQLite | Room KMP / SQLDelight | Room KMP / SQLite JVM | IndexedDB / SQL.js | Interfaces `Repository` em `:core:domain` |
| **Design System & Telas Compose** | Jetpack Compose | Compose Multiplatform | Compose Multiplatform | Compose Multiplatform | Layout adaptativo (`BoxWithConstraints`) |
| **Exportação de PDFs Clínicos** | `android.graphics.pdf` | PDFKit Adapter | Apache PDFBox / Print API | Canvas / Print CSS | Isolado em `core/pdf/` |
| **Áudio & Síntese de Voz (TTS)** | Android TTS + AudioRecord | AVSpeechSynthesizer | Desktop Audio API | Web Speech API | Isolado em `core/audio/AudioCueController` |

---

## 3. Camada de Managers Implementados (`:core:domain`)

Todos os 36 Managers seguem o princípio de responsabilidade única:

1. **`PatientManager`**: Cadastro completo, edição, busca, arquivamento e cálculo automático de idade detalhada.
2. **`FormManager`**: Validação de campos obrigatórios, máscaras brasileiras e estados de formulário.
3. **`AnamnesisManager`**: Gestão da Anamnese Clínica completa de 12 seções, salvamento parcial, conclusão e versionamento histórico.
4. **`AssessmentManager`**: Registro de avaliações fonoaudiológicas estruturadas, escores padronizados e reavaliações.
5. **`PhonologyManager`**: Mapa fonético-fonológico do Português Brasileiro, inventário por posição na palavra e análise de processos fonológicos.
6. **`TherapyManager`**: Planos terapêuticos, especialidades, frequência recomendada e critérios de alta.
7. **`GeneralizationManager`**: Sondagens de generalização por contexto, ambiente, palavra não treinada e interlocutor.
8. **`SessionManager`**: Ciclo de vida completo de sessões (`DRAFT`, `PLANNED`, `IN_PROGRESS`, `PAUSED`, `COMPLETED`, `CANCELLED`).
9. **`SessionPlanningAssistantManager`**: Sugestão inteligente de jogos, estímulos e objetivos com base no histórico do paciente.
10. **`GameManager`**: Catálogo dos 10 jogos terapêuticos, recomendações por área clínica e telemetria de resposta.
11. **`ClinicalDataManager`**: Consolidação de métricas clínicas de acurácia, tentativas, tempo de resposta e nível de ajuda.
12. **`ContentLibraryManager`**: Banco de estímulos terapêuticos (fonemas, sílabas, pares mínimos, vocabulário e frases).
13. **`HistoryManager`**: Linha do tempo longitudinal unificada do prontuário do paciente.
14. **`ScheduleManager`**: Agenda clínica, detecção de conflitos de horário, faltas e remarcações.
15. **`ReferralManager`**: Triagem, fila de espera priorizada e encaminhamentos interdisciplinares.
16. **`HomePracticeManager`**: Prescrição de atividades domiciliares e registro de adesão familiar.
17. **`CaregiverManager`**: Orientações para pais, cuidadores e escola em linguagem acessível.
18. **`DocumentManager`**: Geração de evolução SOAP, pareceres, declarações, laudos e relatórios escolares.
19. **`TemplateManager`**: Modelos reutilizáveis de anamnese, sessões e documentos clínicos.
20. **`DocumentationAssistantManager`**: Geração assistida de rascunhos de evolução SOAP a partir dos dados objetivos da sessão.
21. **`ReportManager`**: Consolidação de indicadores de evolução e comparativos entre períodos.
22. **`BillingManager`**: Controle financeiro de sessões em Reais (`R$`), pacotes, convênios e recibos.
23. **`QualityControlManager`**: Central de pendências clínicas e auditoria de prontuários incompletos.
24. **`AccountManager`**: Perfil profissional do fonoaudiólogo (nome, CRFa, especialidades, clínica).
25. **`AuthManager`**: Estado de sessão local e preparação de autenticação futura (tela de login visual não bloqueante).
26. **`AccessibilityManager`**: Preferências sensoriais, redução de estímulos e ritmo adaptado.
27. **`LanguageProfileManager`**: Contexto linguístico, bilinguismo e exposição escolar/familiar.
28. **`CollaborationManager`**: Comunicação multiprofissional e compartilhamento escolar/familiar.
29. **`SupervisionManager`**: Revisão de casos clínicos para clínicas-escola e supervisores.
30. **`TeamManager`**: Preparação multi-profissional por clínica/unidade.
31. **`MediaManager`**: Gestão de anexos e amostras clínicas do prontuário.
32. **`EngagementManager`**: Recompensas lúdicas separadas das métricas clínicas reais.
33. **`NotificationManager`**: Alertas de reavaliação, pendências e retornos.
34. **`SyncManager`**: Controle de estado offline-first e fila de sincronização futura.
35. **`SettingsManager`**: Preferências globais do sistema, áudio e relatórios.
36. **`AuditManager`**: Trilha de auditoria local de criações, edições e exportações.

---

## 4. Biblioteca de 10 Jogos Terapêuticos Jogáveis

1. **Caça aos Sons (`SOUND_HUNT`)**: Discriminação fonêmica e identificação de fonema inicial.
2. **Monstro Comilão (`HUNGRY_MONSTER`)**: Sustentação vocal, sopro e coordenação pneumofonoarticulatória.
3. **Fábrica de Palavras (`WORD_FACTORY`)**: Consciência silábica e síntese fonológica.
4. **Sequência de Histórias (`STORY_SEQUENCE`)**: Narrativa lógica, temporalidade e pragmática.
5. **Missão Espacial (`SPACE_MISSION`)**: Atenção auditiva seletiva e memória operacional.
6. **Detetive das Rimas (`RHYME_DETECTIVE`)**: Consciência fonológica de rimas e aliterações.
7. **Boca Mágica (`MAGIC_MOUTH`)**: Motricidade orofacial e sequência de praxias articulatórias.
8. **Mercado das Palavras (`WORD_MARKET`)**: Categorização semântica e expansão lexical.
9. **Detetive das Frases (`SENTENCE_DETECTIVE`)**: Estruturação morfossintática e ordenação frasal.
10. **Memória Sonora (`SOUND_MEMORY`)**: Memória auditiva sequencial de estímulos verbais.

---

## 5. Status das Etapas Concluídas

- [x] **Fase 1**: Auditoria do projeto e implementação do `DateTimeManager` brasileiro (`DD/MM/AAAA`) com correção definitiva do campo de data de nascimento.
- [x] **Fase 2**: Modelagem clínica expandida, banco Room v2 (23 tabelas) e camada completa de 36 Managers.
- [x] **Fase 3**: Cadastro completo de pacientes, Anamnese de 12 seções com versionamento e exportação de PDF A4 multipágina.
- [x] **Fase 4**: Prontuário Longitudinal em 6 abas (Resumo/Anamnese, Fonologia/Avaliações, Terapias/Generalização, Sessões Planejadas/Execução, Casa/Documentos SOAP, Linha do Tempo).
- [x] **Fase 5**: Separação estrita entre Sessão Planejada (`PLANNED`) e Sessão em Andamento (`IN_PROGRESS`), com assistente de planejamento e pausa/retomada.
- [x] **Fase 6**: Expansão para 10 jogos terapêuticos com seletor de pistas clínicas (`INDEPENDENT`, `VISUAL`, `AUDITORY`, `TACTILE`, `MODELING`) e separação entre pontuação lúdica e acurácia clínica.
- [x] **Fase 7**: Agenda Clínica, Fila de Espera/Encaminhamentos, Banco de Estímulos e Controle Financeiro em `R$`.
- [x] **Fase 8**: Central de Pendências Clínicas no Dashboard, Perfil Profissional CRFa, Trilha de Auditoria e Tela Visual de Login (acessível via Configurações sem bloquear o uso).
- [x] **Fase 9**: Layout adaptativo (`NavigationBar` + `NavigationRail`) para Smartphones, Tablets e Desktops.
- [x] **Fase 10**: Testes unitários automatizados do `DateTimeManager`, validadores e dos 10 motores de jogos.
