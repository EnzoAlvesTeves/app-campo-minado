package com.enzo.campominado

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class StartupActivity : AppCompatActivity() {

    private val avatars = listOf("👦", "👧", "👨", "👩", "🧔", "👩‍🦰", "👱‍♂️", "👱‍♀️", "🤠", "👩‍🚀", "💂‍♂️", "👨‍🚀")
    private var selectedAvatar = avatars[0]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_startup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val rvAvatars = findViewById<RecyclerView>(R.id.rvAvatars)
        val editName = findViewById<EditText>(R.id.editName)
        val btnPlay = findViewById<Button>(R.id.btnPlay)

        val prefs = getSharedPreferences("Scores", Context.MODE_PRIVATE)
        val lastSavedName = prefs.getString("LAST_PLAYER_NAME", "Jogador")
        selectedAvatar = prefs.getString("LAST_PLAYER_AVATAR", avatars[0]) ?: avatars[0]
        editName.setText(lastSavedName)

        rvAvatars.layoutManager = GridLayoutManager(this, 4)
        rvAvatars.adapter = AvatarAdapter(avatars) { avatar ->
            selectedAvatar = avatar
        }

        btnPlay.setOnClickListener {
            val name = editName.text.toString().ifBlank { "Jogador" }
            saveUserData(name, selectedAvatar)
            showDifficultyDialog(name, selectedAvatar)
        }
    }

    private fun showDifficultyDialog(name: String, avatar: String) {
        val view = layoutInflater.inflate(R.layout.dialog_difficulty, null)
        val dialog = AlertDialog.Builder(this, R.style.Theme_CampoMinado_Dialog)
            .setView(view)
            .create()

        view.findViewById<Button>(R.id.btnEasy).setOnClickListener {
            startGame(8, 8, 10, "Fácil", name, avatar)
            dialog.dismiss()
        }
        view.findViewById<Button>(R.id.btnMedium).setOnClickListener {
            startGame(10, 10, 20, "Médio", name, avatar)
            dialog.dismiss()
        }
        view.findViewById<Button>(R.id.btnHard).setOnClickListener {
            startGame(12, 12, 35, "Difícil", name, avatar)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun startGame(rows: Int, cols: Int, mines: Int, difficulty: String, name: String, avatar: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("ROWS", rows)
            putExtra("COLS", cols)
            putExtra("MINES", mines)
            putExtra("DIFFICULTY", difficulty)
            putExtra("PLAYER_NAME", name)
            putExtra("PLAYER_AVATAR", avatar)
        }
        startActivity(intent)
    }

    private fun saveUserData(name: String, avatar: String) {
        val prefs = getSharedPreferences("Scores", Context.MODE_PRIVATE)
        prefs.edit().putString("LAST_PLAYER_NAME", name)
             .putString("LAST_PLAYER_AVATAR", avatar)
             .apply()
    }
}
