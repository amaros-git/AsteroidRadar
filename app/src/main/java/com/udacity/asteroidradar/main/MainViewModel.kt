package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.network.AsteroidRadarApi
import com.udacity.asteroidradar.network.PictureApi
import com.udacity.asteroidradar.repository.AsteroidRadarRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.net.SocketTimeoutException


class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val asteroidRepository = AsteroidRadarRepository(
        getDatabase(application),
        AsteroidRadarApi,
        PictureApi
    )

    private val _todayAsteroids = MutableLiveData<List<Asteroid>>()
    val todayAsteroids: LiveData<List<Asteroid>>
        get() = _todayAsteroids

    private val _weekAsteroids = MutableLiveData<List<Asteroid>>()
    val weekAsteroids: LiveData<List<Asteroid>>
        get() = _weekAsteroids

    private val _allAsteroids = MutableLiveData<List<Asteroid>>()
    val allAsteroids: LiveData<List<Asteroid>>
        get() = _allAsteroids

    private val _pictureOfDay = MutableLiveData<PictureOfDay>()
    val pictureOfDay: LiveData<PictureOfDay>
        get() = _pictureOfDay

    //null means no message to show
    private val _showToastEvent = MutableLiveData<String?>()
    val showToastEvent: LiveData<String?>
        get() = _showToastEvent

    init {
        viewModelScope.launch {
            try {
                _pictureOfDay.value = asteroidRepository.getPictureOfDay()
            } catch (e: Exception) {
                when (e.cause) {
                    is HttpException -> {
                        Timber.e("Network error: ${e.message}")
                    }
                    is SocketTimeoutException -> {
                        Timber.e("Socket timeout: ${e.message}")
                    }
                    else -> Timber.e("Unexpected exception: ${e.message}")
                }
                //TODO rework to get last saved in case of error
            }
        }
    }

    private suspend fun refreshAsteroidCache(): List<Asteroid> {
        return try {
            asteroidRepository.refreshAsteroidCache()
        } catch (e: Exception) {
            when (e.cause) {
                is HttpException -> {
                    Timber.e("Network error: ${e.message}")
                }
                is SocketTimeoutException -> {
                    Timber.e("Socket timeout: ${e.message}")
                }
                else -> Timber.e("Unexpected exception: ${e.message}")
            }
            _showToastEvent.value = "Cannot connect NASA. Please try later"

            emptyList()
        }
    }

    fun getTodayAsteroids() {
        viewModelScope.launch {
            _todayAsteroids.value = asteroidRepository.getTodayAsteroid()
        }
    }

    fun getAllAsteroids() {
        viewModelScope.launch {
            _allAsteroids.value = asteroidRepository.getAllAsteroids()
        }
    }

    fun getWeekAsteroids() {
        viewModelScope.launch {
            var asteroids = asteroidRepository.getWeekAsteroids()
            if (asteroids.isEmpty()) {
                _showToastEvent.value = "No week asteroids found, trying to get from NASA"
                asteroids = refreshAsteroidCache()
            }
            _allAsteroids.value = asteroids

        }
    }

    fun showEventProcessed() {
        _showToastEvent.value = null
    }

    /**
     * Factory for constructing DevByteViewModel with parameter
     */
    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewModel")
        }
    }
}
