package com.example.android.tune_in_test.network

import com.squareup.moshi.Json

data class TuneInAudioRequest(val body: List<TuneInAudio>)
data class TuneInAudio(
    @Json(name = "guide_id")val id: String,
    @Json(name = "url") val linkURL: String)