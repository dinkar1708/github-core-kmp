package com.github.core

import kotlin.test.Test
import kotlin.test.assertNotNull

class SdkTest {
    @Test
    fun testSdkCreationAndWiring() {
        println("🧪 [github-core] Testing Public SDK Facade Initialization & Module Wiring...")
        
        val sdk = GithubCoreSdk.create()
        println("🚀 [github-core] Sdk instance created: $sdk")
        
        assertNotNull(sdk)
        println("📡 [github-core] Verified Network Client wired: ${sdk.networkClient}")
        assertNotNull(sdk.networkClient)
        
        println("💾 [github-core] Verified Cache Engine wired: ${sdk.cache}")
        assertNotNull(sdk.cache)
        
        println("✅ [github-core] Public SDK Facade fully wired and ready for platform consumers (Android/iOS/Flutter)")
    }
}
