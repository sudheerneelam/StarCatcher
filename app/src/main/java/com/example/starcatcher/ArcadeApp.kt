package com.example.starcatcher

import android.app.Application

class ArcadeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AdManager.init(this)
    }
}
