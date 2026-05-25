package com.enzo.campominado

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScoreboardActivity : AppCompatActivity() {

    private var currentPlayerName = "Jogador"
    private var currentPlayerAvatar = "👦"
    private lateinit var rvScores: RecyclerView

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

        rvScores = findViewById(R.id.recyclerViewScores)
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

            val newScore = Score(currentPlayerName, time, difficulty, win, points, currentPlayerAvatar)
            saveScoreToApi(newScore)
        } else {
            loadScoresFromApi()
        }

        findViewById<Button>(R.id.btnPlayAgain).setOnClickListener {
            showDifficultyDialog()
        }
    }

    private fun loadScoresFromApi() {
        RetrofitClient.instance.getScores().enqueue(object : Callback<List<Score>> {
            override fun onResponse(call: Call<List<Score>>, response: Response<List<Score>>) {
                if (response.isSuccessful) {
                    val scores = response.body() ?: emptyList()
                    rvScores.adapter = ScoreAdapter(scores)
                } else {
                    Toast.makeText(this@ScoreboardActivity, "Erro ao carregar ranking", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Score>>, t: Throwable) {
                Log.e("API", "Erro: ${t.message}")
                Toast.makeText(this@ScoreboardActivity, "Sem conexão com servidor", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveScoreToApi(score: Score) {
        RetrofitClient.instance.saveScore(score).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                loadScoresFromApi() // Recarrega a lista após salvar
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Log.e("API", "Erro ao salvar: ${t.message}")
                loadScoresFromApi() // Tenta carregar mesmo se falhar o save
            }
        })
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
}
