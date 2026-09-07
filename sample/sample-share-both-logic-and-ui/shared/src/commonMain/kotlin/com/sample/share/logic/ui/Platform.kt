package com.sample.share.logic.ui

interface AppPlatform {
    val name: String
}

expect fun getPlatform(): AppPlatform