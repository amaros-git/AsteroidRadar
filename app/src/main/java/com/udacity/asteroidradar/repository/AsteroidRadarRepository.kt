package com.udacity.asteroidradar.repository

import androidx.annotation.Nullable
import androidx.lifecycle.LiveData
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.database.AsteroidRadarDatabase
import com.udacity.asteroidradar.network.AsteroidRadarApi
import com.udacity.asteroidradar.network.PictureApi
import com.udacity.asteroidradar.utils.getCurrentDateString
import com.udacity.asteroidradar.utils.getFutureDateString
import com.udacity.asteroidradar.utils.parseAsteroidsJsonResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import timber.log.Timber
import java.net.SocketTimeoutException

//TODO all EXCEPTIONS MUST be moved into ViewModel to report to Fragment about error
//and show some sort of toast
class AsteroidRadarRepository(
    private val database: AsteroidRadarDatabase,
    private val radar: AsteroidRadarApi,
    private val picture: PictureApi
    ) {


    suspend fun refreshAsteroidCache(): List<Asteroid> {
        val startDate = getCurrentDateString(Constants.API_QUERY_DATE_FORMAT)
        val endDate = getFutureDateString(
            Constants.API_QUERY_DATE_FORMAT,
            Constants.DEFAULT_END_DATE_DAYS
        )

        return withContext(Dispatchers.IO) {
            val response = radar.retrofitService.getAsteroids(
                startDate, endDate, Constants.PRIVATE_KEY
            )

            val asteroids = parseAsteroids(response)

            updateDatabase(asteroids)

            asteroids
        }
    }

    suspend fun getTodayAsteroid(): List<Asteroid> {
        return withContext(Dispatchers.IO) {
            database.asteroidDao.getTodayAsteroid(
                getCurrentDateString(Constants.API_QUERY_DATE_FORMAT)
            )
        }
    }

    suspend fun getWeekAsteroids(): List<Asteroid> {
        return withContext(Dispatchers.IO) {
            database.asteroidDao.getWeekAsteroid(
                getCurrentDateString(Constants.API_QUERY_DATE_FORMAT),
                getFutureDateString(
                    Constants.API_QUERY_DATE_FORMAT,
                    Constants.DEFAULT_END_DATE_DAYS
                )
            )
        }
    }

    suspend fun getAllAsteroids(): List<Asteroid> {
        return withContext(Dispatchers.IO) {
            database.asteroidDao.getAllAsteroids()
        }

    }

    suspend fun getPictureOfDay(): PictureOfDay {
        return withContext(Dispatchers.IO) {
            picture.retrofitService.getPictureOfDay(Constants.PRIVATE_KEY)
        }
    }


    private fun parseAsteroids(response: String): List<Asteroid> {
        return try {
                parseAsteroidsJsonResult(JSONObject(response))
            } catch (e: JSONException) {
                Timber.e("Parsing error: ${e.message}")
                emptyList()
            }
    }

    private fun updateDatabase(asteroids: List<Asteroid>) {
        if(asteroids.isNotEmpty()) {
            val asteroidsArray = asteroids.toTypedArray()
            database.asteroidDao.insertAllAsteroids(*asteroidsArray)
        }
    }
}