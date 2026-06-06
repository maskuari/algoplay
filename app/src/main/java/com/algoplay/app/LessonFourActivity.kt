package com.algoplay.app

import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

class LessonFourActivity : LessonOneActivity() {

    override val layoutResId: Int = R.layout.activity_lesson_four
    override val lessonNumber: Int = 4
    override val nextLessonNumber: Int = 5

    override val quizOneOptions: List<String> = listOf(
        "A. Jelas dan mudah dimengerti",
        "B. Panjang dan membingungkan",
        "C. Acak-acakan",
        "D. Tidak perlu urut"
    )
    override val quizOneCorrectIndex: Int = 0
    override val quizOneCorrectMessage: String = "Benar! Langkah harus jelas supaya mudah diikuti."
    override val quizOneWrongMessage: String =
        "Belum tepat. Langkah yang baik harus jelas dan mudah dimengerti."

    override val quizTwoOptions: List<String> = listOf(
        "A. Pokoknya selesai.",
        "B. Lakukan saja.",
        "C. Ambil sikat gigi, lalu beri pasta gigi.",
        "D. Benda itu dipakai."
    )
    override val quizTwoCorrectIndex: Int = 2
    override val quizTwoCorrectMessage: String =
        "Pintar! Itu contoh langkah yang jelas dan mudah diikuti."
    override val quizTwoWrongMessage: String =
        "Coba cari kalimat yang menjelaskan kegiatan dengan lebih jelas."

    private val juiceSteps = listOf(
        WordStep("Ambil", "buah dari meja."),
        WordStep("Potong", "buah menjadi kecil."),
        WordStep("Masukkan", "buah ke blender."),
        WordStep("Tuang", "air secukupnya."),
        WordStep("Tekan", "tombol blender.")
    )

    private val choices = listOf("Masukkan", "Ambil", "Tekan", "Tuang", "Potong")
    private val sentenceViews = mutableListOf<TextView>()
    private val selectedWords = MutableList(juiceSteps.size) { "" }
    private var currentBlankIndex = 0
    private var hintText: TextView? = null

