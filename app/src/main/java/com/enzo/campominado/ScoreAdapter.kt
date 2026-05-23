package com.enzo.campominado

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Score(
    val name: String,
    val time: Int,
    val difficulty: String,
    val win: Boolean,
    val points: Int
)

class ScoreAdapter(private val scores: List<Score>) : RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder>() {

    class ScoreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textDifficulty: TextView = view.findViewById(R.id.textDifficulty)
        val textName: TextView = view.findViewById(R.id.textName)
        val textTime: TextView = view.findViewById(R.id.textTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_score, parent, false)
        return ScoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) {
        val score = scores[position]
        holder.textDifficulty.text = score.difficulty
        holder.textName.text = if (score.win) "🏆 ${score.name}" else "💀 ${score.name}"
        holder.textTime.text = "${score.points} pts"
        
        if (score.win) {
            holder.textTime.setTextColor(Color.parseColor("#388E3C")) // Verde
        } else {
            holder.textTime.setTextColor(Color.GRAY)
        }
    }

    override fun getItemCount() = scores.size
}