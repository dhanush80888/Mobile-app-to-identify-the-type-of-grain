package com.example.grainclassifier.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.grainclassifier.databinding.ItemSupportedGrainBinding

data class SupportedGrain(
    val id: Int,
    val name: String,
    val description: String,
    val imageResId: Int,
    val badgeText: String,
    val badgeColorResId: Int,
    val botanicalName: String,
    val details: String
)

class SupportedGrainsAdapter(
    private val grains: List<SupportedGrain>,
    private val onItemClick: (SupportedGrain) -> Unit
) : RecyclerView.Adapter<SupportedGrainsAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemSupportedGrainBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(grain: SupportedGrain) {
            binding.tvGrainName.text = grain.name
            binding.tvGrainDesc.text = grain.description
            binding.ivGrain.setImageResource(grain.imageResId)
            binding.tvBadge.text = grain.badgeText
            binding.badgeContainer.setCardBackgroundColor(binding.root.context.getColor(grain.badgeColorResId))

            binding.root.setOnClickListener {
                onItemClick(grain)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSupportedGrainBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(grains[position])
    }

    override fun getItemCount(): Int = grains.size
}
