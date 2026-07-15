package com.github.pluginsgram.core

import android.app.Application

class PluginsGramApp : Application() {
    override fun onCreate() {
        super.onCreate()
        System.loadLibrary("tdjson")
    }
}
