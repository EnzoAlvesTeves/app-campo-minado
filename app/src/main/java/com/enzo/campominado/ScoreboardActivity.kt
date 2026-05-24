package com.enzo.campominado

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import org.json.JSONArray
import org.json.JSONObject

class ScoreboardActivity : AppCompatActivity() {

    private var currentPlayerName = "Jogador"
    private var currentPlayerAvatar = "👦"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scoreboard)
        
        val mainView = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, StartupActivity::class.java))
            finish()
        }

        val rvScores: RecyclerView = findViewById(R.id.recyclerViewScores)
        rvScores.layoutManager = LinearLayoutManager(this)

        val prefs = getSharedPreferences("Scores", Context.MODE_PRIVATE)
        
        currentPlayerName = intent.getStringExtra("PLAYER_NAME") ?: prefs.getString("LAST_PLAYER_NAME", "Jogador") ?: "Jogador"
        currentPlayerAvatar = intent.getStringExtra("PLAYER_AVATAR") ?: prefs.getString("LAST_PLAYER_AVATAR", "👦") ?: "👦"

        val isFromGame = intent.getBooleanExtra("FROM_GAME", false)
        if (isFromGame) {
            val win = intent.getBooleanExtra("WIN", false)
            val time = intent.getIntExtra("TIME", 0)
            val difficulty = intent.getStringExtra("DIFFICULTY") ?: "Fácil"
            val points = intent.getIntExtra("POINTS", 0)

            saveScore(currentPlayerName, time, difficulty, win, points, currentPlayerAvatar)
        }

        val scores = getScores().sortedByDescending { it.points }
        rvScores.adapter = ScoreAdapter(scores)

        findViewById<Button>(R.id.btnPlayAgain).setOnClickListener {
            showDifficultyDialog()
        }
    }

    private fun showDifficultyDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_difficulty, null)
        val dialog = AlertDialog.Builder(this, R.style.Theme_CampoMinado_Dialog)
            .setView(view)
            .create()

        view.findViewById<Button>(R.id.btnEasy).setOnClickListener {
            startGame(8, 8, 10, "Fácil")
            dialog.dismiss()
        }
        view.findViewById<Button>(R.id.btnMedium).setOnClickListener {
            startGame(10, 10, 20, "Médio")
            dialog.dismiss()
        }
        view.findViewById<Button>(R.id.btnHard).setOnClickListener {
            startGame(12, 12, 35, "Difícil")
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun startGame(rows: Int, cols: Int, mines: Int, difficulty: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("ROWS", rows)
            putExtra("COLS", cols)
            putExtra("MINES", mines)
            putExtra("DIFFICULTY", difficulty)
            putExtra("PLAYER_NAME", currentPlayerName)
            putExtra("PLAYER_AVATAR", currentPlayerAvatar)
        }
        startActivity(intent)
        finish()
    }

    private fun saveScore(name: String, time: Int, difficulty: String, win: Boolean, points: Int, avatar: String) {
        val prefs = getSharedPreferences("Scores", Context.MODE_PRIVATE)
        val scoresList = getScores().toMutableList()
        scoresList.add(Score(name, time, difficulty, win, points, avatar))
        
        val jsonArray = JSONArray()
        val topScores = scoresList.sortedByDescending { it.points }.take(50)
        
        topScores.forEach {
            val obj = JSONObject()
            obj.put("name", it.name)
            obj.put("time", it.time)
            obj.put("difficulty", it.difficulty)
            obj.put("win", it.win)
            obj.put("points", it.points)
            obj.put("avatar", it.avatar)
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
                obj.optInt("points", 0),
                obj.optString("avatar", "👦")
            ))
        }
        return scores
    }
}
