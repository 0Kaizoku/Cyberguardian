plugins {
    // Android Gradle plugin
    id("com.android.application") version "8.8.2" apply false
    // Kotlin plugins
    kotlin("android")   version "1.9.10" apply false
    kotlin("kapt")      version "1.9.10" apply false
}

// Configure all projects
subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
}

// Configure all projects
tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