    override fun buildGame() {
        gameChoiceContainer.removeAllViews()
        gameSlotContainer.removeAllViews()
        sentenceViews.clear()
        selectedWords.fill("")
        currentBlankIndex = 0

        btnResetGame.text = "Ulangi"
        btnCheckGame.text = "Cek Langkah"

        hintText = TextView(this).apply {
            text = "Pilih kata untuk langkah 1: ___ buah dari meja."
            gravity = Gravity.CENTER
            includeFontPadding = false
            setTextColor(ContextCompat.getColor(this@LessonFourActivity, R.color.algoplay_text))
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = roundedStrokeDrawable(
                ContextCompat.getColor(this@LessonFourActivity, R.color.algoplay_blue_soft),
                ContextCompat.getColor(this@LessonFourActivity, R.color.algoplay_blue_dark),
                dp(16)
            )
        }
        gameSlotContainer.addView(hintText, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        juiceSteps.forEachIndexed { index, step ->
            val sentence = TextView(this).apply {
                text = "${index + 1}. ___ ${step.tail}"
                gravity = Gravity.CENTER_VERTICAL
                includeFontPadding = false
                setPadding(dp(14), 0, dp(14), 0)
                setTextColor(ContextCompat.getColor(this@LessonFourActivity, R.color.algoplay_subtext))
                textSize = 13f
                background = roundedStrokeDrawable(
                    ContextCompat.getColor(this@LessonFourActivity, R.color.white),
                    ContextCompat.getColor(this@LessonFourActivity, R.color.algoplay_blue_soft),
                    dp(16)
                )
            }
            sentenceViews.add(sentence)
            gameSlotContainer.addView(sentence, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(48)
            ).apply {
                topMargin = dp(8)
            })
        }

        val rows = choices.chunked(2)
        rows.forEach { rowChoices ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            gameChoiceContainer.addView(row, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(8)
            })

            rowChoices.forEach { word ->
                row.addView(createWordChoice(word), LinearLayout.LayoutParams(
                    0,
                    dp(46),
                    1f
                ).apply {
                    leftMargin = dp(4)
                    rightMargin = dp(4)
                })
            }

            if (rowChoices.size == 1) {
                row.addView(View(this), LinearLayout.LayoutParams(0, dp(46), 1f).apply {
                    leftMargin = dp(4)
                    rightMargin = dp(4)
                })
            }
        }
    }

    override fun checkGameAnswer() {
        if (selectedWords == juiceSteps.map { it.answer }) {
            showGameSuccess()
        } else {
            showFeedbackDialog(
                "Belum lengkap",
                "Lengkapi semua langkah dengan kata yang tepat dulu, ya.",
                R.drawable.hai_materi,
                false
            )
        }
    }

    private fun createWordChoice(word: String): TextView {
        return TextView(this).apply {
            text = word
            gravity = Gravity.CENTER
            includeFontPadding = false
            setTextColor(ContextCompat.getColor(this@LessonFourActivity, R.color.algoplay_text))
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            background = roundedStrokeDrawable(
                ContextCompat.getColor(this@LessonFourActivity, R.color.white),
                ContextCompat.getColor(this@LessonFourActivity, R.color.algoplay_blue_soft),
                dp(16)
            )
            setOnClickListener {
                handleWordChoice(word, this)
            }
        }
    }

    private fun handleWordChoice(word: String, button: TextView) {
        if (currentBlankIndex >= juiceSteps.size || !button.isEnabled) {
            return
        }

        val expected = juiceSteps[currentBlankIndex]
        if (word == expected.answer) {
            selectedWords[currentBlankIndex] = word
            sentenceViews[currentBlankIndex].apply {
                text = "${currentBlankIndex + 1}. $word ${expected.tail}"
                setTextColor(ContextCompat.getColor(this@LessonFourActivity, R.color.algoplay_text))
                background = roundedStrokeDrawable(
                    ContextCompat.getColor(this@LessonFourActivity, R.color.algoplay_blue_soft),
                    ContextCompat.getColor(this@LessonFourActivity, R.color.algoplay_green_dark),
                    dp(16)
                )
            }
            button.isEnabled = false
            button.alpha = 0.45f
            button.background = roundedDrawable(
                ContextCompat.getColor(this, R.color.algoplay_green_dark),
                dp(16)
            )
            button.setTextColor(ContextCompat.getColor(this, R.color.white))
            currentBlankIndex++
            updateHint()

            if (currentBlankIndex == juiceSteps.size) {
                showGameSuccess()
            }
        } else {
            button.animate()
                .translationX(dp(8).toFloat())
                .setDuration(60)
                .withEndAction {
                    button.animate()
                        .translationX((-dp(8)).toFloat())
                        .setDuration(60)
                        .withEndAction {
                            button.animate().translationX(0f).setDuration(60).start()
                        }
                        .start()
                }
                .start()
            showFeedbackDialog(
                "Ups!",
                "Kata itu belum cocok. Coba pilih kata yang membuat langkahnya lebih jelas ya!",
                R.drawable.hai_materi,
                false
            )
        }
    }

    private fun updateHint() {
        hintText?.text = if (currentBlankIndex < juiceSteps.size) {
            val step = juiceSteps[currentBlankIndex]
            "Pilih kata untuk langkah ${currentBlankIndex + 1}: ___ ${step.tail}"
        } else {
            "Semua langkah sudah jelas. Robot Algo siap membuat jus!"
        }
    }

    private fun showGameSuccess() {
        showFeedbackDialog(
            "Yeay!",
            "Langkahmu jelas sekali. Robot Algo berhasil membuat jus!",
            R.drawable.sorakan_leaderboard,
            true
        )
    }

    private data class WordStep(
        val answer: String,
        val tail: String
    )
}
