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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android.tune_in_test.network.TuneInApi
import com.example.android.tune_in_test.network.TuneInProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class TuneInStatus { LOADING, ERROR, DONE }

class OverviewViewModel(tuneInProperty: TuneInProperty, app: Application) : AndroidViewModel(app) {

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
        getTuneInProperties()
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
        viewModelJob.cancel()
    }

    fun displayPropertyDetails(tuneInProperty: TuneInProperty) {
        _navigateToSelectedProperty.value = tuneInProperty
    }

    fun displayPropertyDetailsComplete() {
        _navigateToSelectedProperty.value = null
    }

}

