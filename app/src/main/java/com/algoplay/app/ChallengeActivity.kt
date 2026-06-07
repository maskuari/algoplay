package com.algoplay.app

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.DragEvent
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class ChallengeActivity : AppCompatActivity() {

    private lateinit var txtTitle: TextView
    private lateinit var txtProgress: TextView
    private lateinit var txtTimer: TextView
    private lateinit var txtDifficulty: TextView
    private lateinit var txtTarget: TextView
    private lateinit var imgHero: ImageView
    private lateinit var imgTargetVisual: ImageView
    private lateinit var txtQuestion: TextView
    private lateinit var txtFeedback: TextView
    private lateinit var answerContainer: LinearLayout
    private lateinit var resultPanel: LinearLayout
    private lateinit var txtResultTitle: TextView
    private lateinit var txtResultDetail: TextView
    private lateinit var imgResult: ImageView
    private lateinit var btnFinish: TextView

    private val handler = Handler(Looper.getMainLooper())
    private var timer: CountDownTimer? = null
    private var questions = emptyList<ChallengeQuestion>()
    private var currentIndex = 0
    private var correctAnswer = 0
    private val placedBlocks = mutableMapOf<Int, String>()
    private var currentSlots = mutableListOf<TextView>()
    private var isDone = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puzzle_symbol)
        bindViews()
        btnFinish.enableTapFeedback()
        btnFinish.setOnClickListener { finishWithResult() }
        startSession()
    }

    override fun onDestroy() {
        timer?.cancel()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun bindViews() {
        txtTitle = findViewById(R.id.txtPuzzleTitle)
        txtProgress = findViewById(R.id.txtPuzzleProgress)
        txtTimer = findViewById(R.id.txtPuzzleTimer)
        txtDifficulty = findViewById(R.id.txtPuzzleDifficulty)
        txtTarget = findViewById(R.id.txtPuzzleTarget)
        imgHero = findViewById(R.id.imgPuzzleHero)
        imgTargetVisual = findViewById(R.id.imgPuzzleTargetVisual)
        txtQuestion = findViewById(R.id.txtPuzzleQuestion)
        txtFeedback = findViewById(R.id.txtPuzzleFeedback)
        answerContainer = findViewById(R.id.puzzleAnswerContainer)
        resultPanel = findViewById(R.id.puzzleResultPanel)
        txtResultTitle = findViewById(R.id.txtPuzzleResultTitle)
        txtResultDetail = findViewById(R.id.txtPuzzleResultDetail)
        imgResult = findViewById(R.id.imgPuzzleResult)
        btnFinish = findViewById(R.id.btnPuzzleFinish)
    }

    private fun startSession() {
        txtTitle.text = "Tantangan"
        txtDifficulty.text = "15 menit"
        imgHero.setImageResource(R.drawable.tantangan_latihan)
        questions = (
            PuzzleSymbolEngine.getRandomPuzzlePatternQuestions().take(3).map { ChallengeQuestion.Pattern(it) } +
                TrainingQuestionBanks.getRandomSequenceQuestions().take(3).map { ChallengeQuestion.Sequence(it) } +
                TrainingQuestionBanks.getRandomQuickQuizQuestions().take(4).map { ChallengeQuestion.Quiz(it) }
            ).shuffled().take(10)
        currentIndex = 0
        correctAnswer = 0
        isDone = false
        resultPanel.visibility = View.GONE
        btnFinish.visibility = View.GONE
        timer = object : CountDownTimer(15 * 60 * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) { txtTimer.text = formatTime(millisUntilFinished) }
            override fun onFinish() {
                txtTimer.text = "00:00"
                playAlgoSound(AlgoSound.SALAH)
                finishSession()
            }
        }.start()
        renderQuestion()
    }

    private fun renderQuestion() {
        if (currentIndex >= questions.size) {
            finishSession()
            return
        }
        placedBlocks.clear()
        currentSlots.clear()
        txtFeedback.visibility = View.GONE
        answerContainer.removeAllViews()
        txtProgress.text = "Soal ${currentIndex + 1} dari ${questions.size}"

        when (val item = questions[currentIndex]) {
            is ChallengeQuestion.Pattern -> renderPattern(item.question)
            is ChallengeQuestion.Sequence -> renderSequence(item.question)
            is ChallengeQuestion.Quiz -> renderQuiz(item.question)
        }
    }

    private fun renderPattern(question: PuzzlePatternQuestion) {
        txtTarget.text = "Tiru pola balok"
        imgTargetVisual.setImageResource(R.drawable.puzzle_latihan)
        txtQuestion.text = question.question
        answerContainer.addView(label("Contoh pola"))
        answerContainer.addView(patternGrid(question.blocks, readOnly = true), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(8) })
        answerContainer.addView(label("Susun jawabanmu"), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(14) })
        answerContainer.addView(patternGrid(emptyList(), readOnly = false), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(8) })
        answerContainer.addView(label("Balok"), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(14) })
        answerContainer.addView(blockTray(question.blocks.shuffled()), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(8) })
        answerContainer.addView(actionButton("Cek Pola").apply { setOnClickListener { checkPattern(question) } }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(46)).apply { topMargin = dp(12) })
    }

    private fun renderSequence(question: SequenceQuestion) {
        txtTarget.text = question.title
        imgTargetVisual.setImageResource(R.drawable.urutan_latihan)
        txtQuestion.text = question.question
        question.steps.forEachIndexed { index, _ ->
            val slot = slot(index)
            currentSlots.add(slot)
            answerContainer.addView(slot, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)).apply { topMargin = dp(8) })
        }
        answerContainer.addView(label("Langkah acak"), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(14) })
        question.steps.shuffled().forEach { step ->
            answerContainer.addView(stepBlock(step), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(46)).apply { topMargin = dp(7) })
        }
        answerContainer.addView(actionButton("Cek Urutan").apply { setOnClickListener { checkSequence(question) } }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(46)).apply { topMargin = dp(12) })
    }

    private fun renderQuiz(question: QuickQuizQuestion) {
        txtTarget.text = "Pilih jawaban terbaik"
        imgTargetVisual.setImageResource(R.drawable.quiz_latihan)
        txtQuestion.text = question.question
        question.options.forEachIndexed { index, option ->
            answerContainer.addView(optionButton(option).apply { setOnClickListener { chooseQuiz(index, question) } }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(8) })
        }
    }

    private fun checkPattern(question: PuzzlePatternQuestion) {
        val target = question.blocks.associate { index(it.row, it.col) to it.colorHex }
        val correct = placedBlocks.size == target.size && target.all { (key, color) -> placedBlocks[key].equals(color, true) }
        answer(correct, if (correct) "Benar!" else "Pola belum sama.")
    }

    private fun checkSequence(question: SequenceQuestion) {
        val userAnswer = currentSlots.map { it.tag as? String }
        if (userAnswer.any { it == null }) {
            answer(false, "Masih ada slot kosong.")
        } else {
            val correct = userAnswer == question.steps
            answer(correct, if (correct) "Urutannya tepat." else "Urutan belum tepat.")
        }
    }

    private fun chooseQuiz(index: Int, question: QuickQuizQuestion) {
        answer(index == question.correctIndex, if (index == question.correctIndex) "Benar!" else "Belum tepat.")
    }

    private fun answer(correct: Boolean, message: String) {
        if (correct) correctAnswer++
        showFeedback(if (correct) "Benar! $message" else "Salah. $message", correct)
        showBriefResultPopup(
            title = if (correct) "Benar!" else "Salah",
            message = message,
            imageRes = if (correct) R.drawable.sorakan_leaderboard else R.drawable.hai_materi,
            success = correct
        )
        answerContainer.disableChildren()
        handler.postDelayed({ currentIndex++; renderQuestion() }, 900)
    }

    private fun finishSession() {
        if (isDone) return
        isDone = true
        timer?.cancel()
        val score = correctAnswer * 10
        playAlgoSound(AlgoSound.SELESAI)
        answerContainer.removeAllViews()
        txtProgress.text = "Selesai"
        txtTarget.text = "Hasil"
        imgTargetVisual.setImageResource(R.drawable.sorakan_leaderboard)
        txtQuestion.text = "Tantangan selesai. Robot Algo menghitung skor kamu."
        resultPanel.visibility = View.VISIBLE
        btnFinish.visibility = View.VISIBLE
        imgResult.setImageResource(if (correctAnswer == 10) R.drawable.sorakan_leaderboard else R.drawable.hai_materi)
        txtResultTitle.text = "Nilai $score"
        txtResultDetail.text = "Benar: $correctAnswer / 10\nSalah: ${10 - correctAnswer}\nSkor sesi: $score\nWaktu pengerjaan: 15 menit."
    }

    private fun finishWithResult() {
        setResult(Activity.RESULT_OK, Intent().putExtra(EXTRA_TRAINING_MODE, MODE_CHALLENGE).putExtra(EXTRA_CORRECT_ANSWER, correctAnswer).putExtra(EXTRA_SESSION_SCORE, correctAnswer * 10))
        finish()
    }

    private fun patternGrid(blocks: List<PuzzleBlock>, readOnly: Boolean): LinearLayout {
        val blockMap = blocks.associateBy { index(it.row, it.col) }
        val grid = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER }
        repeat(PuzzleSymbolEngine.PATTERN_GRID_SIZE) { row ->
            val rowView = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
            repeat(PuzzleSymbolEngine.PATTERN_GRID_SIZE) { col ->
                val block = blockMap[index(row, col)]
                val cell = TextView(this).apply {
                    background = if (block == null) emptyCell() else colored(block.colorHex)
                    if (!readOnly) setupDrop(this, row, col)
                }
                rowView.addView(cell, LinearLayout.LayoutParams(dp(38), dp(38)).apply { marginStart = dp(2); marginEnd = dp(2); topMargin = dp(2); bottomMargin = dp(2) })
            }
            grid.addView(rowView)
        }
        return grid
    }

    private fun setupDrop(cell: TextView, row: Int, col: Int) {
        cell.setOnDragListener { target, event ->
            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    val source = event.localState as? TextView ?: return@setOnDragListener false
                    val color = event.clipData?.getItemAt(0)?.text?.toString().orEmpty()
                    val key = index(row, col)
                    if (placedBlocks.containsKey(key)) return@setOnDragListener false
                    placedBlocks[key] = color
                    (target as TextView).background = colored(color)
                    source.visibility = View.INVISIBLE
                    true
                }
                else -> true
            }
        }
    }

    private fun blockTray(blocks: List<PuzzleBlock>): LinearLayout {
        val tray = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER }
        blocks.chunked(5).forEach { rowBlocks ->
            val row = LinearLayout(this).apply { gravity = Gravity.CENTER }
            rowBlocks.forEach { block -> row.addView(colorBlock(block.colorHex), LinearLayout.LayoutParams(dp(40), dp(40)).apply { marginStart = dp(4); marginEnd = dp(4); topMargin = dp(4); bottomMargin = dp(4) }) }
            tray.addView(row)
        }
        return tray
    }

    private fun colorBlock(color: String) = TextView(this).apply {
        background = colored(color)
        setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                view.parent?.requestDisallowInterceptTouchEvent(true)
                view.startDragAndDrop(ClipData.newPlainText("color", color), View.DragShadowBuilder(view), view, 0)
                true
            } else false
        }
    }

    private fun slot(index: Int) = text("${index + 1}. Taruh langkah di sini", 13, R.color.algoplay_subtext, true, Gravity.CENTER_VERTICAL).apply {
        tag = null
        setPadding(dp(14), 0, dp(14), 0)
        background = stroke(ContextCompat.getColor(this@ChallengeActivity, R.color.algoplay_bg), ContextCompat.getColor(this@ChallengeActivity, R.color.algoplay_blue_soft), dp(16))
        setOnDragListener { target, event ->
            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    if (tag != null) return@setOnDragListener false
                    val step = event.clipData?.getItemAt(0)?.text?.toString().orEmpty()
                    val source = event.localState as? TextView
                    tag = step
                    this.text = "${index + 1}. $step"
                    setTextColor(ContextCompat.getColor(this@ChallengeActivity, R.color.algoplay_text))
                    background = stroke(ContextCompat.getColor(this@ChallengeActivity, R.color.white), ContextCompat.getColor(this@ChallengeActivity, R.color.algoplay_green_dark), dp(16))
                    source?.visibility = View.INVISIBLE
                    true
                }
                else -> true
            }
        }
    }

    private fun stepBlock(step: String) = text(step, 13, R.color.algoplay_text, true, Gravity.CENTER).apply {
        background = optionBlockDrawable(step)
        setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                view.parent?.requestDisallowInterceptTouchEvent(true)
                view.startDragAndDrop(ClipData.newPlainText("step", step), View.DragShadowBuilder(view), view, 0)
                true
            } else false
        }
    }

    private fun optionButton(value: String) = text(value, 13, R.color.algoplay_text, true, Gravity.CENTER_VERTICAL).apply {
        minHeight = dp(48)
        setPadding(dp(14), dp(8), dp(14), dp(8))
        enableTapFeedback()
        background = stroke(
            ContextCompat.getColor(this@ChallengeActivity, R.color.white),
            ContextCompat.getColor(this@ChallengeActivity, R.color.algoplay_blue_soft),
            dp(16),
            dp(2)
        )
    }
    private fun actionButton(value: String) = text(value, 13, R.color.white, true, Gravity.CENTER).apply {
        enableTapFeedback()
        background = rounded(ContextCompat.getColor(this@ChallengeActivity, R.color.algoplay_green_dark), dp(16))
    }
    private fun label(value: String) = text(value, 13, R.color.algoplay_blue_dark, true, Gravity.START)
    private fun showFeedback(message: String, success: Boolean) {
        txtFeedback.visibility = View.VISIBLE
        txtFeedback.text = message
        txtFeedback.setTextColor(ContextCompat.getColor(this, if (success) R.color.algoplay_green_dark else R.color.algoplay_red_dark))
    }
    private fun LinearLayout.disableChildren() {
        for (i in 0 until childCount) {
            getChildAt(i).isEnabled = false
            if (getChildAt(i) is LinearLayout) (getChildAt(i) as LinearLayout).disableChildren()
        }
    }
    private fun index(row: Int, col: Int) = row * PuzzleSymbolEngine.PATTERN_GRID_SIZE + col
    private fun formatTime(millis: Long): String {
        val seconds = (millis / 1000L).coerceAtLeast(0)
        return "%02d:%02d".format(seconds / 60, seconds % 60)
    }
    private fun colored(color: String) = stroke(Color.parseColor(color), ContextCompat.getColor(this, R.color.white), dp(10), dp(2))
    private fun emptyCell() = stroke(Color.parseColor("#E0F4FF"), ContextCompat.getColor(this, R.color.algoplay_blue_dark), dp(10), dp(2))
    private fun text(value: String, size: Int, color: Int, bold: Boolean, gravityValue: Int) = TextView(this).apply {
        text = value
        textSize = size.toFloat()
        setTextColor(ContextCompat.getColor(this@ChallengeActivity, color))
        gravity = gravityValue
        includeFontPadding = false
        if (bold) setTypeface(null, Typeface.BOLD)
    }
    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = radius.toFloat()
        setColor(color)
    }
    private fun stroke(fill: Int, stroke: Int, radius: Int, width: Int = dp(1)) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = radius.toFloat()
        setColor(fill)
        setStroke(width, stroke)
    }
    private fun optionBlockDrawable(value: String): GradientDrawable {
        val palette = listOf(
            Color.parseColor("#D5F0FF") to Color.parseColor("#38BDF8"),
            Color.parseColor("#DCFCE7") to Color.parseColor("#22C55E"),
            Color.parseColor("#FEF3C7") to Color.parseColor("#F59E0B"),
            Color.parseColor("#FCE7F3") to Color.parseColor("#EC4899"),
            Color.parseColor("#EDE9FE") to Color.parseColor("#8B5CF6")
        )
        val (fill, border) = palette[Math.floorMod(value.hashCode(), palette.size)]
        return stroke(fill, border, dp(16), dp(2))
    }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private sealed class ChallengeQuestion {
        data class Pattern(val question: PuzzlePatternQuestion) : ChallengeQuestion()
        data class Sequence(val question: SequenceQuestion) : ChallengeQuestion()
        data class Quiz(val question: QuickQuizQuestion) : ChallengeQuestion()
    }

    companion object {
        const val EXTRA_TRAINING_MODE = "extra_training_mode"
        const val EXTRA_CORRECT_ANSWER = "extra_correct_answer"
        const val EXTRA_SESSION_SCORE = "extra_session_score"
        const val MODE_CHALLENGE = "tantangan_harian"
    }
}
