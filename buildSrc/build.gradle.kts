import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins { `kotlin-dsl` }

repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
}

dependencies {
    implementation(libs.gradle.kotlin)
    implementation(libs.gradle.kotlin.serialization)
    implementation(libs.gradle.dokka)
    implementation(libs.gradle.publish)
    implementation(libs.gradle.benchmark)
    implementation(libs.gradle.kover)
    implementation(libs.gradle.semver)
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_1_8 } }
