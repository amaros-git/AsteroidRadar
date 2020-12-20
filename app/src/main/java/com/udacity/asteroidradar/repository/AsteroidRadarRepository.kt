package com.udacity.asteroidradar.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.database.AsteroidRadarDatabase
import com.udacity.asteroidradar.network.AsteroidRadarApi
import com.udacity.asteroidradar.utils.getCurrentDateString
import com.udacity.asteroidradar.utils.getFutureDateString
import com.udacity.asteroidradar.utils.parseAsteroidsJsonResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import timber.log.Timber

class AsteroidRadarRepository(
    private val database: AsteroidRadarDatabase,
    private val network: AsteroidRadarApi
    ) {

    val asteroids: LiveData<List<Asteroid>>
        get() = database.asteroidDao.getAllAsteroids()

    /**
     * for caching
     */
    suspend fun refreshAsteroidsCache(): List<Asteroid> {
        val startDate = getCurrentDateString(Constants.API_QUERY_DATE_FORMAT)
        val endDate = getFutureDateString(Constants.API_QUERY_DATE_FORMAT, 7)
        //Timber.i("startDate =  $endDate, endDate = $endDate")

        return withContext(Dispatchers.IO) {
            val responseString = try {
                network.retrofitService.getAsteroids(
                    startDate,
                    endDate,
                    Constants.PRIVATE_KEY
                )
            } catch (e: HttpException) {
                Timber.e("Network error: ${e.message}")
                ""
            }

            val asteroids = parseAsteroids(responseString)

            updateDatabase(asteroids)

            asteroids
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