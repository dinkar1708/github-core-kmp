# Contributing to GitHub Core KMP

Thank you for your interest in contributing to **`github-core-kmp`**!

Please review our complete contributing guidelines and architectural guardrails in [**`docs/CONTRIBUTING.md`**](./docs/CONTRIBUTING.md).

---

### Quick Summary

* **🌿 Branch Naming:** Use semantic prefixes: `feature/*`, `fix/*`, `refactor/*`, `test/*`, `build/*`, `ci/*`, `docs/*`, `perf/*`, `chore/*`.
* **📝 Commit Messages:** All commits must follow [Conventional Commits v1.0.0](https://www.conventionalcommits.org/): `<type>(<scope>): <short imperative summary>`.
* **🏛️ Headless Architecture:** The shared KMP engine stops strictly at the **Domain / Use Case layer**. Never add presentation state or ViewModels to shared code.
* **🧪 Pre-Commit Verification:** Run `./gradlew check` to verify all tests across Android JVM and Apple iOS Simulator targets.
* **📚 References:** Explore [**`docs/references.md`**](./docs/references.md) for platform standards and architectural publications.
