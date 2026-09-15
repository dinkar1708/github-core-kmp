# 📱 Heterogeneous Client Integration Blueprints

> 📖 **Official Standards & References:**  
> • [JetBrains: Kotlin Multiplatform One Logic Layer, Native UI](https://kotlinlang.org/multiplatform/#choose-share-what-logic-native-ui)  
> • [Android Developers: Architecture Guidelines](https://developer.android.com/topic/architecture)  
> • [Apple Developer: SwiftUI & Concurrency](https://developer.apple.com/documentation/swiftui)  
> • [Flutter Dev: Platform Integration](https://docs.flutter.dev/platform-integration/platform-channels)

This directory contains integration blueprints demonstrating how external frontends adopt **`github-core-kmp`** while maintaining 100% native UI and presentation paradigms.

---

## 🗺️ Client Integration Guides

| Platform | Presentation Paradigm | UI Toolkit | Integration Guide |
| :--- | :--- | :--- | :--- |
| **🤖 Android** | AndroidX `ViewModel` + Kotlin `StateFlow` | Jetpack Compose (Material 3) | [**Android Integration Guide**](./01_android_jetpack_compose_guide.md) |
| **🍎 Apple iOS** | Swift `@Observable` / `@MainActor` + `async/await` | SwiftUI | [**iOS Integration Guide**](./02_ios_swiftui_spm_guide.md) |
| **📱 Flutter** | Riverpod `AsyncNotifier` + Dart Event Loop | Flutter Widgets | [**Flutter Integration Guide**](./03_flutter_riverpod_bridge_guide.md) |

---

## 📊 Platform Target Compatibility Matrix

| Target | Minimum OS / Tooling | Language / Compiler | Distribution Format |
| :--- | :--- | :--- | :--- |
| **Android** | API 24+ (Android 7.0) | Kotlin 2.1.0 / AGP 8.5+ | Android AAR / Maven Publication |
| **Apple iOS** | iOS 15.0+ | Swift 5.9+ / Xcode 15+ | Swift Package Manager (SPM) / XCFramework |
| **Flutter** | Flutter 3.x+ | Dart 3.x+ | Platform MethodChannel / Dart FFI Plugin |
| **JVM Desktop** | JDK 17+ | Kotlin 2.1.0 | JAR / Maven Dependency |
