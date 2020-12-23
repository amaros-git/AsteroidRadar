package com.udacity.asteroidradar.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.network.AsteroidRadarApi
import com.udacity.asteroidradar.network.PictureApi
import com.udacity.asteroidradar.repository.AsteroidRadarRepository


class RefreshCacheWork(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "refreshCacheWork"
    }

    override suspend fun doWork(): Result {
        val database = getDatabase(applicationContext)
        val repository = AsteroidRadarRepository(database, AsteroidRadarApi, PictureApi)

        return try {
            repository.refreshAsteroidCache()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}