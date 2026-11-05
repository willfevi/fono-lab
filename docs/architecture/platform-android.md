# Arquitetura da Plataforma Android

## 1. Visão Geral

O módulo `:app` empacota a aplicação móvel Android do **FonoLab**, projetada para dispositivos móveis com Android 8.0 (API 26) até Android 15 (API 35/37).

---

## 2. Configurações Técnicas

- **Namespace**: `com.healthcare.fonolab`
- **Application ID**: `com.healthcare.fonolab`
- **Min SDK**: `26` (garante suporte nativo à API `java.time`, segurança biométrica e notificações estáveis).
- **Target SDK**: `37` (compatibilidade total com as diretrizes mais recentes do Google Play).
- **Java / Kotlin**: Java 17 bytecode, Kotlin 2.3.x com Compose Compiler integrado.
- **Injeção de Dependências**: Koin na raiz Compose. O módulo Room Android registra os DAOs e os repositórios qualificados como `room`. A UI continua usando os repositórios em memória até a validação da migração do banco.
- **Persistência**: Room Database SQLite criptografável e reativo via `StateFlow` e `Flow`.

---

## 3. Ponto de Entrada (`MainActivity`)

A `MainActivity` atua como casca de inicialização (*entrypoint wrapper*) que:
1. Aplica `enableEdgeToEdge()` para imersão total sob a barra de status e barra de navegação gestual do sistema operacional.
2. Injeta e inicializa as dependências de infraestrutura.
3. Invoca o composable raiz multiplataforma `App()` do módulo `:shared`.

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App()
        }
    }
}
```

---

## 4. Otimização e Segurança de Release

No modo Release (`buildAndroidReleaseApk`):
- O R8 / ProGuard executa minificação agressiva e tree-shaking das dependências.
- Geração de perfis de inicialização (*Baseline Profiles*) para carregamento instantâneo da UI.
- Arquitetura 100% offline com banco local seguro.
