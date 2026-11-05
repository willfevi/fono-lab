// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
}

val isWindowsHost = providers.systemProperty("os.name").get().startsWith("Windows", ignoreCase = true)

val distAndroidDebugDir = layout.projectDirectory.dir("dist/android/debug")
val distAndroidReleaseDir = layout.projectDirectory.dir("dist/android/release")
val distWindowsExeDir = layout.projectDirectory.dir("dist/windows/exe")
val distWindowsMsiDir = layout.projectDirectory.dir("dist/windows/msi")
val distWindowsPortableDir = layout.projectDirectory.dir("dist/windows/portable")
val distWebDir = layout.projectDirectory.dir("dist/web")

val buildAndroidDebugApk = tasks.register<Copy>("buildAndroidDebugApk") {
    group = "distribution"
    description = "Compila e copia o APK de depuracao do Android para dist/android/debug/"
    dependsOn(":app:assembleDebug")

    from(layout.projectDirectory.file("app/build/outputs/apk/debug/app-debug.apk"))
    into(distAndroidDebugDir)
    rename { "fonolab-debug.apk" }

    doLast {
        println("APK de depuracao copiado com sucesso para: $destinationDir/fonolab-debug.apk")
    }
}

val buildAndroidReleaseApk = tasks.register<Copy>("buildAndroidReleaseApk") {
    group = "distribution"
    description = "Compila e copia o APK de release do Android para dist/android/release/"
    dependsOn(":app:assembleRelease")

    from(layout.projectDirectory.file("app/build/outputs/apk/release/app-release-unsigned.apk"))
    into(distAndroidReleaseDir)
    rename { "fonolab-release.apk" }

    doLast {
        println("APK de release copiado com sucesso para: $destinationDir/fonolab-release.apk")
    }
}

val buildWindowsExe = tasks.register<Copy>("buildWindowsExe") {
    group = "distribution"
    description = "Gera o instalador .exe do Windows e copia para dist/windows/exe/"
    if (isWindowsHost) {
        dependsOn(":desktopApp:packageExe")
        from(layout.projectDirectory.dir("desktopApp/build/compose/binaries/main/exe")) {
            include("*.exe")
        }
        into(distWindowsExeDir)
        rename { "fonolab-installer.exe" }
        doLast {
            println("Instalador Windows (.exe) copiado para: $destinationDir/fonolab-installer.exe")
        }
    } else {
        doLast {
            println("AVISO: Geracao de instalador .exe para Windows requer ambiente Windows.")
        }
    }
}

val buildWindowsMsi = tasks.register<Copy>("buildWindowsMsi") {
    group = "distribution"
    description = "Gera o instalador .msi do Windows e copia para dist/windows/msi/"
    if (isWindowsHost) {
        dependsOn(":desktopApp:packageMsi")
        from(layout.projectDirectory.dir("desktopApp/build/compose/binaries/main/msi")) {
            include("*.msi")
        }
        into(distWindowsMsiDir)
        rename { "fonolab-installer.msi" }
        doLast {
            println("Instalador Windows (.msi) copiado para: $destinationDir/fonolab-installer.msi")
        }
    } else {
        doLast {
            println("AVISO: Geracao de instalador .msi para Windows requer ambiente Windows.")
        }
    }
}

val buildWindowsPortable = tasks.register<Copy>("buildWindowsPortable") {
    group = "distribution"
    description = "Gera o aplicativo Windows portatil (executavel direto sem instalador) em dist/windows/portable/FonoLab/"
    if (isWindowsHost) {
        dependsOn(":desktopApp:packageAppImage")
        from(layout.projectDirectory.dir("desktopApp/build/compose/binaries/main/app/FonoLab"))
        into(distWindowsPortableDir.dir("FonoLab"))
        doLast {
            println("Aplicativo Windows portatil gerado em: $destinationDir/FonoLab.exe")
        }
    } else {
        doLast {
            println("AVISO: Geracao de versao portatil para Windows requer ambiente Windows.")
        }
    }
}

val buildWindowsDistribution = tasks.register("buildWindowsDistribution") {
    group = "distribution"
    description = "Gera a distribuicao completa para Windows (executavel portatil e instaladores)"
    dependsOn(buildWindowsPortable, buildWindowsExe, buildWindowsMsi)
    doLast {
        println("Distribuicao Windows concluida com sucesso!")
    }
}


val buildWebDistribution = tasks.register<Copy>("buildWebDistribution") {
    group = "distribution"
    description = "Gera a distribuicao web de producao (Wasm + JS + HTML) e copia para dist/web/"
    dependsOn(":webApp:wasmJsBrowserDistribution")

    from(layout.projectDirectory.dir("webApp/build/dist/wasmJs/productionExecutable"))
    into(distWebDir)

    doLast {
        println("Distribuicao Web gerada com sucesso em: $destinationDir")
    }
}

val buildAllDistributions = tasks.register("buildAllDistributions") {
    group = "distribution"
    description = "Gera todas as distribuicoes multiplataforma suportadas pelo ambiente (Android APKs e Windows Desktop)"
    dependsOn(buildAndroidDebugApk, buildAndroidReleaseApk, buildWebDistribution)
    if (isWindowsHost) {
        dependsOn(buildWindowsDistribution)
    }
    doLast {
        println("Build de todas as distribuicoes concluido com sucesso na pasta dist/!")
    }
}

// ============================================================================
// VERIFICATION TASKS
// ============================================================================

val verifyAndroidBuild = tasks.register("verifyAndroidBuild") {
    group = "verification"
    description = "Valida a compilacao do app Android e dos alvos Android KMP"
    dependsOn(":core:domain:compileAndroidMain", ":shared:compileAndroidMain", ":app:compileDebugSources")
    doLast {
        println("Verificacao Android concluida com sucesso!")
    }
}

val verifyDesktopBuild = tasks.register("verifyDesktopBuild") {
    group = "verification"
    description = "Valida a compilacao dos alvos Desktop (:desktopApp e :shared desktop)"
    dependsOn(":shared:compileKotlinDesktop", ":desktopApp:compileKotlin")
    doLast {
        println("Verificacao Desktop (Windows) concluida com sucesso!")
    }
}


val verifyWebBuild = tasks.register("verifyWebBuild") {
    group = "verification"
    description = "Valida a compilacao dos alvos Web (:webApp e :shared wasmJs)"
    dependsOn(":shared:compileKotlinWasmJs", ":webApp:compileKotlinWasmJs")
    doLast {
        println("Verificacao Web (WasmJs) concluida com sucesso!")
    }
}

val verifySharedTests = tasks.register("verifySharedTests") {
    group = "verification"
    description = "Executa os testes do dominio KMP no host Android"
    dependsOn(":core:domain:testAndroidHostTest")
}

val verifyAllPlatforms = tasks.register("verifyAllPlatforms") {
    group = "verification"
    description = "Compila Android, Windows e Web e executa a verificacao do dominio no host Android"
    dependsOn(verifyAndroidBuild, verifyDesktopBuild, verifyWebBuild, verifySharedTests)
}
