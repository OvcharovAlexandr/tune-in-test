package com.example.android.tune_in_test.playback

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.android.tune_in_test.network.TuneInAudio

object PlayerService{

    private val playbackStateListener: Player.Listener = playbackStateListener()
    private var player: Player? = null

    private var playWhenReady = true
    private var mediaItemIndex = 0
    private var playbackPosition = 0L
    fun initPlayer(appContext: Context){
        player = ExoPlayer.Builder(appContext).build()
            .also { exoPlayer ->
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.addListener(playbackStateListener)
                if (exoPlayer.mediaItemCount > 0) {
                    exoPlayer.prepare()
                    exoPlayer.play()
                }
            }
    }
    fun releasePlayer(){
        player?.let { player ->
            playbackPosition = player.currentPosition
            mediaItemIndex = player.currentMediaItemIndex
            playWhenReady = player.playWhenReady
            player.removeListener(playbackStateListener)
            player.release()
        }
        player = null
    }

    fun getPlayer() = player

    fun addMediaItem(audioItem: TuneInAudio) {
        player?.let { player ->
//            player?.addMediaItem(MediaItem.fromUri("http://solid48.streamupsolutions.com:8077/109fm_live"))
            player.clearMediaItems()
            player.addMediaItem(MediaItem.fromUri(audioItem.linkURL))
        }
    }
    fun play() {
        player?.let { player ->
            player.prepare()
            player.play()
        }
    }

    fun pause() {
        player?.let { player ->
            player.pause()}
    }
}
private fun playbackStateListener() = object : Player.Listener {
    override fun onPlaybackStateChanged(playbackState: Int) {
        val stateString: String = when (playbackState) {
            ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
            ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
            ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
            ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
            else -> "UNKNOWN_STATE             -"
        }
//        Log.d(TAG, "changed state to $stateString")
    }
}
