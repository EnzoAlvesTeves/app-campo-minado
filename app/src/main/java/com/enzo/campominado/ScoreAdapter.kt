package com.enzo.campominado

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ScoreAdapter(private val scores: List<Score>) : RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder>() {

    class ScoreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textRank: TextView = view.findViewById(R.id.textRank)
        val textAvatar: TextView = view.findViewById(R.id.textAvatar)
        val textName: TextView = view.findViewById(R.id.textName)
        val textTime: TextView = view.findViewById(R.id.textTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_score, parent, false)
        return ScoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) {
        val score = scores[position]
        val rank = position + 1
        
        holder.textRank.text = when (rank) {
            1 -> "🥇"
            2 -> "🥈"
            3 -> "🥉"
            else -> rank.toString()
        }
        
        holder.textAvatar.text = score.avatar
        holder.textName.text = score.name
        
        // Formatar tempo MM:SS
        val minutes = score.time / 60
        val seconds = score.time % 60
        holder.textTime.text = String.format("%02d:%02d", minutes, seconds)
        
        if (score.win) {
            holder.textTime.setTextColor(Color.parseColor("#388E3C"))
        } else {
            holder.textTime.setTextColor(Color.GRAY)
        }
    }

    override fun getItemCount() = scores.size
}