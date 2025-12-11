// 👇 1. BU BLOĞU EN TEPEYE EKLEYİN (Gradle sisteminin kendisi için zorlama)
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    configurations.all {
        resolutionStrategy {
            force("com.squareup:javapoet:1.13.0")
        }
    }
}

// 👇 2. Pluginleriniz (Aynen kalıyor)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    // Hilt ve Kapt pluginlerini de buraya eklemek iyi bir pratiktir (apply false ile)
    alias(libs.plugins.google.hilt) apply false
    alias(libs.plugins.jetbrains.kotlin.kapt) apply false
}

// 👇 3. Proje Modülleri İçin Zorlama (Sizde olan kısım)
allprojects {
    configurations.all {
        resolutionStrategy {
            force("com.squareup:javapoet:1.13.0")
        }
    }
}