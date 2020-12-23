package com.udacity.asteroidradar.main

import android.app.Application
import android.content.Context
import android.graphics.Picture
import androidx.core.content.edit
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

enum class NetworkRequestStatus { LOADING, ERROR, DONE }

class MainViewModel(application: Application) : AndroidViewModel(application) {

    //Didn't find better solution yet to restore Recycler View data
    //on navigation back. Week is default on ViewModel creation
    //Initial value shall be none to avoid double read from database
    //because when viewModel is created it calls week asteroids
    private enum class LiveDataType { TODAY, WEEK, ALL, NONE }
    private var currentLiveDataType = LiveDataType.NONE

    val sharedPref = application.getSharedPreferences("Picture", Context.MODE_PRIVATE)

    private val asteroidRepository = AsteroidRadarRepository(
        getDatabase(application),
        AsteroidRadarApi,
        PictureApi
    )

    //Liva data and events
    private val _todayAsteroids = MutableLiveData<List<Asteroid>?>()
    val todayAsteroids: LiveData<List<Asteroid>?>
        get() = _todayAsteroids

    private val _weekAsteroids = MutableLiveData<List<Asteroid>?>()
    val weekAsteroids: LiveData<List<Asteroid>?>
        get() = _weekAsteroids

    private val _allAsteroids = MutableLiveData<List<Asteroid>?>()
    val allAsteroids: LiveData<List<Asteroid>?>
        get() = _allAsteroids

    private val _pictureOfDay = MutableLiveData<PictureOfDay>()
    val pictureOfDay: LiveData<PictureOfDay>
        get() = _pictureOfDay

    private val _showToastEvent = MutableLiveData<String?>()
    val showToastEvent: LiveData<String?>
        get() = _showToastEvent

    private val _navigateToDetailsEvent = MutableLiveData<Asteroid?>()
    val navigateToDetailsEvent: LiveData<Asteroid?>
        get() = _navigateToDetailsEvent

    private val _networkRequestStatus = MutableLiveData<NetworkRequestStatus>()
    val networkRequestStatus: LiveData<NetworkRequestStatus>
        get() = _networkRequestStatus
    //

    init {
        //on the start default is week asteroids. But on the very first
        //application start database is empty.
        viewModelScope.launch {
            val asteroids = asteroidRepository.getWeekAsteroids()
            if (asteroids.isEmpty()) {
                _networkRequestStatus.value = NetworkRequestStatus.LOADING
                try {
                    _weekAsteroids.value = asteroidRepository.refreshAsteroidCache()
                    _networkRequestStatus.value = NetworkRequestStatus.DONE
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
                    _networkRequestStatus.value = NetworkRequestStatus.DONE
                    _showToastEvent.value = "Cannot connect NASA and local cache is empty. " +
                            "Please check connection or try later"
                }
            } else {
                _weekAsteroids.value = asteroids
            }
            currentLiveDataType = LiveDataType.WEEK
        }

        getPictureOfDay()
    }

    private fun getPictureOfDay() {
        viewModelScope.launch {
            try {
                val picture = asteroidRepository.getPictureOfDay()
                //Cache picture
                with (sharedPref.edit()) {
                    putString("mediaType", picture.mediaType)
                    putString("url", picture.url)
                    putString("title", picture.title)
                    commit()
                }
                _pictureOfDay.value = picture
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
                //try to restore from the cache. Even if nullable, I provide
                //default value so it is save to use not-null assertion operator !!
                val mediaType = sharedPref.getString("mediaType", "none")!!
                val url = sharedPref.getString("url", "none")!!
                val title = sharedPref.getString("title", "none")!!

                val cachedPicture = PictureOfDay(mediaType, title, url)
                Timber.i("cachedPicture = $cachedPicture")
                _pictureOfDay.value = cachedPicture
            }
        }
    }

    fun getTodayAsteroids() {
        viewModelScope.launch {
            _todayAsteroids.value = asteroidRepository.getTodayAsteroid()
            currentLiveDataType = LiveDataType.TODAY
        }
    }

    fun getWeekAsteroids() {
        viewModelScope.launch {
            _weekAsteroids.value = asteroidRepository.getWeekAsteroids()
            currentLiveDataType = LiveDataType.WEEK
        }

    }

    fun getAllAsteroids() {
        viewModelScope.launch {
            _allAsteroids.value = asteroidRepository.getAllAsteroids()
            currentLiveDataType = LiveDataType.ALL
        }
    }

    fun showToastEventCompleted() {
        _showToastEvent.value = null
    }

    fun clearTodayAsteroidsData() {
        _todayAsteroids.value = null
    }

    fun clearWeekAsteroidsData() {
        _weekAsteroids.value = null
    }

    fun clearAllAsteroidsData() {
        _allAsteroids.value = null
    }

    fun displayAsteroidDetails(asteroid: Asteroid) {
        _navigateToDetailsEvent.value = asteroid
    }

    fun displayAsteroidDetailsCompleted() {
        _navigateToDetailsEvent.value = null
    }

    fun refreshRecycler() {
        if (currentLiveDataType == LiveDataType.TODAY) {
            getTodayAsteroids()
        }
        if (currentLiveDataType == LiveDataType.WEEK) {
           getWeekAsteroids()
        }
        if (currentLiveDataType == LiveDataType.ALL) {
            getAllAsteroids()
        }
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
