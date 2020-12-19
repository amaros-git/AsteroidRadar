package com.udacity.asteroidradar.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.udacity.asteroidradar.Constants
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query



/*private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()*/

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(Constants.BASE_URL)
    .build()

/**
 * Dates must be in yyyy-MM-dd format
 */
interface RadarApiService {
    @GET("neo/rest/v1/feed")
    suspend fun getAsteroids(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("api_key") apiKey: String
    ): String
}

object RadarApi {
    val retrofitService : RadarApiService by lazy {
        retrofit.create(RadarApiService::class.java)
    }
}