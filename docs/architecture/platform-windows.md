# Arquitetura da Plataforma Windows Desktop

## 1. Visão Geral

O módulo `:desktopApp` fornece o cliente desktop nativo para Windows do **FonoLab**. O aplicativo é empacotado como um binário independente contendo um Java Runtime Environment (JRE) embutido gerado via `jpackage`, dispensando a necessidade de o usuário final ter o Java instalado na máquina.

---

## 2. Tecnologias e Configuração

- **Linguagem**: Kotlin 2.3.x com Compose Multiplatform Desktop (`org.jetbrains.compose.desktop`).
- **Alvos de Instalação**:
  - Executável autônomo com assistente de instalação (`.exe`).
  - Pacote de instalação do Windows Installer (`.msi`) gerado via integração com WiX Toolset.
- **Janela e Resolução**:
  - Dimensão padrão: `1280x820` dp.
  - Limite mínimo: `800x600` dp.
  - Comportamento de fechamento: encerramento gracioso de processos em segundo plano.

---

## 3. Ponto de Entrada (`Main.kt`)

O arquivo `desktopApp/src/jvmMain/kotlin/com/healthcare/fonolab/desktop/Main.kt` inicializa a aplicação desktop:

```kotlin
fun main() = application {
    val platformServices = remember { createDesktopPlatformServices() }
    val windowState = rememberWindowState(width = 1280.dp, height = 820.dp)

    Window(
        onCloseRequest = ::exitApplication,
        title = "FonoLab — Gestão Clínica & Terapia Fonoaudiológica",
        state = windowState
    ) {
        window.minimumSize = Dimension(800, 600)

        App(platformServices = platformServices)
    }
}
```

---

## 4. Integração de Sistema Operacional

- **Áudio e Feedback Fônico**: Interage com o sistema de som do Windows para sinalização de acertos/erros nos jogos e sintetizador de pistas auditivas.
- **Exportação de Documentos**: Grava arquivos de texto e relatórios clínicos no diretório do usuário (`Documents/FonoLab/`).
- **Área de Transferência e Atalhos**: Suporte nativo a atalhos de teclado (`Ctrl+C`, `Ctrl+V`, navegação por `Tab`) e cópia de relatórios.
- **Registro do Windows e Menu Iniciar**: Atalho no menu do Windows sob o grupo "FonoLab" com suporte a desinstalação padrão.
