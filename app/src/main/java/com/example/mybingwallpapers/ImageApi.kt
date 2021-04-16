package com.example.mybingwallpapers

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import timber.log.Timber
import java.io.InputStream
import java.lang.Exception


private const val BASE_URL = "https://www.bing.com/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()
private val retrofitJson = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()
private val retrofitImage = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .build()

data class bingImageData(
    val images: List<bingImage>,
    val tooltips: Tooltips
)

data class bingImage(
    val startdate: String,
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
    fun getImageInfo(
        @Query("format") format: String,
        @Query("n") number: Int,
        @Query("mkt") market: String
    ): Call<bingImageData>

    @GET("th")
    fun downloadImage(@Query("id") id: String): Call<ResponseBody>
}

object BingWallpapersApi {
    private val retrofitJsonService: BingWallpapersService by lazy {
        retrofitJson.create(BingWallpapersService::class.java)
    }
    private val retrofitImageService: BingWallpapersService by lazy {
        retrofitImage.create(BingWallpapersService::class.java)
    }

    fun getImageInfo() {
        val call = retrofitJsonService.getImageInfo("js", 1, "en-US")
        call.enqueue(object : Callback<bingImageData> {
            override fun onResponse(call: Call<bingImageData>, response: Response<bingImageData>) {
                val image = response.body()?.images?.firstOrNull()
            }

            override fun onFailure(call: Call<bingImageData>, t: Throwable) {
                val msg = t.message
            }
        })
    }

    fun getImageInfoBlocking(market: String): String? {
        val call = retrofitJsonService.getImageInfo("js", 1, market)
        val response: Response<bingImageData>
        try {
            response = call.execute()
        }
        catch (e: Exception) {
            Timber.e("b1 getImageInfoBlocking ${e.message}")
            return null
        }
        val image = response.body()?.images?.firstOrNull() ?: return null
        if (!image.urlbase.startsWith("/th?id=")) {
            Timber.e("b1 getImageInfoBlocking urlbase = ${image.urlbase}")
            return null
        }
        Timber.i("b1 fullstartdate: ${image.fullstartdate} copyright = ${image.copyright}")
        return image.urlbase.drop(7) + "_720x1280.jpg"
    }

    fun downloadImage(imageId: String): InputStream? {
        val call = retrofitImageService.downloadImage(imageId)
        val response: Response<ResponseBody>
        try {
            response = call.execute()
        }
        catch (e: Exception) {
            Timber.e("b1 downloadImage ${e.message}")
            return null
        }
        val image = response.body()
        if (image == null) {
            Timber.e("b1 downloadImage empty body")
            return null
        }
        return image.byteStream()
    }
}