package com.github.core.network.client

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURLAuthenticationMethodServerTrust
import platform.Foundation.NSURLCredential
import platform.Foundation.NSURLSessionAuthChallengePerformDefaultHandling
import platform.Foundation.NSURLSessionAuthChallengeUseCredential
import platform.Foundation.credentialForTrust
import platform.Foundation.serverTrust

@OptIn(ExperimentalForeignApi::class)
actual fun createPlatformHttpClient(block: HttpClientConfig<*>.() -> Unit): HttpClient {
    return HttpClient(Darwin) {
        block()
        engine {
            handleChallenge { _, _, challenge, completionHandler ->
                val trust = challenge.protectionSpace.serverTrust
                if (challenge.protectionSpace.authenticationMethod == NSURLAuthenticationMethodServerTrust && trust != null) {
                    val credential = NSURLCredential.credentialForTrust(trust)
                    completionHandler(NSURLSessionAuthChallengeUseCredential, credential)
                } else {
                    completionHandler(NSURLSessionAuthChallengePerformDefaultHandling, null)
                }
            }
        }
    }
}
