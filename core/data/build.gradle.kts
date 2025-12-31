plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.kapt)
    alias(libs.plugins.google.hilt)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    namespace = "com.profylish.data"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    kotlin {
        jvmToolchain(17)

        compilerOptions {
            freeCompilerArgs.add("-opt-in=kotlinx.serialization.InternalSerializationApi")
        }
    }

}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:network"))
    implementation(project(":core:datastore"))

    implementation(libs.google.dagger.hilt)
    kapt(libs.google.hilt.compiler)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.room.ktx)
    implementation(libs.bundles.supabase)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}