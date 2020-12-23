package com.udacity.asteroidradar.database

import android.content.Context
import androidx.lifecycle.LiveData

import androidx.room.*
import com.udacity.asteroidradar.Asteroid

const val DATABASE_NAME = "videos"

@Dao
interface AsteroidRadarDao {
    @Query("SELECT * FROM asteroids ORDER BY close_approach_date ASC")
    fun getAllAsteroids(): List<Asteroid>

    @Query("SELECT * FROM asteroids WHERE close_approach_date = :date")
    fun getTodayAsteroid(date: String): List<Asteroid>

    @Query("SELECT * FROM asteroids WHERE close_approach_date >= :startDate " +
            "AND close_approach_date<= :endDate ORDER BY close_approach_date ASC")
    fun getWeekAsteroid(startDate: String, endDate: String): List<Asteroid>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllAsteroids(vararg video: Asteroid)

    @Query("DELETE from asteroids WHERE close_approach_date = :date ")
    fun deleteByDate(date: String)
}

@Database(entities = [Asteroid::class], version = 1)
abstract class AsteroidRadarDatabase: RoomDatabase() {
    abstract val asteroidDao: AsteroidRadarDao
}

private lateinit var INSTANCE: AsteroidRadarDatabase

fun getDatabase(context: Context): AsteroidRadarDatabase {
    synchronized(AsteroidRadarDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                AsteroidRadarDatabase::class.java,
                DATABASE_NAME
            ).build()
        }
    }
    return INSTANCE
}