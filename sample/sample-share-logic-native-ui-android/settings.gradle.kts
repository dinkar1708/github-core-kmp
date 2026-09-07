pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "sample_android"

// =========================================================================================
// Headless GitHub Core KMP SDK Integration:
// Repository: https://github.com/dinkar1708/github-core-kmp
//
// OPTION 1: Local Composite Build (Monorepo / Local Development) - ACTIVE BELOW
//   Points to the parent root directory containing the multiplatform SDK modules.
//   Gradle automatically substitutes the external dependency coordinates
//   "com.github.core:github-core" with the local project ":github-core".
//
// OPTION 2: External Project Checkout / Git Submodule
//   If a new/external project consumes this SDK from source, clone or add as a submodule:
//     git submodule add https://github.com/dinkar1708/github-core-kmp submodules/github-core-kmp
//   Then in the new project's settings.gradle.kts:
//     includeBuild("submodules/github-core-kmp") {
//         dependencySubstitution {
//             substitute(module("com.github.core:github-core")).using(project(":github-core"))
//         }
//     }
//
// OPTION 3: Published Binary / GitHub Packages (Maven Artifact)
//   When published as an AAR/library to GitHub Packages or Maven:
//   1. Remove or comment out the includeBuild block below.
//   2. Add the maven repository credentials in dependencyResolutionManagement:
//        maven {
//            url = uri("https://maven.pkg.github.com/dinkar1708/github-core-kmp")
//            credentials {
//                username = ...
//                password = ...
//            }
//        }
//   3. In app/build.gradle.kts, depend on: implementation("com.github.core:github-core:<version>")
// =========================================================================================
includeBuild("../../") {
    dependencySubstitution {
        substitute(module("com.github.core:github-core")).using(project(":github-core"))
    }
}
include(":app")
