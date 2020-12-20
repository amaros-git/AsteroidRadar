package com.udacity.asteroidradar.database

import android.content.Context
import androidx.lifecycle.LiveData

import androidx.room.*
import com.udacity.asteroidradar.Asteroid

const val DATABASE_NAME = "videos"

@Dao
interface AsteroidRadarDao {
    @Query("select * from asteroids")
    fun getAllAsteroids(): LiveData<List<Asteroid>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllAsteroids(vararg video: Asteroid)
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