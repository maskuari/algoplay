package com.algoplay.app

import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.DragEvent
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

open class LessonOneActivity : AppCompatActivity() {

    private lateinit var lessonScroll: ScrollView
    private lateinit var lessonTopAvatarWrap: View
    private lateinit var imgLessonTopPhoto: ImageView
    private lateinit var txtLessonTopName: TextView
    private lateinit var txtLessonTopDay: TextView
    private lateinit var txtLessonTopDateTime: TextView
    private lateinit var lessonRankBadge: LinearLayout
    private lateinit var txtLessonRankLevel: TextView
    private lateinit var txtLessonRankStatus: TextView
    protected lateinit var gameChoiceContainer: LinearLayout
    protected lateinit var gameSlotContainer: LinearLayout
    protected lateinit var btnResetGame: TextView
    protected lateinit var btnCheckGame: TextView
    private lateinit var quizOneContainer: LinearLayout
    private lateinit var quizTwoContainer: LinearLayout
    private lateinit var btnFinishLesson: TextView

    private val slotViews = mutableListOf<TextView>()
    protected open val layoutResId: Int = R.layout.activity_lesson_one
    protected open val lessonNumber: Int = 1
    protected open val nextLessonNumber: Int = 2
    protected open val correctSteps: List<String> = listOf(
        "Ambil sikat gigi",
        "Beri pasta gigi",
        "Sikat gigi",
        "Berkumur",
        "Simpan sikat gigi"
    )
    protected open val shuffledSteps: List<String> = listOf(
        "Berkumur",
        "Sikat gigi",
        "Ambil sikat gigi",
        "Simpan sikat gigi",
        "Beri pasta gigi"
    )
    protected open val quizOneOptions: List<String> = listOf(
        "A. Gambar yang lucu",
        "B. Urutan langkah-langkah untuk menyelesaikan sesuatu",
        "C. Nama sebuah mainan",
        "D. Warna pada komputer"
    )
    protected open val quizOneCorrectIndex: Int = 1
    protected open val quizOneCorrectMessage: String = "Benar! Algoritma adalah urutan langkah-langkah."
    protected open val quizOneWrongMessage: String = "Belum tepat. Algoritma adalah langkah-langkah yang disusun berurutan."
    protected open val quizTwoOptions: List<String> = listOf(
        "A. Supaya hasilnya benar",
        "B. Supaya lebih membingungkan",
        "C. Supaya tidak selesai",
        "D. Supaya terlihat acak"
    )
    protected open val quizTwoCorrectIndex: Int = 0
    protected open val quizTwoCorrectMessage: String = "Pintar! Langkah yang berurutan membuat tugas selesai dengan benar."
    protected open val quizTwoWrongMessage: String = "Coba ingat lagi, langkah yang urut membantu kita mendapatkan hasil yang benar."
    protected open val gameSuccessTitle: String = "Hebat!"
    protected open val gameSuccessMessage: String = "Kamu berhasil menyusun algoritma dengan benar!"
    protected open val gameWrongTitle: String = "Hampir benar"
    protected open val gameWrongMessage: String = "Coba susun lagi langkahnya dari awal, ya."

