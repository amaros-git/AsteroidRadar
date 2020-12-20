package com.udacity.asteroidradar.network

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.udacity.asteroidradar.Asteroid
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
interface AsteroidRadarApiService {
    @GET("neo/rest/v1/feed")
    suspend fun getAsteroids(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("api_key") apiKey: String
    ): String
}

object AsteroidRadarApi {
    val retrofitService : AsteroidRadarApiService by lazy {
        retrofit.create(AsteroidRadarApiService::class.java)
    }
}