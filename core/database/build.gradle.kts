plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt") // Kapt eklentisi (Standart format)
    alias(libs.plugins.google.hilt)  // Hilt eklentisi
}

android {
    namespace = "com.profylish.database"

    // --- HATALI KISIM BURASIYDI, DÜZELTİLDİ ---
    // Eski Hatalı: compileSdk { version = release(36) }
    // Yeni Doğru:
    compileSdk = 36
    // -------------------------------------------

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    // Standart Kütüphaneler
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Hilt (Veritabanı modülünde Hilt kullanılacaksa bunlar şart)
    implementation(libs.google.dagger.hilt)
    kapt(libs.google.hilt.compiler)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}