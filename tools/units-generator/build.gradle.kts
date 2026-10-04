plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(libs.kotlinpoet)
    implementation(libs.ktfmt)
}

kotlin { jvmToolchain(21) }

application { mainClass = "org.maplibre.spatialk.codegen.MainKt" }

tasks.named<JavaExec>("run") { args(rootProject.projectDir.absolutePath) }

tasks.register<JavaExec>("checkGenerated") {
    description = "Check that the committed units match the catalog."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = application.mainClass
    args(rootProject.projectDir.absolutePath, "--check")
}
