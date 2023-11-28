package com.example.android.tune_in_test.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android.tune_in_test.network.TuneInProperty

class DetailViewModel(tuneInProperty: TuneInProperty, app: Application) : AndroidViewModel(app) {
    private val _selectedProperty = MutableLiveData<TuneInProperty>()

    val selectedProperty: LiveData<TuneInProperty>
        get() = _selectedProperty

    init {
        _selectedProperty.value = tuneInProperty
    }

}