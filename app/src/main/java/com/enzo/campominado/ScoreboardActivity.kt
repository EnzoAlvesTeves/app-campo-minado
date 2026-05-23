package com.enzo.campominado

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONObject

class ScoreboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scoreboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val lastResultText = findViewById<TextView>(R.id.lastResultText)
        val editPlayerName = findViewById<TextInputEditText>(R.id.editPlayerName)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewScores)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Recuperar último nome usado
        val prefs = getSharedPreferences("Scores", Context.MODE_PRIVATE)
        val lastSavedName = prefs.getString("LAST_PLAYER_NAME", "")
        editPlayerName.setText(lastSavedName)

        // Receber resultado da última partida
        val isFromGame = intent.getBooleanExtra("FROM_GAME", false)
        if (isFromGame) {
            val win = intent.getBooleanExtra("WIN", false)
            val time = intent.getIntExtra("TIME", -1)
            val difficulty = intent.getStringExtra("DIFFICULTY") ?: "Fácil"
            val name = intent.getStringExtra("PLAYER_NAME") ?: "Jogador"
            val revealedCount = intent.getIntExtra("REVEALED", 0)

            // Cálculo de Pontuação:
            // 1. Base por progresso (cada célula aberta vale 100 pontos)
            // 2. Multiplicador por dificuldade: Fácil x1, Médio x1.5, Difícil x2
            // 3. Bônus de Vitória: 5000 pontos fixos + Bônus de Tempo ((500 - tempo) * 10)
            
            val difficultyMultiplier = when(difficulty) {
                "Médio" -> 1.5
                "Difícil" -> 2.0
                else -> 1.0
            }
            
            var points = (revealedCount * 100 * difficultyMultiplier).toInt()
            
            if (win) {
                val timeBonus = (500 - time).coerceAtLeast(0) * 10
                points += (5000 * difficultyMultiplier).toInt() + timeBonus
                lastResultText.text = "🎉 Vitória! Pontos: $points"
            } else {
                lastResultText.text = "💣 Explosão! Pontos: $points"
            }

            saveScore(name, time, difficulty, win, points)
        }

        updateScoresList(recyclerView)

        findViewById<Button>(R.id.btnStartEasy).setOnClickListener { 
            val name = editPlayerName.text.toString().ifBlank { "Jogador" }
            saveLastPlayerName(name)
            startGame(8, 8, 10, "Fácil", name) 
        }
        findViewById<Button>(R.id.btnStartMedium).setOnClickListener { 
            val name = editPlayerName.text.toString().ifBlank { "Jogador" }
            saveLastPlayerName(name)
            startGame(10, 10, 20, "Médio", name) 
        }
        findViewById<Button>(R.id.btnStartHard).setOnClickListener { 
            val name = editPlayerName.text.toString().ifBlank { "Jogador" }
            saveLastPlayerName(name)
            startGame(12, 12, 35, "Difícil", name) 
        }
    }

    private fun saveLastPlayerName(name: String) {
        val prefs = getSharedPreferences("Scores", Context.MODE_PRIVATE)
        prefs.edit().putString("LAST_PLAYER_NAME", name).apply()
    }

    private fun updateScoresList(recyclerView: RecyclerView) {
        val scores = getScores().sortedByDescending { it.points }
        recyclerView.adapter = ScoreAdapter(scores)
    }

    private fun startGame(rows: Int, cols: Int, mines: Int, difficulty: String, playerName: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("ROWS", rows)
            putExtra("COLS", cols)
            putExtra("MINES", mines)
            putExtra("DIFFICULTY", difficulty)
            putExtra("PLAYER_NAME", playerName)
        }
        startActivity(intent)
        finish()
    }

    private fun saveScore(name: String, time: Int, difficulty: String, win: Boolean, points: Int) {
        val prefs = getSharedPreferences("Scores", Context.MODE_PRIVATE)
        val scoresList = getScores().toMutableList()
        scoresList.add(Score(name, time, difficulty, win, points))
        
        val jsonArray = JSONArray()
        // Manter apenas os top 50 scores
        val topScores = scoresList.sortedByDescending { it.points }.take(50)
        
        topScores.forEach {
            val obj = JSONObject()
            obj.put("name", it.name)
            obj.put("time", it.time)
            obj.put("difficulty", it.difficulty)
            obj.put("win", it.win)
            obj.put("points", it.points)
            jsonArray.put(obj)
        }
        
        prefs.edit().putString("SCORES_JSON", jsonArray.toString()).apply()
    }

    private fun getScores(): List<Score> {
        val prefs = getSharedPreferences("Scores", Context.MODE_PRIVATE)
        val jsonString = prefs.getString("SCORES_JSON", null) ?: return emptyList()
        val jsonArray = JSONArray(jsonString)
        val scores = mutableListOf<Score>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            scores.add(Score(
                obj.getString("name"),
                obj.getInt("time"),
                obj.getString("difficulty"),
                obj.optBoolean("win", true),
                obj.optInt("points", 0)
            ))
        }
        return scores
    }
}
