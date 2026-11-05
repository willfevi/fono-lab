# Guia de Compilação e Geração de Instaladores — Windows Desktop

## 1. Pré-Requisitos

- **Sistema Operacional**: Windows 10 ou Windows 11 (64-bit).
- **JDK**: Java Development Kit 17 (com a ferramenta `jpackage` disponível no JDK padrão).
- **WiX Toolset**: Baixado automaticamente pelo plugin Compose Multiplatform para empacotamento MSI.

---

## 2. Comandos de Compilação e Empacotamento

### Gerar Instalador Executável (.exe)
Compila o cliente Desktop e gera um instalador executável standalone com JRE embutido:

```powershell
.\gradlew.bat buildWindowsExe
```

Artefato gerado:
- `dist/windows/exe/fonolab-installer.exe`

### Gerar Instalador MSI (.msi)
Gera o pacote de instalação padrão do Windows Installer:

```powershell
.\gradlew.bat buildWindowsMsi
```

Artefato gerado:
- `dist/windows/msi/fonolab-installer.msi`

### Gerar Distribuição Windows Completa (.exe + .msi)
Gera ambos os formatos em uma única execução:

```powershell
.\gradlew.bat buildWindowsDistribution
```

---

## 3. Validação de Compilação do Desktop

Para validar se o código do Desktop compila sem erros sem gerar os instaladores pesados:

```powershell
.\gradlew.bat verifyDesktopBuild
```

---

## 4. Execução em Modo de Desenvolvimento

Para rodar a versão Desktop diretamente em modo desenvolvimento durante a codificação:

```powershell
.\gradlew.bat :desktopApp:run
```
