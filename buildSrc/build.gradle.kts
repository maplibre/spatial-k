import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins { `kotlin-dsl` }

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.gradle.kotlin)
    implementation(libs.gradle.kotlin.serialization)
    implementation(libs.gradle.dokka)
    implementation(libs.gradle.publish)
    implementation(libs.gradle.benchmark)
    implementation(libs.gradle.kover)
    implementation(libs.gradle.semver)
    implementation(libs.gradle.mkdocs.build)
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_1_8 } }
