import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose)
    id("org.jetbrains.kotlin.kapt")
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.profylish.onboarding"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        val properties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }
        buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", "\"${properties.getProperty("SUPABASE_PUBLISHABLE_KEY") ?: ""}\"")
        buildConfigField("String", "SECRET", "\"${properties.getProperty("SECRET") ?: ""}\"")
        buildConfigField("String", "SUPABASE_URL", "\"${properties.getProperty("SUPABASE_URL") ?: ""}\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:model"))

    // Supabase
    implementation(libs.bundles.supabase)
    implementation(libs.bundles.ktor)

    // Core & Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)

    // Artık TOML'da tanımlı olduğu için çalışacak:
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.ui.text)

    // Navigation & Hilt
    implementation(libs.androidx.navigation.common.ktx)
    implementation(libs.androidx.navigation.compose) // Doğru isim
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.google.dagger.hilt)
    kapt(libs.google.hilt.compiler)

    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}