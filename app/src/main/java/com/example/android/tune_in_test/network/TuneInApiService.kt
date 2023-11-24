package com.example.android.tune_in_test.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

private const val BASE_URL = "http://opml.radiotime.com"
private const val BROWSE_URL = "/Browse.ashx"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)
    .build()

interface TuneInApiService {
    @GET("{linkURL}")
    fun getProperties(
        @Path("linkURL") linkURL: String? = "",
        @Query("render") type: String = "json",
    ):
            Deferred<TuneInRequest>
}

object TuneInApi {
    val retrofitService: TuneInApiService by lazy {
        retrofit.create(TuneInApiService::class.java)
    }

    fun modifyLink(linkText: String?): String? {
        return linkText?.replace(BASE_URL, "", true)?.replace(BROWSE_URL, "", true)
    }

}
