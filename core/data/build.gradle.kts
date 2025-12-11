plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)

    // CHANGE THIS:
    id("org.jetbrains.kotlin.kapt")

    alias(libs.plugins.google.hilt)
}

android {
    namespace = "com.profylish.data"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:network"))

    implementation(libs.google.dagger.hilt)
    kapt(libs.google.hilt.compiler)

    implementation(libs.bundles.supabase)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}