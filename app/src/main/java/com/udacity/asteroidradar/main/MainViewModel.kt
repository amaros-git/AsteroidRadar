package com.udacity.asteroidradar.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.network.RadarApi
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber

class MainViewModel : ViewModel() {

    private val _response = MutableLiveData<String>()
    val response: LiveData<String>
        get() = _response

    init {
        getAsteroids()
    }

    private fun getAsteroids() {
        viewModelScope.launch {
            try {
                val response =
                    RadarApi.retrofitService.getAsteroids(
                        "2020-01-01",
                        "2020-01-08",
                        Constants.PRIVATE_KEY
                    )
                _response.value = response
            }
            catch (e: HttpException) {
                Timber.i("Exception occurred: ${e.message}")
            }
        }
    }
}