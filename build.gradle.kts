plugins {
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
}

subprojects {
    tasks.withType<org.gradle.api.tasks.testing.AbstractTestTask>().configureEach {
        testLogging {
            showStandardStreams = true
            events("passed", "skipped", "failed", "standardOut")
        }
    }
}