package com.example.android.tune_in_test.network

import com.squareup.moshi.Json

data class TuneInAudioRequest(val body: List<TuneInAudio>)
data class TuneInAudio(
    @Json(name = "player_width")val playerWidth: Double,
    @Json(name = "player_height")val playerHeight: Double,
    @Json(name = "url") val linkURL: String)