# FonoLab — Plataforma Gamificada de Terapia Fonoaudiológica

O **FonoLab** é um aplicativo Android nativo completo desenvolvido em Kotlin e Jetpack Compose, voltado para fonoaudiólogos no apoio a sessões clínicas terapêuticas por meio de minijogos educativos e estruturados.

---

## 🚀 Como Abrir e Executar o Projeto

1. Abra o **Android Studio** (Koala / Ladybug ou superior).
2. Selecione **Open** e navegue até a pasta raiz do projeto clonado.
3. Aguarde o Gradle sincronizar as dependências automaticamente.
4. Escolha um emulador ou dispositivo físico com **Android 8.0 (API 26) ou superior**.
5. Clique em **Run ▶ (`app`)** ou execute pelo terminal:
   ```powershell
   .\gradlew.bat installDebug
   ```

### APK de Debug Gerado
O binário instalável está disponível em:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🛠️ Tecnologias e Decisões de Arquitetura

* **Linguagem:** Kotlin 2.3.x com Gradle Kotlin DSL e Version Catalog (`gradle/libs.versions.toml`).
* **Interface & Design System:** Jetpack Compose com Material 3, tema adaptativo com paleta lúdico-clínica, suporte a telas pequenas e tablets com `NavigationSuiteScaffold`.
* **Arquitetura:** Clean Architecture e MVVM estruturado em 3 módulos:
  - `:core:domain`: Módulo Kotlin puro (JVM). Contém contratos, regras de pontuação, cálculo de idade, validação de pacientes e motores dos jogos. Não depende de Android, Room ou Compose.
  - `:core:data`: Persistência local com Room (Entidades, DAOs, Migrações), DataStore e repositórios desacoplados.
  - `:app`: Apresentação, telas Jetpack Compose, DI (Hilt), rotas type-safe com Navigation Compose, áudio nativo e exportação de relatórios.
* **Injeção de Dependências:** Hilt 2.60.1 com KSP.
* **Banco de Dados Local:** Room 2.8.x (`fonolab.db`) com integridade referencial, exclusão em cascata e histórico por paciente.
* **Áudio:** TextToSpeech nativo para leitura de instruções em português brasileiro (pt-BR) e SoundPool para efeitos sonoros.
* **Relatórios:** Geração de PDF nativa utilizando `android.graphics.pdf.PdfDocument`, funcionando **100% offline**.
* **minSdk 26 (Android 8.0):** Decisão arquitetural documentada para suporte nativo à API `java.time` (evitando desugaring pesado), APIs estáveis de PDF/TTS e ícones adaptativos.

---

## 🎮 Os Cinco Jogos Terapêuticos Implementados

1. **Caça ao Som (Sound Hunt):**
   - *Área:* Fala e fonologia / Discriminação auditiva.
   - *Mecânica:* Apresentação do som-alvo com instrução em voz/texto e alternativas visuais.
   - *Avaliação:* Registro objetivo de identificação (`AutoScored`).

2. **Missão Espacial (Space Mission):**
   - *Área:* Linguagem receptiva e vocabulário semântico.
   - *Mecânica:* Nave espacial e planetas com identificação e seguimento de comandos instrucionais.
   - *Avaliação:* Registro objetivo (`AutoScored`).

3. **História Embaralhada (Story Sequence):**
   - *Área:* Narrativa, sequenciamento temporal e linguagem expressiva.
   - *Mecânica:* Cenas temporais reordenáveis com controles acessíveis de subir/descer e validação contra ordens aceitáveis.
   - *Avaliação:* Registro de sequenciamento temporal (`AutoScored`).

4. **Monstrinho Faminto (Hungry Monster):**
   - *Área:* Nomeação rápida e produção articulatória de fala.
   - *Mecânica:* Personagem interativo que solicita alimentos/itens para o paciente nomear.
   - *Avaliação:* Registro ágil pelo fonoaudiólogo (Correta independente, Com pista, Incorreta, Sem resposta) — sem falso reconhecimento automático de fala.

5. **Fábrica de Palavras (Word Factory):**
   - *Área:* Consciência fonológica.
   - *Mecânica:* Modos para identificação de rimas, aliteração (som inicial) e segmentação silábica com blocos montáveis.
   - *Avaliação:* Registro objetivo (`AutoScored`).

---

## 🔒 Privacidade e Segurança dos Dados

* **Offline por Padrão:** Não realiza nenhuma chamada externa nem compartilha dados com a nuvem.
* **Sem vazamento em logs:** Identificadores e notas de prontuário não são impressos no logcat.
* **Backup desativado (`android:allowBackup="false"`):** Impede extração acidental de dados clínicos em backups automáticos de contas Google pessoais.
* **Sem gravação de áudio desnecessária:** O app utiliza síntese e não realiza captura involuntária do microfone.

---

## 🧪 Testes Automatizados

Para rodar todos os testes unitários do projeto:
```powershell
.\gradlew.bat test
```
- **Testes Unitários de Domínio:** Validação de regras e campos obrigatórios de pacientes (`PatientValidatorTest`), cálculo de idade (`AgeCalculator`), cálculo de acurácia sem distorção estatística (`PerformanceCalculatorTest`).
- **Testes dos Motores dos Jogos:** Geração de rodadas, avaliação de respostas, tratamento de tentativas e retry (`GamesEnginesUnitTest`).

---

## ⚠️ Limitações Conhecidas no MVP

1. **Estímulos e Figuras:** Os estímulos utilizam representações emoji + texto e vetoriais nativos para garantia de operação 100% offline e sem bibliotecas pesadas de terceiros.
2. **Validação Clínica de Conteúdo:** As palavras e pares mínimos iniciais seguem regras da fonologia do PB, mas devem ser revisados e validados pelo profissional responsável para cada caso clínico específico.
3. **Personalização de Estímulos:** No MVP, as atividades utilizam o catálogo pré-instalado com filtros de fonemas e categorias. O cadastro de fotos/gravações externas pelo fonoaudiólogo está preparado na arquitetura para versões subsequentes.
