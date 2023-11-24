package com.example.android.tune_in_test.overview

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.tune_in_test.network.TuneInProperty

class OverviewViewModelFactory(
    private val tuneInProperty: TuneInProperty,
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OverviewViewModel::class.java)) {
            return OverviewViewModel(tuneInProperty, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
