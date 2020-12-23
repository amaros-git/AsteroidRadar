package com.udacity.asteroidradar

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import com.udacity.asteroidradar.main.NetworkRequestStatus

@BindingAdapter("statusIcon")
fun ImageView.setAsteroidIcon(item: Asteroid?) {
    item?.let {
        setImageResource(when (item.isPotentiallyHazardous) {
            true -> R.drawable.ic_status_potentially_hazardous
            else -> R.drawable.ic_status_normal
        })
    }
}

@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, picture: PictureOfDay?) {
    if (picture?.mediaType == "image") {
        val imgURI = picture.url.toUri().buildUpon().scheme("https").build()
        Picasso.with(imgView.context)
            .load(imgURI)
            .placeholder(R.drawable.loading_animation)
            .error(R.drawable.ic_broken_image)
            .into(imgView)

    } else {
        //TODO else what ?
    }
}

@BindingAdapter("asteroidStatusImage")
fun bindDetailsStatusImage(imageView: ImageView, isHazardous: Boolean) {
    if (isHazardous) {
        imageView.setImageResource(R.drawable.asteroid_hazardous)
    } else {
        imageView.setImageResource(R.drawable.asteroid_safe)
    }
}

@BindingAdapter("astronomicalUnitText")
fun bindTextViewToAstronomicalUnit(textView: TextView, number: Double) {
    val context = textView.context
    textView.text = String.format(context.getString(R.string.astronomical_unit_format), number)
}

@BindingAdapter("kmUnitText")
fun bindTextViewToKmUnit(textView: TextView, number: Double) {
    val context = textView.context
    textView.text = String.format(context.getString(R.string.km_unit_format), number)
}

@BindingAdapter("velocityText")
fun bindTextViewToDisplayVelocity(textView: TextView, number: Double) {
    val context = textView.context
    textView.text = String.format(context.getString(R.string.km_s_unit_format), number)
}

@BindingAdapter("networkStatus")
fun bindStatus(progressBar: ProgressBar, status: NetworkRequestStatus?) {
    when (status) {
        NetworkRequestStatus.LOADING -> {
            progressBar.visibility = View.VISIBLE
        }
        NetworkRequestStatus.DONE -> {
            progressBar.visibility = View.GONE
        }
        else -> {/*error out of scope */}
    }
}
