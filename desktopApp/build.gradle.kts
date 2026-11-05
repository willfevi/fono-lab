import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

sourceSets {
    named("main") {
        kotlin.srcDirs("src/jvmMain/kotlin")
        resources.srcDirs("src/jvmMain/resources")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":core:domain"))
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.core)
}

compose.desktop {
    application {
        mainClass = "com.healthcare.fonolab.desktop.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Msi)
            packageName = "FonoLab"
            packageVersion = "1.0.0"
            description = "FonoLab - Plataforma de Gestao Fonoaudiologica e Terapia Gamificada"
            vendor = "FonoLab Healthcare"
            copyright = "© 2026 FonoLab. Todos os direitos reservados."

            windows {
                menu = true
                shortcut = true
                dirChooser = true
                menuGroup = "FonoLab"
                upgradeUuid = "8c6b7389-722a-4f51-b88d-e4eb6a953a12"
            }
        }
    }
}
