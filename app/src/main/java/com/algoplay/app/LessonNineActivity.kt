package com.algoplay.app

import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

class LessonNineActivity : LessonOneActivity() {

    override val layoutResId: Int = R.layout.activity_lesson_nine
    override val lessonNumber: Int = 9
    override val nextLessonNumber: Int = 10

    override val quizOneOptions: List<String> = listOf(
        "A. Membawa payung",
        "B. Membawa bantal",
        "C. Melepas sepatu",
        "D. Tidur di jalan"
    )
    override val quizOneCorrectIndex: Int = 0
    override val quizOneCorrectMessage: String =
        "Benar! Kalau hujan, kita bisa membawa payung agar tidak basah."
    override val quizOneWrongMessage: String =
        "Belum tepat. Kalau hujan, kita perlu sesuatu agar tidak kehujanan."

    override val quizTwoOptions: List<String> = listOf(
        "A. Iya atau Tidak",
        "B. Merah atau Biru saja",
        "C. Lompat atau Tidur",
        "D. Makan atau Hilang"
    )
    override val quizTwoCorrectIndex: Int = 0
    override val quizTwoCorrectMessage: String =
        "Pintar! Pilihan biasanya memakai jawaban IYA atau TIDAK."
    override val quizTwoWrongMessage: String =
        "Coba ingat lagi. Dalam algoritma, pilihan sering memakai IYA atau TIDAK."

    private val rounds = listOf(
        WeatherRound(
            title = "Langit hujan",
            weather = "HUJAN",
            correctAnswer = "IYA",
            correctStep = "Bawa payung",
            wrongMessage = "Ups! Coba lihat cuacanya lagi. Kalau hujan, apa yang sebaiknya dibawa?"
        ),
        WeatherRound(
            title = "Langit cerah",
            weather = "CERAH",
            correctAnswer = "TIDAK",
            correctStep = "Pergi bermain",
            wrongMessage = "Hmm, langitnya cerah. Apakah Robot Algo masih perlu payung?"
        )
    )

    private var currentRoundIndex = 0
    private val completedRounds = mutableSetOf<Int>()
    private var selectedAnswer: String? = null
    private var selectedStep: String? = null
    private var hintText: TextView? = null
    private var weatherCard: TextView? = null

    override fun buildGame() {
        gameChoiceContainer.removeAllViews()
        gameSlotContainer.removeAllViews()
        completedRounds.clear()
        currentRoundIndex = 0
        selectedAnswer = null
        selectedStep = null

        btnResetGame.text = "Ulangi"
        btnCheckGame.text = "Cek Pilihan"
        renderRound()
    }

    override fun checkGameAnswer() {
        if (completedRounds.size == rounds.size) {
            showSuccess()
        } else {
            showFeedbackDialog(
                "Belum selesai",
                "Bantu Robot Algo menyelesaikan kondisi hujan dan cerah dulu, ya.",
                R.drawable.hai_materi,
                false
            )
        }
    }

