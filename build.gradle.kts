import org.gradle.internal.os.OperatingSystem

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.room) apply false
}

val isWindowsHost = OperatingSystem.current().isWindows

val distAndroidDebugDir = layout.projectDirectory.dir("dist/android/debug")
val distAndroidReleaseDir = layout.projectDirectory.dir("dist/android/release")
val distWindowsExeDir = layout.projectDirectory.dir("dist/windows/exe")
val distWindowsMsiDir = layout.projectDirectory.dir("dist/windows/msi")

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
        from(layout.projectDirectory.file("desktopApp/build/compose/binaries/main/exe/FonoLab-1.0.0.exe"))
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
        from(layout.projectDirectory.file("desktopApp/build/compose/binaries/main/msi/FonoLab-1.0.0.msi"))
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

val buildWindowsDistribution = tasks.register("buildWindowsDistribution") {
    group = "distribution"
    description = "Gera a distribuicao completa para Windows (executavel e instalador)"
    dependsOn(buildWindowsExe, buildWindowsMsi)
    doLast {
        println("Distribuicao Windows concluida com sucesso!")
    }
}

val buildAllDistributions = tasks.register("buildAllDistributions") {
    group = "distribution"
    description = "Gera todas as distribuicoes multiplataforma suportadas pelo ambiente (Android APKs e Windows Desktop)"
    dependsOn(buildAndroidDebugApk, buildAndroidReleaseApk)
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
    description = "Valida a compilacao de todos os alvos Android (:app e :shared androidTarget)"
    dependsOn(":shared:compileDebugSources", ":app:compileDebugSources")
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

val verifySharedTests = tasks.register("verifySharedTests") {
    group = "verification"
    description = "Executa os testes unitarios compartilhados da arquitetura KMP (:core:domain:test)"
    dependsOn(":core:domain:test")
    doLast {
        println("Testes compartilhados executados com sucesso!")
    }
}

val verifyAllPlatforms = tasks.register("verifyAllPlatforms") {
    group = "verification"
    description = "Executa todas as verificacoes de compilacao e testes multiplataforma (Android, Desktop e Shared)"
    dependsOn(verifyAndroidBuild, verifyDesktopBuild, verifySharedTests)
    doLast {
        println("Verificacao completa de todas as plataformas aprovada com 100% de sucesso!")
    }
}
