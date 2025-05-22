package com.actito.sample.ui.assets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.actito.assets.models.ActitoAsset
import com.actito.sample.R
import com.actito.sample.databinding.ItemAssetBinding
import com.bumptech.glide.Glide

class AssetsListAdapter(
    private val onAssetClicked: (ActitoAsset) -> Unit,
) : ListAdapter<ActitoAsset, RecyclerView.ViewHolder>(AssetsDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder(
            ItemAssetBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        (holder as ItemViewHolder).bind(item)
    }

    private inner class ItemViewHolder(
        private val binding: ItemAssetBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ActitoAsset) {
            binding.assetTitle.text = item.title

            if (item.url == null) {
                binding.assetImage.setImageResource(R.drawable.ic_baseline_text_fields_24)
            } else {
                when (item.metaData?.contentType) {
                    "image/jpeg", "image/gif", "image/png" -> {
                        val imgUrl = item.url
                        Glide.with(binding.root.context).load(imgUrl).into(binding.assetImage)
                    }

                    "video/mp4" -> {
                        binding.assetImage.setImageResource(R.drawable.ic_baseline_video_camera_back_24)
                    }

                    "application/pdf" -> {
                        binding.assetImage.setImageResource(R.drawable.ic_baseline_picture_as_pdf_24)
                    }

                    "application/json" -> {
                        binding.assetImage.setImageResource(R.drawable.ic_baseline_data_object_24)
                    }

                    "text/javascript" -> {
                        binding.assetImage.setImageResource(R.drawable.ic_baseline_javascript_24)
                    }

                    "text/css" -> {
                        binding.assetImage.setImageResource(R.drawable.ic_baseline_css_24)
                    }

                    "text/html" -> {
                        binding.assetImage.setImageResource(R.drawable.ic_baseline_html_24)
                    }
                }
            }

            binding.root.setOnClickListener {
                onAssetClicked(item)
            }
        }
    }
}

private class AssetsDiffCallback : DiffUtil.ItemCallback<ActitoAsset>() {
    override fun areItemsTheSame(oldItem: ActitoAsset, newItem: ActitoAsset): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: ActitoAsset, newItem: ActitoAsset): Boolean {
        return oldItem == newItem
    }
}
