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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class SequenceOrderActivity : AppCompatActivity() {

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
    private var questions = emptyList<SequenceQuestion>()
    private var difficulty = PuzzleDifficulty.EASY
    private var currentIndex = 0
    private var correctAnswer = 0
    private var currentSlots = mutableListOf<TextView>()
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
            background = roundedDrawable(ContextCompat.getColor(this@SequenceOrderActivity, R.color.white), dp(26))
            elevation = dp(10).toFloat()
            setPadding(dp(18), dp(18), dp(18), dp(18))
            alpha = 0f
            scaleX = 0.92f
            scaleY = 0.92f
        }
        root.addView(card, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER))

        card.addView(ImageView(this).apply {
            setImageResource(R.drawable.urutan_latihan)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
        }, LinearLayout.LayoutParams(dp(112), dp(112)))
        card.addView(textView("Pilih Level", 22, R.color.algoplay_text, true, Gravity.CENTER))
        card.addView(textView("Susun cerita tanpa acuan. Level menentukan waktu dan bonus jika skor 100.", 13, R.color.algoplay_subtext, false, Gravity.CENTER), LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(8) })

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
            PuzzleDifficulty.MEDIUM -> "2 menit per soal - bonus +20 jika skor 100"
            PuzzleDifficulty.HARD -> "30 detik per soal - bonus +50 jika skor 100"
        }
        return textView("${level.label}\n$detail", 13, R.color.algoplay_text, true, Gravity.CENTER).apply {
            setLineSpacing(dp(2).toFloat(), 1f)
            enableTapFeedback()
            background = roundedStrokeDrawable(ContextCompat.getColor(this@SequenceOrderActivity, R.color.white), ContextCompat.getColor(this@SequenceOrderActivity, R.color.algoplay_blue_soft), dp(18))
        }
    }

    private fun startSession(level: PuzzleDifficulty) {
        difficulty = level
        questions = TrainingQuestionBanks.getRandomSequenceQuestions()
        currentIndex = 0
        correctAnswer = 0
        isDone = false
        txtTitle.text = "Urutan Langkah"
        txtDifficulty.text = "Level ${difficulty.label}"
        imgHero.setImageResource(R.drawable.urutan_latihan)
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
        val question = questions[currentIndex]
        currentSlots.clear()
        txtProgress.text = "Soal ${currentIndex + 1} dari ${questions.size}"
        txtTarget.text = question.title
        imgTargetVisual.setImageResource(sequenceVisualFor(question.id))
        txtQuestion.text = question.question
        txtFeedback.visibility = View.GONE
        answerContainer.removeAllViews()

        answerContainer.addView(label("Taruh langkah ke urutan yang benar"))
        question.steps.forEachIndexed { index, _ ->
            val slot = createSlot(index)
            currentSlots.add(slot)
            answerContainer.addView(slot, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)).apply { topMargin = dp(8) })
        }

        answerContainer.addView(label("Balok langkah acak"), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(16) })
        question.steps.shuffled().forEach { step ->
            answerContainer.addView(stepBlock(step), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(46)).apply { topMargin = dp(7) })
        }

        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        actions.addView(actionButton("Ulangi", false).apply { setOnClickListener { renderQuestion() } }, LinearLayout.LayoutParams(0, dp(46), 1f).apply { marginEnd = dp(6) })
        actions.addView(actionButton("Cek", true).apply { setOnClickListener { checkAnswer() } }, LinearLayout.LayoutParams(0, dp(46), 1f).apply { marginStart = dp(6) })
        answerContainer.addView(actions, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(14) })
        startTimer()
    }

    private fun createSlot(index: Int): TextView {
        return textView("${index + 1}. Taruh langkah di sini", 13, R.color.algoplay_subtext, true, Gravity.CENTER_VERTICAL).apply {
            tag = null
            setPadding(dp(14), 0, dp(14), 0)
            background = roundedStrokeDrawable(ContextCompat.getColor(this@SequenceOrderActivity, R.color.algoplay_bg), ContextCompat.getColor(this@SequenceOrderActivity, R.color.algoplay_blue_soft), dp(16))
            setOnDragListener { target, event ->
                when (event.action) {
                    DragEvent.ACTION_DROP -> {
                        if (tag != null) return@setOnDragListener false
                        val step = event.clipData?.getItemAt(0)?.text?.toString().orEmpty()
                        val source = event.localState as? TextView
                        tag = step
                        text = "${index + 1}. $step"
                        setTextColor(ContextCompat.getColor(this@SequenceOrderActivity, R.color.algoplay_text))
                        background = roundedStrokeDrawable(ContextCompat.getColor(this@SequenceOrderActivity, R.color.white), ContextCompat.getColor(this@SequenceOrderActivity, R.color.algoplay_green_dark), dp(16))
                        source?.visibility = View.INVISIBLE
                        true
                    }
                    else -> true
                }
            }
        }
    }

    private fun stepBlock(step: String): TextView {
        return textView(step, 13, R.color.algoplay_text, true, Gravity.CENTER).apply {
            background = optionBlockDrawable(step)
            setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    view.parent?.requestDisallowInterceptTouchEvent(true)
                    view.startDragAndDrop(ClipData.newPlainText("step", step), View.DragShadowBuilder(view), view, 0)
                    true
                } else false
            }
        }
    }

    private fun checkAnswer() {
        val question = questions[currentIndex]
        val userAnswer = currentSlots.map { it.tag as? String }
        val complete = userAnswer.none { it == null }
        val correct = complete && userAnswer == question.steps
        if (correct) correctAnswer++
        timer?.cancel()
        val message = when {
            correct -> "Urutannya rapi dan masuk akal."
            complete -> "Urutannya belum tepat."
            else -> "Masih ada langkah yang belum diisi."
        }
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

    private fun startTimer() {
        timer = object : CountDownTimer(difficulty.timeLimitSeconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) { txtTimer.text = formatTime(millisUntilFinished) }
            override fun onFinish() {
                txtTimer.text = "00:00"
                playAlgoSound(AlgoSound.SALAH)
                showFeedback("Waktu habis. Lanjut ke soal berikutnya.", false)
                answerContainer.disableChildren()
                handler.postDelayed({ currentIndex++; renderQuestion() }, 900)
            }
        }.start()
    }

    private fun finishSession() {
        if (isDone) return
        isDone = true
        timer?.cancel()
        answerContainer.removeAllViews()
        txtTimer.text = "--:--"
        txtProgress.text = "Selesai"
        txtTarget.text = "Hasil"
        imgTargetVisual.setImageResource(R.drawable.sorakan_leaderboard)
        txtQuestion.text = "Robot Algo selesai menghitung skor Urutan Langkah."
        val score = TrainingQuestionBanks.calculateSequenceScore(correctAnswer)
        val bonus = if (score == 100) difficulty.perfectScoreBonus else 0
        playAlgoSound(AlgoSound.SELESAI)
        resultPanel.visibility = View.VISIBLE
        btnFinish.visibility = View.VISIBLE
        imgResult.setImageResource(if (correctAnswer == 5) R.drawable.sorakan_leaderboard else R.drawable.hai_materi)
        txtResultTitle.text = "Nilai $score"
        txtResultDetail.text = "Benar: $correctAnswer / 5\nSalah: ${5 - correctAnswer}\nSkor sesi: $score\nBonus level: $bonus\nPoin harian mengikuti batas 2 kali per hari."
    }

    private fun finishWithResult() {
        setResult(Activity.RESULT_OK, Intent().putExtra(EXTRA_TRAINING_MODE, MODE_SEQUENCE_ORDER).putExtra(EXTRA_CORRECT_ANSWER, correctAnswer).putExtra(EXTRA_SESSION_SCORE, TrainingQuestionBanks.calculateSequenceScore(correctAnswer)).putExtra(EXTRA_DIFFICULTY, difficulty.key))
        finish()
    }

    private fun label(value: String) = textView(value, 13, R.color.algoplay_blue_dark, true, Gravity.START)
    private fun actionButton(value: String, primary: Boolean) = textView(value, 13, if (primary) R.color.white else R.color.algoplay_blue_dark, true, Gravity.CENTER).apply {
        enableTapFeedback()
        background = if (primary) roundedDrawable(ContextCompat.getColor(this@SequenceOrderActivity, R.color.algoplay_green_dark), dp(16)) else roundedStrokeDrawable(ContextCompat.getColor(this@SequenceOrderActivity, R.color.white), ContextCompat.getColor(this@SequenceOrderActivity, R.color.algoplay_blue_dark), dp(16))
    }
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
    private fun formatTime(millis: Long): String {
        val seconds = (millis / 1000L).coerceAtLeast(0)
        return "%02d:%02d".format(seconds / 60, seconds % 60)
    }
    private fun textView(value: String, sizeSp: Int, colorRes: Int, bold: Boolean, gravityValue: Int) = TextView(this).apply {
        text = value
        textSize = sizeSp.toFloat()
        setTextColor(ContextCompat.getColor(this@SequenceOrderActivity, colorRes))
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
    private fun optionBlockDrawable(value: String): GradientDrawable {
        val palette = listOf(
            Color.parseColor("#D5F0FF") to Color.parseColor("#38BDF8"),
            Color.parseColor("#DCFCE7") to Color.parseColor("#22C55E"),
            Color.parseColor("#FEF3C7") to Color.parseColor("#F59E0B"),
            Color.parseColor("#FCE7F3") to Color.parseColor("#EC4899"),
            Color.parseColor("#EDE9FE") to Color.parseColor("#8B5CF6")
        )
        val (fill, stroke) = palette[Math.floorMod(value.hashCode(), palette.size)]
        return roundedStrokeDrawable(fill, stroke, dp(16))
    }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun sequenceVisualFor(id: Int): Int {
        val visuals = intArrayOf(
            R.drawable.urutan_latihan,
            R.drawable.belajar_materi,
            R.drawable.hai_materi,
            R.drawable.menantang_latihan,
            R.drawable.informasi_penjelasan
        )
        return visuals[id % visuals.size]
    }

    companion object {
        const val EXTRA_TRAINING_MODE = "extra_training_mode"
        const val EXTRA_CORRECT_ANSWER = "extra_correct_answer"
        const val EXTRA_SESSION_SCORE = "extra_session_score"
        const val EXTRA_DIFFICULTY = "extra_difficulty"
        const val MODE_SEQUENCE_ORDER = "urut_langkah"
    }
}
