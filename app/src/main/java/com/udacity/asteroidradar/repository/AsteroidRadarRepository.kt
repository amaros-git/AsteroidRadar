package com.udacity.asteroidradar.repository

import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.database.AsteroidRadarDatabase
import com.udacity.asteroidradar.network.AsteroidRadarApi
import com.udacity.asteroidradar.network.PictureApi
import com.udacity.asteroidradar.utils.getCurrentDateString
import com.udacity.asteroidradar.utils.getDateString
import com.udacity.asteroidradar.utils.parseAsteroidsJsonResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber


class AsteroidRadarRepository(
    private val database: AsteroidRadarDatabase,
    private val radar: AsteroidRadarApi,
    private val picture: PictureApi
) {

    /**
     * @throws JSONException
     * @throws HttpException
     * @throws SocketTimeoutException
     */
    suspend fun refreshAsteroidCache(): List<Asteroid> {
        Timber.i("refreshAsteroidCache called")
        val startDate = getCurrentDateString(Constants.API_QUERY_DATE_FORMAT)
        val endDate = getDateString(
            Constants.API_QUERY_DATE_FORMAT,
            Constants.DEFAULT_END_DATE_DAYS
        )

        return withContext(Dispatchers.IO) {
            val response = radar.retrofitService.getAsteroids(
                startDate, endDate, Constants.PRIVATE_KEY
            )

            val asteroids = parseAsteroidsJsonResult(JSONObject(response))

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
                getDateString(
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

    /**
     * deletes all from the previous day and then inserts new asteroids
     */
    private fun updateDatabase(asteroids: List<Asteroid>) {
        database.asteroidDao.deleteByDate(getDateString(Constants.API_QUERY_DATE_FORMAT, -1))

        if (asteroids.isNotEmpty()) {
            val asteroidsArray = asteroids.toTypedArray()
            database.asteroidDao.insertAllAsteroids(*asteroidsArray)
        }
    }
}