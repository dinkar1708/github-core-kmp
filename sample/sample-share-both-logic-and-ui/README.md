# 🎨 Sample: Share Both Logic and UI (Tier 3: Compose Multiplatform)

This sample demonstrates JetBrains' official **"Share both logic and UI"** adoption pattern using **Compose Multiplatform (CMP)**.

---

## 🎯 Purpose
* Share **both** business logic and user interface from a single codebase across:
  - 🤖 **Android** (Compose runtime)
  - 🍏 **iOS** (`ComposeUIViewController` rendering via Skiko)
  - 🖥️ **Desktop** (JVM Skia windowing)
* Consumes the headless **`:github-core`** SDK for networking, caching, and repository search use cases.

---

## 📖 Documentation
See the complete implementation guide in [**`docs/samples/3-guide-share-both-logic-and-ui.md`**](../../docs/samples/3-guide-share-both-logic-and-ui.md).

---

## 🔗 Setup
To link the local SDK engine, declare composite build substitution in `settings.gradle.kts`:

```kotlin
includeBuild("../../") {
    dependencySubstitution {
        substitute(module("com.github.core:github-core")).using(project(":github-core"))
    }
}
```

And in `composeApp/build.gradle.kts`:

```kotlin
dependencies {
    // Shared headless SDK engine
    commonMainImplementation("com.github.core:github-core")

    // Compose Multiplatform UI
    commonMainImplementation(compose.runtime)
    commonMainImplementation(compose.foundation)
    commonMainImplementation(compose.material3)
    commonMainImplementation(compose.ui)
}
```
