# Sample: Share a Piece of Logic (Tier 1)

This sample demonstrates JetBrains' official **"Share a piece of logic"** adoption pattern.

---

## Purpose
* Demonstrate consuming **only** `:core-domain` (`QueryValidator`, `PaginationValidator`, domain entities) with **zero third-party dependencies**.
* No Ktor, no SQLDelight, no Compose — pure business logic shared across platforms.

---

## Documentation
See the complete implementation guide in [**`docs/samples/1-guide-share-piece-of-logic.md`**](../../docs/samples/1-guide-share-piece-of-logic.md).

---

## Setup
To link the local SDK engine, declare composite build substitution in `settings.gradle.kts`:

```kotlin
includeBuild("../../") {
    dependencySubstitution {
        substitute(module("com.github.core:core-domain")).using(project(":core-domain"))
    }
}
```

And in `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.core:core-domain")
}
```
