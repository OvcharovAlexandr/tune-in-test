package com.example.android.tune_in_test.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.Player
import com.example.android.tune_in_test.network.TuneInApi
import com.example.android.tune_in_test.network.TuneInProperty
import com.example.android.tune_in_test.overview.TuneInStatus
import com.example.android.tune_in_test.playback.PlayerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class DetailViewModel(tuneInProperty: TuneInProperty, app: Application) : AndroidViewModel(app) {
    private val _selectedProperty = MutableLiveData<TuneInProperty>()

    val selectedProperty: LiveData<TuneInProperty>
        get() = _selectedProperty

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.IO)

    init {
        _selectedProperty.value = tuneInProperty
        updatePlayList()
    }

    private fun updatePlayList() {
//        _status.value = TuneInStatus.LOADING
        coroutineScope.launch {
            val getAudioDeferred = TuneInApi.retrofitService.getAudio(_selectedProperty.value?.linkURL)
            val requestResult = getAudioDeferred.await()
            withContext(Dispatchers.Main) {
                try {
//                    _status.value = TuneInStatus.DONE
                    if (requestResult.body.isNotEmpty()) {
                        requestResult.body.map { tuneInAudio ->  PlayerService.addMediaItem(tuneInAudio)}
                        PlayerService.play()
                    }
                } catch (e: Exception) {
//                    _headerTitle.value = "Failure: ${e.message}"
//                    _status.value = TuneInStatus.ERROR
//                    _properties.value = ArrayList()
                }
            }
        }
    }
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
        PlayerService.pause()
    }
}