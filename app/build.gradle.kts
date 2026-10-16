import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // KSP genera el código de Room (DAOs y base de datos) en tiempo de compilación.
    alias(libs.plugins.ksp)
}

android {
    namespace = "cl.duoc.textilcircular"
    compileSdk = 35
    // Se usa la versión de Build-Tools instalada en el equipo (AGP pediría 35.0.0 por defecto).
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId = "cl.duoc.textilcircular"
        // minSdk 24 (Android 7.0): cubre equipos de gama media y baja, requisito del caso.
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // BOM: fija versiones compatibles de todas las librerias de Compose.
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // Window size classes: adaptar la navegacion a telefono / tablet.
    implementation(libs.androidx.material3.window.size)
    implementation(libs.androidx.material.icons.extended)

    // MVVM: ViewModel + recoleccion de StateFlow consciente del ciclo de vida.
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Navegacion entre pantallas (NavController + NavHost).
    implementation(libs.androidx.navigation.compose)

    // Persistencia local (SQLite) con Room: cuentas, perfil, catálogo y mensajes funcionan sin conexión.
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    debugImplementation(libs.androidx.ui.tooling)
}
