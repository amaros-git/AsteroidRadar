package com.udacity.asteroidradar

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import com.squareup.picasso.Picasso
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
fun bindImage(imageView: ImageView, picture: PictureOfDay?) {
    if (picture?.mediaType == "image") {
        val imgURI = picture.url.toUri().buildUpon().scheme("https").build()
        Picasso.with(imageView.context)
            .load(imgURI)
            .placeholder(R.drawable.loading_animation)
            .error(R.drawable.ic_broken_image)
            .into(imageView)

        imageView.contentDescription = picture.title
    }
    //TODO else show some default image or get previous one from cache
}

@BindingAdapter("pictureOfDayDescription")
fun bindPictureDescription(imageView: ImageView, picture: PictureOfDay?) {
    if (null != picture) {
        imageView.contentDescription =
            imageView.context.getString(
                R.string.nasa_picture_of_day_content_description_format, picture.title)
    }
    else {
        imageView.contentDescription =
            imageView.context.getString(R.string.this_is_nasa_s_picture_of_day_showing_nothing_yet)
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

@BindingAdapter("asteroidStatusImageContent")
fun bindDetailsStatusImageContent(imageView: ImageView, isHazardous: Boolean) {
    if (isHazardous) {
        imageView.contentDescription =
            imageView.context.getString(R.string.potentially_hazardous_asteroid_image)
    } else {
        imageView.contentDescription =
            imageView.context.getString(R.string.not_hazardous_asteroid_image)
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