    private fun renderRound() {
        gameSlotContainer.removeAllViews()
        gameChoiceContainer.removeAllViews()
        selectedAnswer = null
        selectedStep = null

        val round = rounds[currentRoundIndex]

        hintText = TextView(this).apply {
            text = "Pertanyaan: Apakah hari ini hujan?"
            gravity = Gravity.CENTER
            includeFontPadding = false
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setTextColor(ContextCompat.getColor(this@LessonNineActivity, R.color.algoplay_text))
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            background = roundedStrokeDrawable(
                ContextCompat.getColor(this@LessonNineActivity, R.color.algoplay_blue_soft),
                ContextCompat.getColor(this@LessonNineActivity, R.color.algoplay_blue_dark),
                dp(16)
            )
        }
        gameSlotContainer.addView(hintText, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        weatherCard = TextView(this).apply {
            text = "${round.title}\n${round.weather}"
            gravity = Gravity.CENTER
            includeFontPadding = false
            setPadding(dp(16), dp(18), dp(16), dp(18))
            setTextColor(ContextCompat.getColor(this@LessonNineActivity, R.color.algoplay_text))
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setLineSpacing(dp(4).toFloat(), 1f)
            background = roundedStrokeDrawable(
                ContextCompat.getColor(this@LessonNineActivity, R.color.white),
                ContextCompat.getColor(this@LessonNineActivity, R.color.algoplay_blue_soft),
                dp(20)
            )
        }
        gameSlotContainer.addView(weatherCard, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(10)
        })

        gameChoiceContainer.addView(sectionLabel("Pilih jawaban"), LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(8)
        })
        gameChoiceContainer.addView(choiceRow(listOf("IYA", "TIDAK"), isAnswer = true), LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(50)
        ).apply {
            topMargin = dp(8)
        })

        gameChoiceContainer.addView(sectionLabel("Pilih langkah"), LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(14)
        })
        gameChoiceContainer.addView(choiceRow(listOf("Bawa payung", "Pergi bermain"), isAnswer = false), LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(54)
        ).apply {
            topMargin = dp(8)
        })
    }

    private fun sectionLabel(value: String): TextView {
        return TextView(this).apply {
            text = value
            includeFontPadding = false
            setTextColor(ContextCompat.getColor(this@LessonNineActivity, R.color.algoplay_blue_dark))
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
        }
    }

    private fun choiceRow(values: List<String>, isAnswer: Boolean): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            values.forEach { value ->
                addView(choiceButton(value, isAnswer), LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
                ).apply {
                    leftMargin = dp(4)
                    rightMargin = dp(4)
                })
            }
        }
    }

    private fun choiceButton(value: String, isAnswer: Boolean): TextView {
        return TextView(this).apply {
            text = value
            gravity = Gravity.CENTER
            includeFontPadding = false
            setPadding(dp(8), 0, dp(8), 0)
            setTextColor(ContextCompat.getColor(this@LessonNineActivity, R.color.algoplay_text))
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            background = normalBackground()
            setOnClickListener {
                if (isAnswer) {
                    handleAnswer(value, this)
                } else {
                    handleStep(value, this)
                }
            }
        }
    }

    private fun handleAnswer(value: String, button: TextView) {
        val round = rounds[currentRoundIndex]
        if (value == round.correctAnswer) {
            selectedAnswer = value
            lockChoice(button)
            hintText?.text = "Benar. Sekarang pilih langkah yang cocok."
        } else {
            shake(button)
            blinkWeather()
            showFeedbackDialog("Ups!", round.wrongMessage, R.drawable.hai_materi, false)
        }
    }

    private fun handleStep(value: String, button: TextView) {
        val round = rounds[currentRoundIndex]
        if (selectedAnswer == null) {
            shake(button)
            showFeedbackDialog(
                "Pilih jawaban dulu",
                "Jawab dulu apakah hari ini hujan: IYA atau TIDAK.",
                R.drawable.hai_materi,
                false
            )
            return
        }

        if (value == round.correctStep) {
            selectedStep = value
            lockChoice(button)
            completedRounds.add(currentRoundIndex)
            if (completedRounds.size == rounds.size) {
                showSuccess()
            } else {
                showFeedbackDialog(
                    "Bagus!",
                    "Pilihanmu cocok. Sekarang coba kondisi berikutnya.",
                    R.drawable.sorakan_leaderboard,
                    true
                ) {
                    currentRoundIndex++
                    renderRound()
                }
            }
        } else {
            shake(button)
            blinkWeather()
            showFeedbackDialog("Ups!", round.wrongMessage, R.drawable.hai_materi, false)
        }
    }

    private fun lockChoice(button: TextView) {
        button.isEnabled = false
        button.setTextColor(ContextCompat.getColor(this, R.color.algoplay_green_dark))
        button.background = roundedStrokeDrawable(
            ContextCompat.getColor(this, R.color.algoplay_blue_soft),
            ContextCompat.getColor(this, R.color.algoplay_green_dark),
            dp(16)
        )
    }

    private fun normalBackground() = roundedStrokeDrawable(
        ContextCompat.getColor(this, R.color.white),
        ContextCompat.getColor(this, R.color.algoplay_blue_soft),
        dp(16)
    )

    private fun blinkWeather() {
        weatherCard?.animate()
            ?.alpha(0.45f)
            ?.setDuration(90)
            ?.withEndAction {
                weatherCard?.animate()?.alpha(1f)?.setDuration(120)?.start()
            }
            ?.start()
    }

    private fun shake(view: View) {
        view.animate()
            .translationX(dp(8).toFloat())
            .setDuration(60)
            .withEndAction {
                view.animate()
                    .translationX((-dp(8)).toFloat())
                    .setDuration(60)
                    .withEndAction {
                        view.animate().translationX(0f).setDuration(60).start()
                    }
                    .start()
            }
            .start()
    }

    private fun showSuccess() {
        showFeedbackDialog(
            "Yeay!",
            "Pilihanmu tepat. Robot Algo bisa pergi dengan aman!",
            R.drawable.sorakan_leaderboard,
            true
        )
    }

    private data class WeatherRound(
        val title: String,
        val weather: String,
        val correctAnswer: String,
        val correctStep: String,
        val wrongMessage: String
    )
}
