package com.example.android.tune_in_test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.android.tune_in_test.playback.PlayerService

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
    override fun onStart() {
        super.onStart()
        PlayerService.initPlayer(this)
    }
    override fun onResume() {
        super.onResume()
        PlayerService.initPlayer(this)
    }
    override fun onPause() {
        super.onPause()
        PlayerService.releasePlayer()
    }
    override fun onStop() {
        super.onStop()
        PlayerService.releasePlayer()
    }
}
