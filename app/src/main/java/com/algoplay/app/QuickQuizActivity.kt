package com.algoplay.app

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class QuickQuizActivity : AppCompatActivity() {

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
    private var questions = emptyList<QuickQuizQuestion>()
    private var difficultyKey = PuzzleDifficulty.EASY.key
    private var difficultyLabel = PuzzleDifficulty.EASY.label
    private var timeLimitSeconds = 180
    private var perfectScoreBonus = 0
    private var currentIndex = 0
    private var correctAnswer = 0
    private var isDone = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puzzle_symbol)
        bindViews()
        btnFinish.enableTapFeedback()
        btnFinish.setOnClickListener { finishWithResult() }
        showLevelDialog()
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

    private fun showLevelDialog() {
        val dialog = Dialog(this)
        dialog.setCancelable(false)
        val root = FrameLayout(this).apply { setPadding(dp(22), 0, dp(22), 0) }
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            background = roundedDrawable(ContextCompat.getColor(this@QuickQuizActivity, R.color.white), dp(26))
            elevation = dp(10).toFloat()
            setPadding(dp(18), dp(18), dp(18), dp(18))
            alpha = 0f
            scaleX = 0.92f
            scaleY = 0.92f
        }
        root.addView(card, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER))
        card.addView(ImageView(this).apply {
            setImageResource(R.drawable.quiz_latihan)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
        }, LinearLayout.LayoutParams(dp(112), dp(112)))
        card.addView(textView("Pilih Level", 22, R.color.algoplay_text, true, Gravity.CENTER))
        card.addView(textView("Quiz Cepat memakai timer per soal.", 13, R.color.algoplay_subtext, false, Gravity.CENTER), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(8) })

        listOf(PuzzleDifficulty.EASY, PuzzleDifficulty.MEDIUM, PuzzleDifficulty.HARD).forEach { level ->
            card.addView(levelButton(level).apply {
                setOnClickListener {
                    dialog.dismiss()
                    startSession(level)
                }
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(58)).apply { topMargin = dp(10) })
        }
        dialog.setContentView(root)
        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setDimAmount(0.42f)
            dialog.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            card.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(260).setInterpolator(OvershootInterpolator(1.05f)).start()
        }
        dialog.show()
    }

    private fun levelButton(level: PuzzleDifficulty): TextView {
        val detail = when (level) {
            PuzzleDifficulty.EASY -> "3 menit per soal - tanpa bonus"
            PuzzleDifficulty.MEDIUM -> "1 menit per soal - bonus +20 jika skor 100"
            PuzzleDifficulty.HARD -> "20 detik per soal - bonus +50 jika skor 100"
        }
        return textView("${level.label}\n$detail", 13, R.color.algoplay_text, true, Gravity.CENTER).apply {
            setLineSpacing(dp(2).toFloat(), 1f)
            enableTapFeedback()
            background = roundedStrokeDrawable(ContextCompat.getColor(this@QuickQuizActivity, R.color.white), ContextCompat.getColor(this@QuickQuizActivity, R.color.algoplay_blue_soft), dp(18))
        }
    }

    private fun startSession(level: PuzzleDifficulty) {
        difficultyKey = level.key
        difficultyLabel = level.label
        perfectScoreBonus = level.perfectScoreBonus
        timeLimitSeconds = when (level) {
            PuzzleDifficulty.EASY -> 180
            PuzzleDifficulty.MEDIUM -> 60
            PuzzleDifficulty.HARD -> 20
        }
        questions = TrainingQuestionBanks.getRandomQuickQuizQuestions()
        currentIndex = 0
        correctAnswer = 0
        isDone = false
        txtTitle.text = "Quiz Cepat"
        txtDifficulty.text = "Level $difficultyLabel"
        imgHero.setImageResource(R.drawable.quiz_latihan)
        resultPanel.visibility = View.GONE
        btnFinish.visibility = View.GONE
        renderQuestion()
    }

    private fun renderQuestion() {
        timer?.cancel()
        if (currentIndex >= questions.size) {
            finishSession()
            return
        }
        val q = questions[currentIndex]
        txtProgress.text = "Soal ${currentIndex + 1} dari ${questions.size}"
        txtTarget.text = "Pilih jawaban terbaik"
        imgTargetVisual.setImageResource(quizVisualFor(q.id))
        txtQuestion.text = q.question
        txtFeedback.visibility = View.GONE
        answerContainer.removeAllViews()
        q.options.forEachIndexed { index, option ->
            answerContainer.addView(optionButton(option).apply {
                setOnClickListener { chooseAnswer(index, this) }
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(8) })
        }
        startTimer()
    }

    private fun chooseAnswer(index: Int, view: TextView) {
        timer?.cancel()
        val q = questions[currentIndex]
        val correct = index == q.correctIndex
        if (correct) correctAnswer++
        answerContainer.disableChildren()
        view.background = roundedStrokeDrawable(ContextCompat.getColor(this, R.color.white), ContextCompat.getColor(this, if (correct) R.color.algoplay_green_dark else R.color.algoplay_red_dark), dp(16))
        val message = if (correct) "Jawabanmu tepat." else "Jawaban benar: ${q.options[q.correctIndex]}"
        showFeedback(if (correct) "Benar! $message" else "Salah. $message", correct)
        showBriefResultPopup(
            title = if (correct) "Benar!" else "Salah",
            message = message,
            imageRes = if (correct) R.drawable.sorakan_leaderboard else R.drawable.hai_materi,
            success = correct
        )
        handler.postDelayed({ currentIndex++; renderQuestion() }, 900)
    }

    private fun startTimer() {
        timer = object : CountDownTimer(timeLimitSeconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) { txtTimer.text = formatTime(millisUntilFinished) }
            override fun onFinish() {
                txtTimer.text = "00:00"
                playAlgoSound(AlgoSound.SALAH)
                showFeedback("Waktu habis. Lanjut soal berikutnya.", false)
                answerContainer.disableChildren()
                handler.postDelayed({ currentIndex++; renderQuestion() }, 850)
            }
        }.start()
    }

    private fun finishSession() {
        if (isDone) return
        isDone = true
        val score = TrainingQuestionBanks.calculateQuickQuizScore(correctAnswer)
        val bonus = if (score == 100) perfectScoreBonus else 0
        playAlgoSound(AlgoSound.SELESAI)
        timer?.cancel()
        answerContainer.removeAllViews()
        txtTimer.text = "--:--"
        txtProgress.text = "Selesai"
        txtTarget.text = "Hasil"
        imgTargetVisual.setImageResource(R.drawable.sorakan_leaderboard)
        txtQuestion.text = "Robot Algo selesai menghitung skor Quiz Cepat."
        resultPanel.visibility = View.VISIBLE
        btnFinish.visibility = View.VISIBLE
        imgResult.setImageResource(if (correctAnswer == 10) R.drawable.sorakan_leaderboard else R.drawable.hai_materi)
        txtResultTitle.text = "Nilai $score"
        txtResultDetail.text = "Benar: $correctAnswer / 10\nSalah: ${10 - correctAnswer}\nSkor sesi: $score\nBonus level: $bonus\nTimer berjalan per soal."
    }

    private fun finishWithResult() {
        setResult(Activity.RESULT_OK, Intent().putExtra(EXTRA_TRAINING_MODE, MODE_QUICK_QUIZ).putExtra(EXTRA_CORRECT_ANSWER, correctAnswer).putExtra(EXTRA_SESSION_SCORE, TrainingQuestionBanks.calculateQuickQuizScore(correctAnswer)).putExtra(EXTRA_DIFFICULTY, difficultyKey))
        finish()
    }

    private fun optionButton(value: String) = textView(value, 13, R.color.algoplay_text, true, Gravity.CENTER_VERTICAL).apply {
        minHeight = dp(48)
        setPadding(dp(14), dp(8), dp(14), dp(8))
        enableTapFeedback()
        background = roundedStrokeDrawable(
            ContextCompat.getColor(this@QuickQuizActivity, R.color.white),
            ContextCompat.getColor(this@QuickQuizActivity, R.color.algoplay_blue_soft),
            dp(16)
        )
    }
    private fun showFeedback(message: String, success: Boolean) {
        txtFeedback.visibility = View.VISIBLE
        txtFeedback.text = message
        txtFeedback.setTextColor(ContextCompat.getColor(this, if (success) R.color.algoplay_green_dark else R.color.algoplay_red_dark))
    }
    private fun LinearLayout.disableChildren() {
        for (i in 0 until childCount) getChildAt(i).isEnabled = false
    }
    private fun formatTime(millis: Long): String {
        val seconds = (millis / 1000L).coerceAtLeast(0)
        return "%02d:%02d".format(seconds / 60, seconds % 60)
    }
    private fun textView(value: String, sizeSp: Int, colorRes: Int, bold: Boolean, gravityValue: Int) = TextView(this).apply {
        text = value
        textSize = sizeSp.toFloat()
        setTextColor(ContextCompat.getColor(this@QuickQuizActivity, colorRes))
        gravity = gravityValue
        includeFontPadding = false
        if (bold) setTypeface(null, Typeface.BOLD)
    }
    private fun roundedDrawable(color: Int, radius: Int) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = radius.toFloat()
        setColor(color)
    }
    private fun roundedStrokeDrawable(fillColor: Int, strokeColor: Int, radius: Int) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = radius.toFloat()
        setColor(fillColor)
        setStroke(dp(1), strokeColor)
    }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun quizVisualFor(id: Int): Int {
        val visuals = intArrayOf(
            R.drawable.quiz_latihan,
            R.drawable.hai_latihan,
            R.drawable.menantang_latihan,
            R.drawable.tantangan_latihan,
            R.drawable.belajar_materi,
            R.drawable.sorakan_leaderboard
        )
        return visuals[id % visuals.size]
    }

    companion object {
        const val EXTRA_TRAINING_MODE = "extra_training_mode"
        const val EXTRA_CORRECT_ANSWER = "extra_correct_answer"
        const val EXTRA_SESSION_SCORE = "extra_session_score"
        const val EXTRA_DIFFICULTY = "extra_difficulty"
        const val MODE_QUICK_QUIZ = "quiz_cepat"
    }
}
