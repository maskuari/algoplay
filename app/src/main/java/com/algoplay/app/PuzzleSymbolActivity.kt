package com.algoplay.app

import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.DragEvent
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth

class PuzzleSymbolActivity : AppCompatActivity() {

    private lateinit var txtPuzzleTitle: TextView
    private lateinit var txtPuzzleProgress: TextView
    private lateinit var txtPuzzleTimer: TextView
    private lateinit var txtPuzzleDifficulty: TextView
    private lateinit var txtPuzzleTarget: TextView
    private lateinit var imgPuzzleHero: ImageView
    private lateinit var imgPuzzleTargetVisual: ImageView
    private lateinit var txtPuzzleQuestion: TextView
    private lateinit var txtPuzzleFeedback: TextView
    private lateinit var answerContainer: LinearLayout
    private lateinit var resultPanel: LinearLayout
    private lateinit var txtPuzzleResultTitle: TextView
    private lateinit var txtPuzzleResultDetail: TextView
    private lateinit var imgPuzzleResult: ImageView
    private lateinit var btnPuzzleFinish: TextView

    private val handler = Handler(Looper.getMainLooper())
    private val repository = PuzzleSymbolRepository()
    private var timer: CountDownTimer? = null
    private var questions = emptyList<PuzzlePatternQuestion>()
    private var difficulty = PuzzleDifficulty.EASY
    private var currentIndex = 0
    private var correctAnswer = 0
    private var isGuestMode = false
    private var userId = ""
    private var isFinishingSession = false
    private var awardedPointsForResult = 0
    private val placedBlocks = mutableMapOf<Int, String>()
    private val draggedViews = mutableListOf<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puzzle_symbol)

        isGuestMode = intent.getBooleanExtra(EXTRA_GUEST_MODE, false)
        userId = intent.getStringExtra(EXTRA_USER_ID)
            ?: FirebaseAuth.getInstance().currentUser?.uid
            ?: ""

        bindViews()
        btnPuzzleFinish.enableTapFeedback()
        btnPuzzleFinish.setOnClickListener { finishWithResult() }
        showLevelDialog()
    }

    override fun onDestroy() {
        timer?.cancel()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun bindViews() {
        txtPuzzleTitle = findViewById(R.id.txtPuzzleTitle)
        txtPuzzleProgress = findViewById(R.id.txtPuzzleProgress)
        txtPuzzleTimer = findViewById(R.id.txtPuzzleTimer)
        txtPuzzleDifficulty = findViewById(R.id.txtPuzzleDifficulty)
        txtPuzzleTarget = findViewById(R.id.txtPuzzleTarget)
        imgPuzzleHero = findViewById(R.id.imgPuzzleHero)
        imgPuzzleTargetVisual = findViewById(R.id.imgPuzzleTargetVisual)
        txtPuzzleQuestion = findViewById(R.id.txtPuzzleQuestion)
        txtPuzzleFeedback = findViewById(R.id.txtPuzzleFeedback)
        answerContainer = findViewById(R.id.puzzleAnswerContainer)
        resultPanel = findViewById(R.id.puzzleResultPanel)
        txtPuzzleResultTitle = findViewById(R.id.txtPuzzleResultTitle)
        txtPuzzleResultDetail = findViewById(R.id.txtPuzzleResultDetail)
        imgPuzzleResult = findViewById(R.id.imgPuzzleResult)
        btnPuzzleFinish = findViewById(R.id.btnPuzzleFinish)
    }

    private fun showLevelDialog() {
        val dialog = Dialog(this)
        dialog.setCancelable(false)
        val root = FrameLayout(this).apply {
            setPadding(dp(22), 0, dp(22), 0)
        }
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            background = roundedDrawable(ContextCompat.getColor(this@PuzzleSymbolActivity, R.color.white), dp(26))
            elevation = dp(10).toFloat()
            setPadding(dp(18), dp(18), dp(18), dp(18))
            alpha = 0f
            scaleX = 0.92f
            scaleY = 0.92f
        }
        root.addView(card, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        ))

        val image = ImageView(this).apply {
            setImageResource(R.drawable.menantang_latihan)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        card.addView(image, LinearLayout.LayoutParams(dp(112), dp(112)))

        card.addView(textView("Pilih Level", 22, R.color.algoplay_text, true, Gravity.CENTER), LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(8)
        })

        card.addView(textView("Tiru pola balok 5x5. Level menentukan batas waktu dan bonus jika skor 100.", 13, R.color.algoplay_subtext, false, Gravity.CENTER), LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(8)
        })

        listOf(PuzzleDifficulty.EASY, PuzzleDifficulty.MEDIUM, PuzzleDifficulty.HARD).forEach { level ->
            card.addView(levelButton(level).apply {
                setOnClickListener {
                    dialog.dismiss()
                    startSession(level)
                }
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(58)
            ).apply {
                topMargin = dp(10)
            })
        }

        dialog.setContentView(root)
        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setDimAmount(0.42f)
            dialog.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            card.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(260)
                .setInterpolator(OvershootInterpolator(1.05f))
                .start()
        }
        dialog.show()
    }

    private fun levelButton(level: PuzzleDifficulty): TextView {
        val detail = when (level) {
            PuzzleDifficulty.EASY -> "3 menit per soal - tanpa bonus"
            PuzzleDifficulty.MEDIUM -> "2 menit per soal - bonus +20 jika skor 100"
            PuzzleDifficulty.HARD -> "30 detik per soal - bonus +50 jika skor 100"
        }
        return textView("${level.label}\n$detail", 13, R.color.algoplay_text, true, Gravity.CENTER).apply {
            setLineSpacing(dp(2).toFloat(), 1f)
            enableTapFeedback()
            background = roundedStrokeDrawable(
                ContextCompat.getColor(this@PuzzleSymbolActivity, R.color.white),
                ContextCompat.getColor(this@PuzzleSymbolActivity, R.color.algoplay_blue_soft),
                dp(18)
            )
        }
    }

    private fun startSession(level: PuzzleDifficulty) {
        difficulty = level
        questions = PuzzleSymbolEngine.getRandomPuzzlePatternQuestions()
        currentIndex = 0
        correctAnswer = 0
        isFinishingSession = false
        resultPanel.visibility = View.GONE
        btnPuzzleFinish.visibility = View.GONE
        txtPuzzleDifficulty.text = "Level ${difficulty.label}"
        txtPuzzleTitle.text = "Puzzle Simbol"
        imgPuzzleHero.setImageResource(R.drawable.puzzle_latihan)
        renderQuestion()
    }

    private fun renderQuestion() {
        timer?.cancel()
        if (currentIndex >= questions.size) {
            finishSession()
            return
        }

        val question = questions[currentIndex]
        placedBlocks.clear()
        draggedViews.clear()
        txtPuzzleProgress.text = "Soal ${currentIndex + 1} dari ${questions.size}"
        txtPuzzleTarget.text = question.title
        imgPuzzleTargetVisual.setImageResource(puzzleVisualFor(currentIndex))
        txtPuzzleQuestion.text = question.question
        txtPuzzleFeedback.visibility = View.GONE
        answerContainer.removeAllViews()

        answerContainer.addView(label("Contoh pola"), LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(4)
        })
        answerContainer.addView(createPatternGrid(question.blocks, readOnly = true), LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(8)
        })

        answerContainer.addView(label("Susun jawabanmu"), LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(16)
        })
        answerContainer.addView(createAnswerGrid(), LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(8)
        })

        answerContainer.addView(label("Balok yang bisa diseret"), LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(16)
        })
        answerContainer.addView(createBlockTray(question.blocks.shuffled()), LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(8)
        })

        val actionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        actionRow.addView(actionButton("Ulangi Susunan", false).apply {
            setOnClickListener { renderQuestionWithoutRestartingTimer() }
        }, LinearLayout.LayoutParams(0, dp(46), 1f).apply {
            marginEnd = dp(6)
        })
        actionRow.addView(actionButton("Cek Susunan", true).apply {
            setOnClickListener { checkPatternAnswer() }
        }, LinearLayout.LayoutParams(0, dp(46), 1f).apply {
            marginStart = dp(6)
        })
        answerContainer.addView(actionRow, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(14)
        })

        startTimer()
    }

    private fun renderQuestionWithoutRestartingTimer() {
        val currentTimerText = txtPuzzleTimer.text
        val question = questions[currentIndex]
        placedBlocks.clear()
        draggedViews.clear()
        txtPuzzleFeedback.visibility = View.GONE
        answerContainer.removeAllViews()

        answerContainer.addView(label("Contoh pola"))
        answerContainer.addView(createPatternGrid(question.blocks, readOnly = true), LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(8)
        })
        answerContainer.addView(label("Susun jawabanmu"), LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(16)
        })
        answerContainer.addView(createAnswerGrid(), LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(8)
        })
        answerContainer.addView(label("Balok yang bisa diseret"), LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(16)
        })
        answerContainer.addView(createBlockTray(question.blocks.shuffled()), LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(8)
        })

        val actionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        actionRow.addView(actionButton("Ulangi Susunan", false).apply {
            setOnClickListener { renderQuestionWithoutRestartingTimer() }
        }, LinearLayout.LayoutParams(0, dp(46), 1f).apply {
            marginEnd = dp(6)
        })
        actionRow.addView(actionButton("Cek Susunan", true).apply {
            setOnClickListener { checkPatternAnswer() }
        }, LinearLayout.LayoutParams(0, dp(46), 1f).apply {
            marginStart = dp(6)
        })
        answerContainer.addView(actionRow, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(14)
        })
        txtPuzzleTimer.text = currentTimerText
    }

    private fun createPatternGrid(blocks: List<PuzzleBlock>, readOnly: Boolean): LinearLayout {
        val blockMap = blocks.associateBy { cellIndex(it.row, it.col) }
        val grid = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        repeat(PuzzleSymbolEngine.PATTERN_GRID_SIZE) { row ->
            val rowView = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }
            repeat(PuzzleSymbolEngine.PATTERN_GRID_SIZE) { col ->
                val block = blockMap[cellIndex(row, col)]
                val cell = TextView(this).apply {
                    gravity = Gravity.CENTER
                    text = ""
                    tag = block?.colorHex
                    background = if (block == null) {
                        emptyCellDrawable()
                    } else {
                        coloredCellDrawable(block.colorHex)
                    }
                    if (!readOnly) setupDropTarget(this, row, col)
                }
                rowView.addView(cell, LinearLayout.LayoutParams(dp(42), dp(42)).apply {
                    marginStart = dp(2)
                    marginEnd = dp(2)
                    topMargin = dp(2)
                    bottomMargin = dp(2)
                })
            }
            grid.addView(rowView, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ))
        }
        return grid
    }

    private fun createAnswerGrid(): LinearLayout {
        return createPatternGrid(emptyList(), readOnly = false)
    }

    private fun setupDropTarget(cell: TextView, row: Int, col: Int) {
        cell.setOnDragListener { target, event ->
            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    val source = event.localState as? TextView ?: return@setOnDragListener false
                    val colorHex = event.clipData?.getItemAt(0)?.text?.toString().orEmpty()
                    val key = cellIndex(row, col)
                    if (placedBlocks.containsKey(key)) return@setOnDragListener false
                    placedBlocks[key] = colorHex
                    (target as TextView).tag = colorHex
                    target.background = coloredCellDrawable(colorHex)
                    source.visibility = View.INVISIBLE
                    true
                }
                else -> true
            }
        }
    }

    private fun createBlockTray(blocks: List<PuzzleBlock>): LinearLayout {
        val tray = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        blocks.chunked(5).forEach { rowBlocks ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }
            rowBlocks.forEach { block ->
                row.addView(draggableBlock(block.colorHex), LinearLayout.LayoutParams(dp(42), dp(42)).apply {
                    marginStart = dp(4)
                    marginEnd = dp(4)
                    topMargin = dp(4)
                    bottomMargin = dp(4)
                })
            }
            tray.addView(row, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ))
        }
        return tray
    }

    private fun draggableBlock(colorHex: String): TextView {
        return TextView(this).apply {
            text = ""
            tag = colorHex
            background = coloredCellDrawable(colorHex)
            draggedViews.add(this)
            setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    view.parent?.requestDisallowInterceptTouchEvent(true)
                    val shadow = View.DragShadowBuilder(view)
                    view.startDragAndDrop(ClipData.newPlainText("color", colorHex), shadow, view, 0)
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun checkPatternAnswer() {
        val question = questions[currentIndex]
        val targetBlocks = question.blocks.associate { cellIndex(it.row, it.col) to it.colorHex }
        val isComplete = placedBlocks.size == targetBlocks.size
        val isCorrect = isComplete && targetBlocks.all { (key, color) ->
            placedBlocks[key].equals(color, ignoreCase = true)
        }

        timer?.cancel()
        if (isCorrect) correctAnswer++
        val message = when {
            isCorrect -> "Polanya sama persis."
            isComplete -> "Posisi atau warnanya belum sama."
            else -> "Baloknya belum lengkap."
        }
        showFeedback(if (isCorrect) "Benar! $message" else "Salah. $message", isCorrect)
        showBriefResultPopup(
            title = if (isCorrect) "Benar!" else "Salah",
            message = message,
            imageRes = if (isCorrect) R.drawable.sorakan_leaderboard else R.drawable.hai_materi,
            success = isCorrect
        )
        answerContainer.disableChildren()
        handler.postDelayed({ goNext() }, 900)
    }

    private fun startTimer() {
        val totalMillis = difficulty.timeLimitSeconds * 1000L
        timer = object : CountDownTimer(totalMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                txtPuzzleTimer.text = formatTime(millisUntilFinished)
            }

            override fun onFinish() {
                txtPuzzleTimer.text = "00:00"
                playAlgoSound(AlgoSound.SALAH)
                showFeedback("Waktu habis. Kita lanjut ke soal berikutnya.", false)
                answerContainer.disableChildren()
                handler.postDelayed({ goNext() }, 900)
            }
        }.start()
    }

    private fun goNext() {
        currentIndex++
        renderQuestion()
    }

    private fun finishSession() {
        if (isFinishingSession) return
        isFinishingSession = true
        timer?.cancel()
        answerContainer.removeAllViews()
        txtPuzzleTimer.text = "--:--"
        txtPuzzleProgress.text = "Selesai"
        txtPuzzleTarget.text = "Hasil"
        imgPuzzleTargetVisual.setImageResource(R.drawable.sorakan_leaderboard)
        txtPuzzleQuestion.text = "Robot Algo sedang menghitung skor Puzzle Simbol kamu."

        if (isGuestMode || userId.isBlank()) {
            val output = PuzzleSymbolEngine.finishPuzzleSessionUpdate(
                userId = "guest",
                correctAnswer = correctAnswer,
                currentStats = PuzzleStats(),
                currentTotalLeaderboardScore = 0,
                todayDate = PuzzleSymbolEngine.todayDate(),
                difficulty = difficulty
            )
            showResult(output, saved = false)
            return
        }

        repository.finishSession(userId, correctAnswer, difficulty)
            .addOnSuccessListener { output ->
                showResult(output, saved = true)
            }
            .addOnFailureListener {
                val output = PuzzleSymbolEngine.finishPuzzleSessionUpdate(
                    userId = userId,
                    correctAnswer = correctAnswer,
                    currentStats = PuzzleStats(),
                    currentTotalLeaderboardScore = 0,
                    todayDate = PuzzleSymbolEngine.todayDate(),
                    difficulty = difficulty
                )
                Toast.makeText(this, "Data belum tersimpan. Cek koneksi/Firebase.", Toast.LENGTH_LONG).show()
                showResult(output, saved = false)
            }
    }

    private fun showResult(output: PuzzleSessionOutput, saved: Boolean) {
        resultPanel.visibility = View.VISIBLE
        btnPuzzleFinish.visibility = View.VISIBLE
        awardedPointsForResult = output.poinLeaderboardDidapat
        playAlgoSound(AlgoSound.SELESAI)
        imgPuzzleResult.setImageResource(if (output.jumlahBenar == 5) R.drawable.sorakan_leaderboard else R.drawable.hai_materi)
        txtPuzzleResultTitle.text = "Nilai ${output.skorSesi}"
        txtPuzzleResultDetail.text = buildString {
            append("Benar: ${output.jumlahBenar} / 5\n")
            append("Salah: ${output.jumlahSalah}\n")
            append("Skor sesi: ${output.skorSesi}\n")
            append("Bonus level: ${output.bonusLevel}\n")
            append("Poin leaderboard: ${output.poinLeaderboardDidapat}\n")
            append("Percobaan hari ini: ${output.jumlahPercobaanPuzzleHariIni}\n")
            append(if (output.statusBerpoin) "Status: Masih dapat poin harian" else "Status: Tidak menambah poin harian")
            if (!saved) append("\nData belum tersimpan ke akun.")
        }
    }

    private fun finishWithResult() {
        val score = PuzzleSymbolEngine.calculatePuzzleScore(correctAnswer)
        setResult(
            Activity.RESULT_OK,
            Intent()
                .putExtra(EXTRA_TRAINING_MODE, MODE_PUZZLE_SYMBOL)
                .putExtra(EXTRA_CORRECT_ANSWER, correctAnswer)
                .putExtra(EXTRA_SESSION_SCORE, score)
                .putExtra(EXTRA_AWARDED_POINTS, awardedPointsForResult)
                .putExtra(EXTRA_DIFFICULTY, difficulty.key)
        )
        finish()
    }

    private fun label(value: String): TextView {
        return textView(value, 13, R.color.algoplay_blue_dark, true, Gravity.START)
    }

    private fun actionButton(value: String, primary: Boolean): TextView {
        return textView(value, 13, if (primary) R.color.white else R.color.algoplay_blue_dark, true, Gravity.CENTER).apply {
            enableTapFeedback()
            background = if (primary) {
                roundedDrawable(ContextCompat.getColor(this@PuzzleSymbolActivity, R.color.algoplay_green_dark), dp(16))
            } else {
                roundedStrokeDrawable(
                    ContextCompat.getColor(this@PuzzleSymbolActivity, R.color.white),
                    ContextCompat.getColor(this@PuzzleSymbolActivity, R.color.algoplay_blue_dark),
                    dp(16)
                )
            }
        }
    }

    private fun showFeedback(message: String, success: Boolean) {
        txtPuzzleFeedback.visibility = View.VISIBLE
        txtPuzzleFeedback.text = message
        txtPuzzleFeedback.setTextColor(
            ContextCompat.getColor(this, if (success) R.color.algoplay_green_dark else R.color.algoplay_red_dark)
        )
    }

    private fun LinearLayout.disableChildren() {
        for (index in 0 until childCount) {
            getChildAt(index).isEnabled = false
            if (getChildAt(index) is LinearLayout) {
                (getChildAt(index) as LinearLayout).disableChildren()
            }
        }
    }

    private fun formatTime(millis: Long): String {
        val totalSeconds = (millis / 1000L).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    private fun cellIndex(row: Int, col: Int): Int {
        return (row * PuzzleSymbolEngine.PATTERN_GRID_SIZE) + col
    }

    private fun coloredCellDrawable(colorHex: String): GradientDrawable {
        return roundedStrokeDrawable(
            Color.parseColor(colorHex),
            ContextCompat.getColor(this, R.color.white),
            dp(10),
            dp(2)
        )
    }

    private fun emptyCellDrawable(): GradientDrawable {
        return roundedStrokeDrawable(
            Color.parseColor("#E0F4FF"),
            ContextCompat.getColor(this, R.color.algoplay_blue_dark),
            dp(10),
            dp(2)
        )
    }

    private fun textView(value: String, sizeSp: Int, colorRes: Int, bold: Boolean, gravityValue: Int): TextView {
        return TextView(this).apply {
            text = value
            textSize = sizeSp.toFloat()
            setTextColor(ContextCompat.getColor(this@PuzzleSymbolActivity, colorRes))
            gravity = gravityValue
            includeFontPadding = false
            if (bold) setTypeface(null, Typeface.BOLD)
        }
    }

    private fun roundedDrawable(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius.toFloat()
            setColor(color)
        }
    }

    private fun roundedStrokeDrawable(fillColor: Int, strokeColor: Int, radius: Int, strokeWidth: Int = dp(1)): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius.toFloat()
            setColor(fillColor)
            setStroke(strokeWidth, strokeColor)
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun puzzleVisualFor(index: Int): Int {
        val visuals = intArrayOf(
            R.drawable.puzzle_latihan,
            R.drawable.menantang_latihan,
            R.drawable.hai_latihan,
            R.drawable.latihan
        )
        return visuals[index % visuals.size]
    }

    companion object {
        const val EXTRA_GUEST_MODE = "extra_guest_mode"
        const val EXTRA_USER_ID = "extra_user_id"
        const val EXTRA_TRAINING_MODE = "extra_training_mode"
        const val EXTRA_CORRECT_ANSWER = "extra_correct_answer"
        const val EXTRA_SESSION_SCORE = "extra_session_score"
        const val EXTRA_AWARDED_POINTS = "extra_awarded_points"
        const val EXTRA_DIFFICULTY = "extra_difficulty"
        const val MODE_PUZZLE_SYMBOL = "puzzle_symbol"
    }
}