    private var isGuestMode = false
    private var alreadyCompleted = false
    private var userName = "Teman"
    private var photoUrl: String? = null
    private var photoUri: String? = null
    private var currentScore = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutResId)
        preloadAlgoSounds()

        isGuestMode = intent.getBooleanExtra(EXTRA_GUEST_MODE, false)
        alreadyCompleted = intent.getBooleanExtra(EXTRA_ALREADY_COMPLETED, false)
        userName = intent.getStringExtra(EXTRA_USER_NAME) ?: if (isGuestMode) "Guest" else "Teman"
        photoUrl = intent.getStringExtra(EXTRA_PHOTO_URL)
        photoUri = intent.getStringExtra(EXTRA_PHOTO_URI)
        currentScore = intent.getIntExtra(EXTRA_SCORE, 0)

        bindViews()
        setupTopBar()
        buildGame()
        buildQuiz()
        setupActions()
    }

    private fun bindViews() {
        lessonScroll = findViewById(R.id.lessonScroll)
        lessonTopAvatarWrap = findViewById(R.id.lessonTopAvatarWrap)
        imgLessonTopPhoto = findViewById(R.id.imgLessonTopPhoto)
        txtLessonTopName = findViewById(R.id.txtLessonTopName)
        txtLessonTopDay = findViewById(R.id.txtLessonTopDay)
        txtLessonTopDateTime = findViewById(R.id.txtLessonTopDateTime)
        lessonRankBadge = findViewById(R.id.lessonRankBadge)
        txtLessonRankLevel = findViewById(R.id.txtLessonRankLevel)
        txtLessonRankStatus = findViewById(R.id.txtLessonRankStatus)
        gameChoiceContainer = findViewById(R.id.gameChoiceContainer)
        gameSlotContainer = findViewById(R.id.gameSlotContainer)
        btnResetGame = findViewById(R.id.btnResetGame)
        btnCheckGame = findViewById(R.id.btnCheckGame)
        quizOneContainer = findViewById(R.id.quizOneContainer)
        quizTwoContainer = findViewById(R.id.quizTwoContainer)
        btnFinishLesson = findViewById(R.id.btnFinishLesson)
    }

    private fun setupTopBar() {
        val now = Date()
        txtLessonTopName.text = if (isGuestMode) "Guest" else userName
        txtLessonTopDay.text = SimpleDateFormat("EEEE", Locale("id", "ID")).format(now)
        txtLessonTopDateTime.text = SimpleDateFormat("d MMMM yyyy - HH:mm", Locale("id", "ID")).format(now)

        lessonTopAvatarWrap.visibility = if (isGuestMode) View.GONE else View.VISIBLE
        lessonRankBadge.visibility = if (isGuestMode) View.GONE else View.VISIBLE
        if (!isGuestMode) {
            val rank = scoreRank(currentScore)
            txtLessonRankLevel.text = "Tingkat ${rank.level}"
            txtLessonRankStatus.text = rank.status
            lessonRankBadge.background = roundedDrawable(Color.parseColor(rank.colorHex), dp(18))
            updateTopPhoto()
        }
    }

    private fun updateTopPhoto() {
        imgLessonTopPhoto.clearColorFilter()
        imgLessonTopPhoto.imageTintList = null
        photoUri?.takeIf { it.isNotBlank() }?.let { value ->
            runCatching {
                imgLessonTopPhoto.setImageURI(Uri.parse(value))
                imgLessonTopPhoto.scaleType = ImageView.ScaleType.CENTER_CROP
            }.onSuccess {
                return
            }
        }

        imgLessonTopPhoto.setImageResource(R.drawable.ic_user)
        imgLessonTopPhoto.setColorFilter(ContextCompat.getColor(this, R.color.algoplay_blue_dark))
        imgLessonTopPhoto.scaleType = ImageView.ScaleType.FIT_CENTER
        photoUrl?.takeIf { it.isNotBlank() }?.let { loadRemotePhoto(it, imgLessonTopPhoto) }
    }

    protected open fun buildGame() {
        gameChoiceContainer.removeAllViews()
        gameSlotContainer.removeAllViews()
        slotViews.clear()

        shuffledSteps.forEach { step ->
            gameChoiceContainer.addView(createStepBlock(step), LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(44)
            ).apply {
                topMargin = dp(7)
            })
        }

        correctSteps.forEachIndexed { index, _ ->
            val slot = createDropSlot(index + 1)
            slotViews.add(slot)
            gameSlotContainer.addView(slot, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
            ).apply {
                topMargin = dp(7)
            })
        }
    }

    private fun createStepBlock(step: String): TextView {
        return TextView(this).apply {
            text = step
            gravity = Gravity.CENTER
            includeFontPadding = false
            setTextColor(ContextCompat.getColor(this@LessonOneActivity, R.color.algoplay_text))
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            background = optionBlockDrawable(step)
            setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val shadow = View.DragShadowBuilder(view)
                    view.startDragAndDrop(ClipData.newPlainText("step", step), shadow, view, 0)
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun createDropSlot(number: Int): TextView {
        return TextView(this).apply {
            text = "$number. Taruh langkah di sini"
            tag = null
            gravity = Gravity.CENTER_VERTICAL
            includeFontPadding = false
            setPadding(dp(14), 0, dp(14), 0)
            setTextColor(ContextCompat.getColor(this@LessonOneActivity, R.color.algoplay_subtext))
            textSize = 13f
            background = roundedStrokeDrawable(
                ContextCompat.getColor(this@LessonOneActivity, R.color.algoplay_bg),
                ContextCompat.getColor(this@LessonOneActivity, R.color.algoplay_blue_soft),
                dp(16)
            )
            setOnDragListener { target, event ->
                when (event.action) {
                    DragEvent.ACTION_DROP -> {
                        val dragged = event.localState as? TextView ?: return@setOnDragListener false
                        val value = dragged.text.toString()
                        (target as TextView).text = "$number. $value"
                        target.tag = value
                        target.setTextColor(ContextCompat.getColor(this@LessonOneActivity, R.color.algoplay_text))
                        target.background = roundedStrokeDrawable(
                            ContextCompat.getColor(this@LessonOneActivity, R.color.white),
                            ContextCompat.getColor(this@LessonOneActivity, R.color.algoplay_green_dark),
                            dp(16)
                        )
                        dragged.visibility = View.GONE
                        true
                    }
                    else -> true
                }
            }
        }
    }

    private fun buildQuiz() {
        quizOneContainer.removeAllViews()
        addQuizOptions(
            quizOneContainer,
            quizOneOptions,
            quizOneCorrectIndex,
            quizOneCorrectMessage,
            quizOneWrongMessage
        )

        quizTwoContainer.removeAllViews()
        addQuizOptions(
            quizTwoContainer,
            quizTwoOptions,
            quizTwoCorrectIndex,
            quizTwoCorrectMessage,
            quizTwoWrongMessage
        )
    }

    private fun addQuizOptions(
        container: LinearLayout,
        options: List<String>,
        correctIndex: Int,
        correctMessage: String,
        wrongMessage: String
    ) {
        options.forEachIndexed { index, option ->
            val button = TextView(this).apply {
                text = option
                gravity = Gravity.CENTER_VERTICAL
                minHeight = dp(46)
                setPadding(dp(14), dp(8), dp(14), dp(8))
                setTextColor(ContextCompat.getColor(this@LessonOneActivity, R.color.algoplay_text))
                textSize = 13f
                background = roundedStrokeDrawable(
                    ContextCompat.getColor(this@LessonOneActivity, R.color.white),
                    ContextCompat.getColor(this@LessonOneActivity, R.color.algoplay_blue_soft),
                    dp(16)
                )
                enableTapFeedback()
                setOnClickListener {
                    val isCorrect = index == correctIndex
                    background = roundedStrokeDrawable(
                        ContextCompat.getColor(this@LessonOneActivity, if (isCorrect) R.color.algoplay_blue_soft else R.color.algoplay_bg),
                        ContextCompat.getColor(this@LessonOneActivity, if (isCorrect) R.color.algoplay_green_dark else R.color.algoplay_red_dark),
                        dp(16)
                    )
                    showFeedbackDialog(
                        if (isCorrect) "Jawaban Benar" else "Coba Lagi",
                        if (isCorrect) correctMessage else wrongMessage,
                        if (isCorrect) R.drawable.sorakan_leaderboard else R.drawable.hai_materi,
                        isCorrect
                    )
                }
            }
            container.addView(button, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(7)
            })
        }
    }

    protected open fun setupActions() {
        btnResetGame.enableTapFeedback()
        btnCheckGame.enableTapFeedback()
        btnFinishLesson.enableTapFeedback()
        btnResetGame.setOnClickListener { buildGame() }
        btnCheckGame.setOnClickListener { checkGameAnswer() }
        btnFinishLesson.setOnClickListener {
            showFeedbackDialog(
                "Materi Selesai",
                when {
                    alreadyCompleted -> "Materi $lessonNumber sudah selesai. Yuk lanjut ke Materi $nextLessonNumber."
                    isGuestMode -> "Keren! Ini simulasi selesai. Materi $nextLessonNumber akan terbuka selama sesi ini."
                    else -> "Keren! Kamu menyelesaikan Materi $lessonNumber dan mendapat +100 score."
                },
                R.drawable.sorakan_leaderboard,
                true,
                soundEffect = AlgoSound.SELESAI
            ) {
                val data = Intent().putExtra(EXTRA_COMPLETED_LESSON, lessonNumber)
                setResult(Activity.RESULT_OK, data)
                finish()
            }
        }
    }

    protected open fun checkGameAnswer() {
        val userAnswer = slotViews.map { it.tag as? String }
        if (userAnswer.any { it == null }) {
            showFeedbackDialog(
                "Belum lengkap",
                "Isi semua kotak nomor dulu, ya.",
                R.drawable.hai_materi,
                false
            )
            return
        }

        val isCorrect = userAnswer == correctSteps
        if (isCorrect) {
            slotViews.forEach {
                it.background = roundedStrokeDrawable(
                    ContextCompat.getColor(this, R.color.white),
                    ContextCompat.getColor(this, R.color.algoplay_green_dark),
                    dp(16)
                )
            }
            showFeedbackDialog(
                gameSuccessTitle,
                gameSuccessMessage,
                R.drawable.sorakan_leaderboard,
                true
            )
        } else {
            slotViews.forEachIndexed { index, slot ->
                val right = slot.tag == correctSteps[index]
                slot.background = roundedStrokeDrawable(
                    ContextCompat.getColor(this, R.color.white),
                    ContextCompat.getColor(this, if (right) R.color.algoplay_green_dark else R.color.algoplay_red_dark),
                    dp(16)
                )
            }
            showFeedbackDialog(
                gameWrongTitle,
                gameWrongMessage,
                R.drawable.hai_materi,
                false
            )
        }
    }

    protected fun showFeedbackDialog(
        title: String,
        message: String,
        imageRes: Int,
        success: Boolean,
        soundEffect: AlgoSound? = if (success) AlgoSound.BENAR else AlgoSound.SALAH,
        onClose: (() -> Unit)? = null
    ) {
        soundEffect?.let { playAlgoSound(it) }
        val dialog = Dialog(this)
        val root = FrameLayout(this).apply {
            setPadding(dp(22), 0, dp(22), 0)
        }
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            background = roundedDrawable(ContextCompat.getColor(this@LessonOneActivity, R.color.white), dp(26))
            elevation = dp(10).toFloat()
            setPadding(dp(20), dp(18), dp(20), dp(18))
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
            setImageResource(imageRes)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        card.addView(image, LinearLayout.LayoutParams(dp(116), dp(116)))

        card.addView(compactText(title, 21, R.color.algoplay_text, true, Gravity.CENTER), LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(8)
        })

        card.addView(compactText(message, 14, R.color.algoplay_subtext, false, Gravity.CENTER).apply {
            setLineSpacing(dp(2).toFloat(), 1f)
        }, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(8)
        })

        val close = compactText(if (success) "Mantap" else "Oke", 14, R.color.white, true, Gravity.CENTER).apply {
            background = roundedDrawable(
                ContextCompat.getColor(this@LessonOneActivity, if (success) R.color.algoplay_green_dark else R.color.algoplay_blue_dark),
                dp(16)
            )
            setOnClickListener {
                dialog.dismiss()
                onClose?.invoke()
            }
            enableTapFeedback()
        }
        card.addView(close, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)).apply {
            topMargin = dp(16)
        })

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
            image.animate().rotationBy(if (success) 5f else -5f).setDuration(320).withEndAction {
                image.animate().rotation(0f).setDuration(220).start()
            }.start()
        }
        dialog.show()
    }

    private fun loadRemotePhoto(photoUrl: String, target: ImageView) {
        thread {
            runCatching {
                URL(photoUrl).openStream().use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }.onSuccess { bitmap ->
                runOnUiThread {
                    target.clearColorFilter()
                    target.setImageBitmap(bitmap)
                    target.scaleType = ImageView.ScaleType.CENTER_CROP
                }
            }
        }
    }

    private fun scoreRank(score: Int): ScoreRank {
        return when {
            score >= 5500 -> ScoreRank(10, "Legend", "#22D3EE")
            score >= 4800 -> ScoreRank(9, "Grandmaster", "#FACC15")
            score >= 4100 -> ScoreRank(8, "Master", "#F97316")
            score >= 3400 -> ScoreRank(7, "Expert", "#EF4444")
            score >= 2700 -> ScoreRank(6, "Strategist", "#EC4899")
            score >= 2000 -> ScoreRank(5, "Problem Solver", "#7C3AED")
            score >= 1400 -> ScoreRank(4, "Challenger", "#8B5CF6")
            score >= 800 -> ScoreRank(3, "Explorer", "#3B82F6")
            score >= 300 -> ScoreRank(2, "Learner", "#38BDF8")
            else -> ScoreRank(1, "Beginner", "#4ADE80")
        }
    }

    protected fun compactText(value: String, sizeSp: Int, colorRes: Int, bold: Boolean, gravityValue: Int): TextView {
        return TextView(this).apply {
            text = value
            textSize = sizeSp.toFloat()
            setTextColor(ContextCompat.getColor(this@LessonOneActivity, colorRes))
            gravity = gravityValue
            includeFontPadding = false
            if (bold) setTypeface(null, Typeface.BOLD)
        }
    }

    protected fun roundedDrawable(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius.toFloat()
            setColor(color)
        }
    }

    protected fun roundedStrokeDrawable(fillColor: Int, strokeColor: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius.toFloat()
            setColor(fillColor)
            setStroke(dp(2), strokeColor)
        }
    }

    protected fun optionBlockDrawable(value: String): GradientDrawable {
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

    protected fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private data class ScoreRank(
        val level: Int,
        val status: String,
        val colorHex: String
    )

    companion object {
        const val EXTRA_GUEST_MODE = "extra_guest_mode"
        const val EXTRA_USER_NAME = "extra_user_name"
        const val EXTRA_SCORE = "extra_score"
        const val EXTRA_PHOTO_URL = "extra_photo_url"
        const val EXTRA_PHOTO_URI = "extra_photo_uri"
        const val EXTRA_ALREADY_COMPLETED = "extra_already_completed"
        const val EXTRA_COMPLETED_LESSON = "extra_completed_lesson"
    }
}
