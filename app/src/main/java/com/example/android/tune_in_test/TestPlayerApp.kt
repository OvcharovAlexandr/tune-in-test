package com.example.android.tune_in_test

import android.app.Application
import com.example.android.tune_in_test.playback.PlayerService

class TestPlayerApp: Application() {
    override fun onCreate() {
        super.onCreate()
        PlayerService.initPlayer(applicationContext)
    }

    override fun onTerminate() {
        super.onTerminate()
        PlayerService.releasePlayer()
    }
}