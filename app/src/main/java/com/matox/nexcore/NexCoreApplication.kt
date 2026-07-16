package com.matox.nexcore

import android.app.Application
import com.matox.nexcore.core.util.AppContainer

class NexCoreApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
    }
}
