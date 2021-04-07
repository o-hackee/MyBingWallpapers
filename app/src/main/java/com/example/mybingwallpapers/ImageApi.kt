package com.example.mybingwallpapers

import android.widget.Toast
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


private const val BASE_URL = "https://www.bing.com/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()
private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

data class bingImageData(
    val images: List<bingImage>,
    val tooltips: Tooltips
)

data class bingImage(
    val startdate: String, // TODO convert? +2
    val fullstartdate: String,
    val enddate: String,
    val url: String,
    val urlbase: String,
    val copyright: String,
    val copyrightlink: String,
    val title: String,
    val quiz: String,
    val wp: Boolean,
    val hsh: String,
    val drk: Int,
    val top: Int,
    val bot: Int,
    val hs: List<Any>
)

data class Tooltips(
    val loading: String,
    val previous: String,
    val next: String,
    val walle: String,
    val walls: String
)

interface BingWallpapersService {
    @GET("HPImageArchive.aspx")
    fun getProperties(
        @Query("format") format: String,
        @Query("n") number: Int,
        @Query("mkt") market: String
    ): Call<bingImageData>
}

object BingWallpapersApi {
    private val retrofitService: BingWallpapersService by lazy {
        retrofit.create(BingWallpapersService::class.java)
    }

    fun getImage() {
        val call = retrofitService.getProperties("js", 1, "en-US")
        call.enqueue(object : Callback<bingImageData> {
            override fun onResponse(call: Call<bingImageData>, response: Response<bingImageData>) {
                val image = response.body()?.images?.firstOrNull()
            }

            override fun onFailure(call: Call<bingImageData>, t: Throwable) {
                val msg = t.message
            }
        })
    }
}