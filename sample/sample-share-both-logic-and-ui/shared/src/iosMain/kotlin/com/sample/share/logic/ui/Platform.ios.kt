package com.sample.share.logic.ui

import platform.UIKit.UIDevice

class IOSPlatform : AppPlatform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): AppPlatform = IOSPlatform()