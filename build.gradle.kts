// Archivo build.gradle.kts (Nivel Proyecto)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    // Añadimos el plugin de Google Services
    id("com.google.gms.google-services") version "4.4.0" apply false
}