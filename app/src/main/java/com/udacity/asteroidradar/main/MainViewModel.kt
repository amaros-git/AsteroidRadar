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

    val asteroids: LiveData<List<Asteroid>> = asteroidRepository.asteroids

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
                asteroidRepository.refreshAsteroidsCache()
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
                _showToastEvent.value = "Network error, trying to display saved data"
            }
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
