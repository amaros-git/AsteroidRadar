package com.udacity.asteroidradar.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.network.RadarApi
import com.udacity.asteroidradar.utils.getCurrentDateString
import com.udacity.asteroidradar.utils.getFutureDateString
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.util.*

class MainViewModel : ViewModel() {

    private val _response = MutableLiveData<String>()
    val response: LiveData<String>
        get() = _response

    init {
        getAsteroids()
    }

    private fun getAsteroids() {
        val startDate = getCurrentDateString(Constants.API_QUERY_DATE_FORMAT)
        Timber.i("startDate =  $startDate")
        val endDate = getFutureDateString(Constants.API_QUERY_DATE_FORMAT, 7)
        Timber.i("startDate =  $endDate")

        viewModelScope.launch {
            try {
                val response =
                    RadarApi.retrofitService.getAsteroids(
                        startDate,
                        endDate,
                        Constants.PRIVATE_KEY
                    )
                _response.value = response
            }
            catch (e: HttpException) {
                Timber.e("Exception occurred: ${e.message}")
            }
        }
    }
}