package com.udacity.asteroidradar.main

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.udacity.asteroidradar.Asteroid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

class AsteroidRecyclerAdapter (private var clickListener: AsteroidClickListener) :
    ListAdapter<ViewDataItem, RecyclerView.ViewHolder>(AsteroidDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> Header.from(parent)
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    class ViewHolder private constructor(val binding: ListItemSleepBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    class Header {

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ViewDataItem.Header -> ITEM_VIEW_TYPE_HEADER
            is ViewDataItem.AsteroidItem -> ITEM_VIEW_TYPE_ITEM
        }

    }
}

class AsteroidDiffCallback : DiffUtil.ItemCallback<ViewDataItem>() {
    override fun areItemsTheSame(oldItem: ViewDataItem, newItem: ViewDataItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ViewDataItem, newItem: ViewDataItem): Boolean {
        return oldItem == newItem
    }

}

sealed class ViewDataItem {
    data class AsteroidItem(val asteroid: Asteroid) : ViewDataItem() {
        override val id = asteroid.id
    }

    object Header : ViewDataItem() {
        override val id = Long.MIN_VALUE
    }

    abstract val id: Long
}

class AsteroidClickListener(val clickListener: (asteroidId: Long) -> Unit) {
    fun onClick(asteroid: Asteroid) = clickListener(asteroid.id)
}
