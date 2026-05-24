package com.enzo.campominado

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AvatarAdapter(
    private val avatars: List<String>,
    private val onAvatarSelected: (String) -> Unit
) : RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder>() {

    private var selectedPosition = 0

    class AvatarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAvatar: TextView = view.findViewById(R.id.tvAvatar)
        val ivSelected: ImageView = view.findViewById(R.id.ivSelected)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_avatar, parent, false)
        return AvatarViewHolder(view)
    }

    override fun onBindViewHolder(holder: AvatarViewHolder, position: Int) {
        val avatar = avatars[position]
        holder.tvAvatar.text = avatar
        
        val isSelected = position == selectedPosition
        holder.tvAvatar.isSelected = isSelected
        holder.ivSelected.visibility = if (isSelected) View.VISIBLE else View.GONE
        
        holder.itemView.setOnClickListener {
            val oldPos = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(oldPos)
            notifyItemChanged(selectedPosition)
            onAvatarSelected(avatar)
        }
    }

    override fun getItemCount() = avatars.size
}