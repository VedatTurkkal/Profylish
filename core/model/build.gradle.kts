plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}
kotlin {
    jvmToolchain(17)
}
dependencies {
    implementation(libs.androidx.compose.runtime)
    implementation(libs.kotlinx.serialization.json)
}