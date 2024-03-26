package com.example.android.tune_in_test.detail

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.TextView
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.android.tune_in_test.R
import com.example.android.tune_in_test.network.TuneInApi
import com.example.android.tune_in_test.network.TuneInAudio
import com.example.android.tune_in_test.network.TuneInProperty
import com.example.android.tune_in_test.overview.TuneInStatus
import com.example.android.tune_in_test.playback.PlaybackService
import com.example.android.tune_in_test.playback.PlayerService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL

class DetailViewModel(tuneInProperty: TuneInProperty, app: Application) : AndroidViewModel(app) {

    private val _selectedProperty = MutableLiveData<TuneInProperty>()
    val selectedProperty: LiveData<TuneInProperty>
        get() = _selectedProperty

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean>
        get() = _isPlaying

    private val _isPaused = MutableLiveData<Boolean>()
    val isPaused: LiveData<Boolean>
        get() = _isPaused

    private val _isCurrentTrackPlaying = MutableLiveData<Boolean>()
    val isCurrentTrackPlaying: LiveData<Boolean>
        get() = _isCurrentTrackPlaying

    private lateinit var currentMediaItem: MediaItem

    private val _playingStation = MutableLiveData<String>()
    val playingStation: LiveData<String>
        get() = _playingStation

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.IO)

    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val controller: MediaController?
        get() =
            if (controllerFuture.isDone && !controllerFuture.isCancelled) controllerFuture.get() else null

    init {
        _selectedProperty.value = tuneInProperty
        initializeController(app.applicationContext)
        setCurrentMediaItem()
    }

    private fun initializeController(appContext: Context) {
        controllerFuture =
            MediaController.Builder(
                appContext,
                SessionToken(appContext, ComponentName(appContext, PlaybackService::class.java))
            )
                .buildAsync()
        controllerFuture.addListener({ setController() }, MoreExecutors.directExecutor())
    }

    private fun setCurrentMediaItem() {
        coroutineScope.launch {
            val getAudioDeferred =
                TuneInApi.retrofitService.getAudio(_selectedProperty.value?.linkURL)
            val requestResult = getAudioDeferred.await()

            try {
                if (requestResult.body.isNotEmpty()) {
                    var currentTuneInAudio: TuneInAudio? = null
                    requestResult.body.map { tuneInAudio -> currentTuneInAudio = tuneInAudio }
                    currentTuneInAudio?.let { it ->
                        currentMediaItem =
                            MediaItem.Builder()
                                .setMediaId(it.id)
                                .setUri(it.linkURL)
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle(selectedProperty.value?.text)
                                        .setArtworkUri(selectedProperty.value?.imgSrcUrl?.toUri())
                                        .build()
                                ).build()
                    }
                }
            } catch (e: Exception) {
//                    _headerTitle.value = "Failure: ${e.message}"
//                    _status.value = TuneInStatus.ERROR
//                    _properties.value = ArrayList()
            }

        }
    }

    override fun onCleared() {
        super.onCleared()
        releaseController()
        viewModelJob.cancel()
    }

    private fun releaseController() {
        MediaController.releaseFuture(controllerFuture)
    }

    private fun setController() {
        val controller = this.controller ?: return
        controller.prepare()
        updateMediaMetadataUI()
        controller.addListener(
            object : Player.Listener {
                override fun onEvents(player: Player, events: Player.Events) {
                    if (events.contains(Player.EVENT_TRACKS_CHANGED)) {
                        updateMediaMetadataUI()
                    }
                    if (events.contains(Player.EVENT_IS_PLAYING_CHANGED)) {
                        updateMediaMetadataUI()
                    }
                }
            }
        )
    }

    private fun updateMediaMetadataUI() {

        val controller = this.controller
        if (controller == null || controller.mediaItemCount == 0) {
            _playingStation.value = "no music available"
            _isPlaying.value = false
            _isCurrentTrackPlaying.value = false
            _isPaused.value = false
        } else if (controller.isPlaying) {
            _playingStation.value = (controller.mediaMetadata.title ?: "").toString()
            _isPlaying.value = true
            _isCurrentTrackPlaying.value =
                (controller.mediaMetadata.title == selectedProperty.value?.text)
            _isPaused.value = false
        } else {
            _playingStation.value = (controller.mediaMetadata.title ?: "").toString()
            _isPlaying.value = false
            _isCurrentTrackPlaying.value = false
            _isPaused.value = true
        }

    }

    fun onPlaybackButtonClick() {
        controller?.run { if (isPlaying) pause() else play() }
    }

    fun onCurrentTrackPlaybackButtonClick() {
        controller?.let {
            if (it.isPlaying && _isCurrentTrackPlaying.value!!) {
                it.pause()
            } else {
                it.clearMediaItems()
                it.setMediaItem(currentMediaItem)
                it.prepare()
                it.play()
            }
        }
    }
}