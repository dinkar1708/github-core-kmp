package com.sample.share.logic.ui

import android.os.Build

class AndroidPlatform : AppPlatform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): AppPlatform = AndroidPlatform()