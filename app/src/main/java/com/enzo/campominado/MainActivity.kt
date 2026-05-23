package com.enzo.campominado

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.GridLayout
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

    private lateinit var gridLayout: GridLayout
    private lateinit var statusText: TextView
    private lateinit var restartButton: MaterialButton
    private lateinit var mineCountText: TextView
    private lateinit var timerText: TextView
    private lateinit var toolbar: MaterialToolbar
    
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
                timerText.text = "Tempo: $timeSeconds"
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
        statusText = findViewById(R.id.statusText)
        restartButton = findViewById(R.id.restartButton)
        mineCountText = findViewById(R.id.mineCountText)
        timerText = findViewById(R.id.timerText)

        // Carregar parâmetros da dificuldade e jogador
        rows = intent.getIntExtra("ROWS", 8)
        cols = intent.getIntExtra("COLS", 8)
        minesCount = intent.getIntExtra("MINES", 10)
        difficultyName = intent.getStringExtra("DIFFICULTY") ?: "Fácil"
        playerName = intent.getStringExtra("PLAYER_NAME") ?: "Jogador"

        restartButton.setOnClickListener {
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
        
        statusText.text = "$playerName - $difficultyName"
        mineCountText.text = "Minas: $minesCount"
        timerText.text = "Tempo: 0"
        
        gridLayout.removeAllViews()
        gridLayout.columnCount = cols
        gridLayout.rowCount = rows
        
        // Inicializar matrizes
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

        // Criar botões no GridLayout
        val displayMetrics = resources.displayMetrics
        val availableWidth = displayMetrics.widthPixels - (32 * displayMetrics.density).toInt()
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
                    textSize = if (cols > 10) 12f else 16f
                    insetTop = 0
                    insetBottom = 0
                    cornerRadius = 0
                    strokeWidth = 1
                    strokeColor = ColorStateList.valueOf(Color.DKGRAY)
                    backgroundTintList = ColorStateList.valueOf(Color.LTGRAY)
                    
                    setOnClickListener {
                        onCellClick(i, j)
                    }
                    
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
            button.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            button.strokeColor = ColorStateList.valueOf(Color.LTGRAY)
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
        
        mineCountText.text = "Minas: ${minesCount - flagsCount}"
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
        
        if (win) {
            statusText.text = "Você venceu!"
            Toast.makeText(this, "Parabéns!", Toast.LENGTH_SHORT).show()
        } else {
            statusText.text = "Você perdeu!"
            Toast.makeText(this, "Game Over!", Toast.LENGTH_SHORT).show()
            revealAllMines()
        }

        // Redirecionar para o placar após 2 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, ScoreboardActivity::class.java).apply {
                putExtra("WIN", win)
                putExtra("TIME", timeSeconds)
                putExtra("DIFFICULTY", difficultyName)
                putExtra("PLAYER_NAME", playerName)
                putExtra("REVEALED", revealedCount)
                putExtra("TOTAL", (rows * cols) - minesCount)
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
                if (r in 0 until rows && c in 0 until cols && mines[r][c]) {
                    count++
                }
            }
        }
        return count
    }

    private fun revealNeighbors(row: Int, col: Int) {
        for (i in -1..1) {
            for (j in -1..1) {
                val r = row + i
                val c = col + j
                if (r in 0 until rows && c in 0 until cols && !revealed[r][c]) {
                    onCellClick(r, c)
                }
            }
        }
    }

    private fun getNumberColor(count: Int): Int {
        return when (count) {
            1 -> Color.BLUE
            2 -> Color.parseColor("#388E3C") // Verde
            3 -> Color.RED
            4 -> Color.parseColor("#191970") // Azul escuro
            5 -> Color.parseColor("#8B0000") // Marrom/Vinho
            6 -> Color.CYAN
            7 -> Color.BLACK
            8 -> Color.GRAY
            else -> Color.BLACK
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

        if (revealedCount == (rows * cols) - minesCount) {
            endGame(true)
        }
    }
}
