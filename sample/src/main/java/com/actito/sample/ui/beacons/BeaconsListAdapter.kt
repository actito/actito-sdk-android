package com.actito.sample.ui.beacons

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.actito.geo.models.ActitoBeacon
import com.actito.sample.R
import com.actito.sample.databinding.BeaconRowBinding

class BeaconsListAdapter : ListAdapter<ActitoBeacon, RecyclerView.ViewHolder>(BeaconsDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder(
            BeaconRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        (holder as ItemViewHolder).bind(item)
    }

    private inner class ItemViewHolder(
        private val binding: BeaconRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ActitoBeacon) {
            binding.name.text = item.name
            binding.details.text = "${item.major}:${item.minor}"

            when (item.proximity) {
                ActitoBeacon.Proximity.IMMEDIATE -> binding.proximity.setImageResource(R.drawable.ic_signal_wifi_4_bar)
                ActitoBeacon.Proximity.NEAR -> binding.proximity.setImageResource(R.drawable.ic_signal_wifi_3_bar)
                ActitoBeacon.Proximity.FAR -> binding.proximity.setImageResource(R.drawable.ic_signal_wifi_1_bar)
                ActitoBeacon.Proximity.UNKNOWN -> binding.proximity.setImageDrawable(null)
            }
        }
    }
}

private class BeaconsDiffCallback : DiffUtil.ItemCallback<ActitoBeacon>() {
    override fun areItemsTheSame(oldItem: ActitoBeacon, newItem: ActitoBeacon): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ActitoBeacon, newItem: ActitoBeacon): Boolean {
        return oldItem == newItem
    }
}
