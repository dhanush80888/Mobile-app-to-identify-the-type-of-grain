package com.example.grainclassifier.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grainclassifier.R
import com.example.grainclassifier.data.entity.ClassificationHistory
import com.example.grainclassifier.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * RecyclerView Adapter utilizing ViewBinding and ListAdapter (DiffUtil) 
 * for presenting grain classification history records.
 */
class HistoryAdapter(
    private val onDeleteClicked: (ClassificationHistory) -> Unit
) : ListAdapter<ClassificationHistory, HistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.US)
        private val timeFormat = SimpleDateFormat("h:mm a", Locale.US)

        fun bind(item: ClassificationHistory) {
            // Bind text views with exact labels
            binding.tvGrainName.text = item.grainName
            binding.tvConfidence.text = "${item.confidencePercentage}%"

            // Format date and time
            val date = Date(item.timestamp)
            binding.tvDate.text = dateFormat.format(date)
            binding.tvTime.text = timeFormat.format(date)

            // Bind image resource
            if (!item.imagePath.isNullOrEmpty()) {
                try {
                    val imageUri = Uri.parse(item.imagePath)
                    binding.ivGrainImage.setImageURI(imageUri)
                    
                    // Safety check: if image fails to render (e.g. URI permission revoked), fallback
                    if (binding.ivGrainImage.drawable == null) {
                        binding.ivGrainImage.setImageResource(R.drawable.ic_grain_logo)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    binding.ivGrainImage.setImageResource(R.drawable.ic_grain_logo)
                }
            } else {
                binding.ivGrainImage.setImageResource(R.drawable.ic_grain_logo)
            }

            // Setup single delete action
            binding.btnDeleteItem.setOnClickListener {
                onDeleteClicked(item)
            }
        }
    }

    /**
     * DiffUtil Callback to calculate differences between lists efficiently.
     */
    class HistoryDiffCallback : DiffUtil.ItemCallback<ClassificationHistory>() {
        override fun areItemsTheSame(oldItem: ClassificationHistory, newItem: ClassificationHistory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ClassificationHistory, newItem: ClassificationHistory): Boolean {
            return oldItem == newItem
        }
    }
}
