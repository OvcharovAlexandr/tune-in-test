package com.example.android.tune_in_test.network

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

data class TuneInRequest(
    val head: HeaderClass,
    val body: List<TuneInProperty>,
)

data class HeaderClass(
    val title: String?,
)

@Parcelize
data class TuneInProperty(
    val type: String?,
    val text: String?,
    @Json(name = "URL") val linkURL: String?,
    val children: List<TuneInProperty>?,
    @Json(name = "image") val imgSrcUrl: String?
) : Parcelable