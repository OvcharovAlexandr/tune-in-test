/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.example.android.tune_in_test.overview

import android.app.Application
import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.android.tune_in_test.network.TuneInApi
import com.example.android.tune_in_test.network.TuneInProperty
import com.example.android.tune_in_test.playback.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class TuneInStatus { LOADING, ERROR, DONE }

class OverviewViewModel(tuneInProperty: TuneInProperty, app: Application) : AndroidViewModel(app) {

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean>
        get() = _isPlaying

    private val _isPaused = MutableLiveData<Boolean>()
    val isPaused: LiveData<Boolean>
        get() = _isPaused

    private val _playingStation = MutableLiveData<String>()
    val playingStation: LiveData<String>
        get() = _playingStation

    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val controller: MediaController?
        get() =
            if (controllerFuture.isDone && !controllerFuture.isCancelled) controllerFuture.get() else null


    private val _linkURL = MutableLiveData<String?>()

    private val _status = MutableLiveData<TuneInStatus>()
    val status: LiveData<TuneInStatus>
        get() = _status

    private val _headerTitle = MutableLiveData<String?>()
    val headerTitle: LiveData<String?>
        get() = _headerTitle

    private val _properties = MutableLiveData<List<TuneInProperty>>()
    val properties: LiveData<List<TuneInProperty>>
        get() = _properties

    private val _navigateToSelectedProperty = MutableLiveData<TuneInProperty?>()

    val navigateToSelectedProperty: LiveData<TuneInProperty?>
        get() = _navigateToSelectedProperty

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.IO)

    init {
        _linkURL.value = tuneInProperty.linkURL
        initializeController(app.applicationContext)
        getTuneInProperties()
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

    private fun flatMapList(list: List<TuneInProperty>): List<TuneInProperty> {
        return list.flatMap { it ->
            when (it.children) {
                null -> listOf(it)
                else -> listOf(it) + flatMapList(it.children)
            }
        }
    }

    private fun getTuneInProperties() {
        _status.value = TuneInStatus.LOADING
        coroutineScope.launch {
            val getPropertiesDeferred = TuneInApi.retrofitService.getProperties(_linkURL.value)
            val requestResult = getPropertiesDeferred.await()
            withContext(Dispatchers.Main) {
                try {
                    _status.value = TuneInStatus.DONE
                    if (requestResult.body.isNotEmpty()) {
                        _properties.value = flatMapList(requestResult.body)
                        _headerTitle.value = requestResult.head.title
                    }
                } catch (e: Exception) {
                    _headerTitle.value = "Failure: ${e.message}"
                    _status.value = TuneInStatus.ERROR
                    _properties.value = ArrayList()
                }
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
            _isPaused.value = false
        } else if (controller.isPlaying) {
            _playingStation.value = (controller.mediaMetadata.title ?: "").toString()
            _isPlaying.value = true
            _isPaused.value = false
        } else {
            _playingStation.value = (controller.mediaMetadata.title ?: "").toString()
            _isPlaying.value = false
            _isPaused.value = true
        }

    }

    fun onPlaybackButtonClick() {
        controller?.run { if (isPlaying) pause() else play() }
    }

    fun displayPropertyDetails(tuneInProperty: TuneInProperty) {
        _navigateToSelectedProperty.value = tuneInProperty
    }

    fun displayPropertyDetailsComplete() {
        _navigateToSelectedProperty.value = null
    }

}

