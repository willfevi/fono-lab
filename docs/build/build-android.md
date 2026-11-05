# Guia de Compilação e Geração de APKs — Android

## 1. Pré-Requisitos

- **JDK**: Java Development Kit 17 (versão 17.0.x ou superior).
- **Android SDK**: Plataforma API 34 ou 35 com Build-Tools instaladas.
- **Gradle**: Gradle 9.6.0 (utilize o wrapper `./gradlew` ou `.\gradlew.bat`).

---

## 2. Comandos de Compilação

### Gerar APK de Depuração (Debug)
Compila o aplicativo móvel com símbolos de depuração e salva o executável final em `dist/android/debug/`:

```powershell
.\gradlew.bat buildAndroidDebugApk
```

Artefato gerado:
- `dist/android/debug/fonolab-debug.apk`

### Gerar APK de Produção (Release)
Compila o aplicativo com otimizações do R8, minificação de bytecode e salva o pacote final em `dist/android/release/`:

```powershell
.\gradlew.bat buildAndroidReleaseApk
```

Artefato gerado:
- `dist/android/release/fonolab-release.apk`

---

## 3. Validação de Compilação do Android

Para validar a integridade da compilação de código do Android sem gerar os arquivos de instalação:

```powershell
.\gradlew.bat verifyAndroidBuild
```

---

## 4. Instalação Direta via ADB

Para instalar o APK gerado diretamente no dispositivo Android conectado:

```powershell
adb install -r dist/android/debug/fonolab-debug.apk
```
