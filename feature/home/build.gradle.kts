plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.kotlin.kapt)
    alias(libs.plugins.google.hilt)
}

android {
    namespace = "com.profylish.home"
    compileSdk = 35 // 36 deneysel olabilir, 35 stabil

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    packaging {
        resources {
            pickFirsts.add("META-INF/gradle/incremental.annotation.processors")
        }
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
    buildFeatures{
        compose = true
    }
    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)

    // EKSİK OLAN BU OLABİLİR:
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(libs.google.dagger.hilt)
    kapt(libs.google.hilt.compiler)

    implementation(project(":core:ui"))
    implementation(project(":core:domain"))
    implementation(project(":core:model"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
}