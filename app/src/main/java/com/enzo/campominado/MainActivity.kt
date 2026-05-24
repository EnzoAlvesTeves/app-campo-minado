package com.enzo.campominado

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private var rows = 8
    private var cols = 8
    private var minesCount = 10
    private var difficultyName = "Fácil"
    private var playerName = "Jogador"
    private var playerAvatar = "👦"

    private lateinit var gridLayout: GridLayout
    private lateinit var mineCountText: TextView
    private lateinit var timerText: TextView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var btnRestartHeader: LinearLayout
    
    private lateinit var mines: Array<BooleanArray>
    private lateinit var revealed: Array<BooleanArray>
    private lateinit var flagged: Array<BooleanArray>
    
    private var gameOver = false
    private var firstClick = true
    private var flagsCount = 0
    private var timeSeconds = 0
    private var timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (!gameOver) {
                timeSeconds++
                val minutes = timeSeconds / 60
                val seconds = timeSeconds % 60
                timerText.text = String.format("%02d:%02d", minutes, seconds)
                timerHandler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        gridLayout = findViewById(R.id.gridLayout)
        mineCountText = findViewById(R.id.mineCountText)
        timerText = findViewById(R.id.timerText)
        btnRestartHeader = findViewById(R.id.btnRestartHeader)

        // Carregar parâmetros
        rows = intent.getIntExtra("ROWS", 8)
        cols = intent.getIntExtra("COLS", 8)
        minesCount = intent.getIntExtra("MINES", 10)
        difficultyName = intent.getStringExtra("DIFFICULTY") ?: "Fácil"
        playerName = intent.getStringExtra("PLAYER_NAME") ?: "Jogador"
        playerAvatar = intent.getStringExtra("PLAYER_AVATAR") ?: "👦"

        btnRestartHeader.setOnClickListener {
            startGame()
        }

        startGame()
    }

    private fun startGame() {
        gameOver = false
        firstClick = true
        flagsCount = 0
        timeSeconds = 0
        timerHandler.removeCallbacks(timerRunnable)
        
        mineCountText.text = "$minesCount"
        timerText.text = "00:00"
        
        gridLayout.removeAllViews()
        gridLayout.columnCount = cols
        gridLayout.rowCount = rows
        
        mines = Array(rows) { BooleanArray(cols) }
        revealed = Array(rows) { BooleanArray(cols) }
        flagged = Array(rows) { BooleanArray(cols) }

        var placedMines = 0
        while (placedMines < minesCount) {
            val r = Random.nextInt(rows)
            val c = Random.nextInt(cols)
            if (!mines[r][c]) {
                mines[r][c] = true
                placedMines++
            }
        }

        val displayMetrics = resources.displayMetrics
        val availableWidth = displayMetrics.widthPixels - (64 * displayMetrics.density).toInt()
        val size = (availableWidth / cols).coerceAtMost((48 * displayMetrics.density).toInt())

        for (i in 0 until rows) {
            for (j in 0 until cols) {
                val button = MaterialButton(this).apply {
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = size
                        height = size
                        setMargins(1, 1, 1, 1)
                    }
                    setPadding(0, 0, 0, 0)
                    minWidth = 0
                    minHeight = 0
                    textSize = if (cols > 10) 14f else 18f
                    insetTop = 0
                    insetBottom = 0
                    cornerRadius = 4
                    strokeWidth = 1
                    strokeColor = ColorStateList.valueOf(Color.parseColor("#2D3748"))
                    backgroundTintList = ColorStateList.valueOf(Color.parseColor("#334155")) // Cor das células fechadas
                    
                    setOnClickListener { onCellClick(i, j) }
                    setOnLongClickListener {
                        onCellLongClick(i, j)
                        true
                    }
                }
                gridLayout.addView(button)
            }
        }
    }

    private fun onCellClick(row: Int, col: Int) {
        if (gameOver || revealed[row][col] || flagged[row][col]) return

        if (firstClick) {
            firstClick = false
            timerHandler.postDelayed(timerRunnable, 1000)
        }

        revealed[row][col] = true
        val index = row * cols + col
        val button = gridLayout.getChildAt(index) as MaterialButton

        if (mines[row][col]) {
            button.text = "💣"
            button.backgroundTintList = ColorStateList.valueOf(Color.RED)
            endGame(false)
        } else {
            val count = countAdjacentMines(row, col)
            button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#94A3B8")) // Cor da célula aberta
            button.strokeColor = ColorStateList.valueOf(Color.parseColor("#1C242D"))
            if (count > 0) {
                button.text = count.toString()
                button.setTextColor(getNumberColor(count))
            } else {
                button.text = ""
                revealNeighbors(row, col)
            }
            checkWin()
        }
    }

    private fun onCellLongClick(row: Int, col: Int) {
        if (gameOver || revealed[row][col]) return
        val index = row * cols + col
        val button = gridLayout.getChildAt(index) as MaterialButton
        if (flagged[row][col]) {
            flagged[row][col] = false
            button.text = ""
            flagsCount--
        } else {
            flagged[row][col] = true
            button.text = "🚩"
            flagsCount++
        }
        mineCountText.text = "${minesCount - flagsCount}"
    }

    private fun endGame(win: Boolean) {
        gameOver = true
        timerHandler.removeCallbacks(timerRunnable)

        var revealedCount = 0
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                if (revealed[i][j]) revealedCount++
            }
        }
        
        val difficultyMultiplier = when(difficultyName) {
            "Médio" -> 1.5
            "Difícil" -> 2.0
            else -> 1.0
        }
        
        var points = (revealedCount * 100 * difficultyMultiplier).toInt()
        if (win) {
            val timeBonus = (500 - timeSeconds).coerceAtLeast(0) * 10
            points += (5000 * difficultyMultiplier).toInt() + timeBonus
        }

        if (!win) revealAllMines()

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, ScoreboardActivity::class.java).apply {
                putExtra("WIN", win)
                putExtra("TIME", timeSeconds)
                putExtra("DIFFICULTY", difficultyName)
                putExtra("PLAYER_NAME", playerName)
                putExtra("PLAYER_AVATAR", playerAvatar)
                putExtra("POINTS", points)
                putExtra("FROM_GAME", true)
            }
            startActivity(intent)
            finish()
        }, 2500)
    }

    private fun countAdjacentMines(row: Int, col: Int): Int {
        var count = 0
        for (i in -1..1) {
            for (j in -1..1) {
                val r = row + i
                val c = col + j
                if (r in 0 until rows && c in 0 until cols && mines[r][c]) count++
            }
        }
        return count
    }

    private fun revealNeighbors(row: Int, col: Int) {
        for (i in -1..1) {
            for (j in -1..1) {
                val r = row + i
                val c = col + j
                if (r in 0 until rows && c in 0 until cols && !revealed[r][c]) onCellClick(r, c)
            }
        }
    }

    private fun getNumberColor(count: Int): Int {
        return when (count) {
            1 -> Color.parseColor("#3B82F6") // Azul
            2 -> Color.parseColor("#10B981") // Verde
            3 -> Color.parseColor("#EF4444") // Vermelho
            else -> Color.WHITE
        }
    }

    private fun revealAllMines() {
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                if (mines[i][j]) {
                    val index = i * cols + j
                    val button = gridLayout.getChildAt(index) as MaterialButton
                    button.text = "💣"
                    button.backgroundTintList = ColorStateList.valueOf(Color.RED)
                }
            }
        }
    }

    private fun checkWin() {
        var revealedCount = 0
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                if (revealed[i][j]) revealedCount++
            }
        }
        if (revealedCount == (rows * cols) - minesCount) endGame(true)
    }
}
