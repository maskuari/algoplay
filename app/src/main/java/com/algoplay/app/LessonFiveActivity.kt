package com.algoplay.app

import android.graphics.Typeface
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

class LessonFiveActivity : LessonOneActivity() {

    override val layoutResId: Int = R.layout.activity_lesson_five
    override val lessonNumber: Int = 5
    override val nextLessonNumber: Int = 6

    override val quizOneOptions: List<String> = listOf(
        "A. Gambar mainan robot",
        "B. Langkah-langkah seperti kode, tapi mudah dibaca",
        "C. Lagu untuk komputer",
        "D. Warna pada layar"
    )
    override val quizOneCorrectIndex: Int = 1
    override val quizOneCorrectMessage: String =
        "Benar! Pseudocode itu seperti kode sederhana yang masih mudah dibaca."
    override val quizOneWrongMessage: String =
        "Belum tepat. Pseudocode adalah langkah-langkah seperti kode, tapi memakai bahasa yang mudah dimengerti."

    override val quizTwoOptions: List<String> = listOf(
        "A. SELESAI",
        "B. LOMPAT",
        "C. MULAI",
        "D. TIDUR"
    )
    override val quizTwoCorrectIndex: Int = 2
    override val quizTwoCorrectMessage: String =
        "Pintar! Pseudocode biasanya diawali dengan MULAI."
    override val quizTwoWrongMessage: String =
        "Coba ingat lagi. Pseudocode biasanya dimulai dari kata MULAI."

    private val options = listOf(
        PseudocodeOption(
            "Pilihan A",
            "MULAI\nBasahi tangan\nAmbil sabun\nGosok tangan\nBilas tangan\nKeringkan tangan\nSELESAI",
            true
        ),
        PseudocodeOption(
            "Pilihan B",
            "SELESAI\nGosok tangan\nMULAI\nAmbil sabun\nBasahi tangan",
            false
        ),
        PseudocodeOption(
            "Pilihan C",
            "MULAI\nKeringkan tangan\nBilas tangan\nAmbil sabun\nSELESAI",
            false
        )
    )

    private var selectedCorrect = false

    override fun buildGame() {
        gameChoiceContainer.removeAllViews()
        gameSlotContainer.removeAllViews()
        selectedCorrect = false

        btnResetGame.text = "Ulangi"
        btnCheckGame.text = "Cek Pilihan"

        val prompt = TextView(this).apply {
            text = "Manakah pseudocode yang benar untuk mencuci tangan?"
            gravity = Gravity.CENTER
            includeFontPadding = false
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setTextColor(ContextCompat.getColor(this@LessonFiveActivity, R.color.algoplay_text))
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            background = roundedStrokeDrawable(
                ContextCompat.getColor(this@LessonFiveActivity, R.color.algoplay_blue_soft),
                ContextCompat.getColor(this@LessonFiveActivity, R.color.algoplay_blue_dark),
                dp(16)
            )
        }
        gameSlotContainer.addView(prompt, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        options.forEach { option ->
            gameChoiceContainer.addView(createOptionCard(option), LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(9)
            })
        }
    }

    override fun checkGameAnswer() {
        if (selectedCorrect) {
            showSuccess()
        } else {
            showFeedbackDialog(
                "Belum dipilih",
                "Pilih kartu pseudocode yang menurutmu paling rapi dulu, ya.",
                R.drawable.hai_materi,
                false
            )
        }
    }

    private fun createOptionCard(option: PseudocodeOption): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            isClickable = true
            isFocusable = true
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = roundedStrokeDrawable(
                ContextCompat.getColor(this@LessonFiveActivity, R.color.white),
                ContextCompat.getColor(this@LessonFiveActivity, R.color.algoplay_blue_soft),
                dp(18)
            )

            addView(TextView(this@LessonFiveActivity).apply {
                text = option.title
                includeFontPadding = false
                setTextColor(ContextCompat.getColor(this@LessonFiveActivity, R.color.algoplay_blue_dark))
                textSize = 14f
                setTypeface(null, Typeface.BOLD)
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ))

            addView(TextView(this@LessonFiveActivity).apply {
                text = option.code
                setPadding(0, dp(8), 0, 0)
                setTextColor(ContextCompat.getColor(this@LessonFiveActivity, R.color.algoplay_text))
                textSize = 13f
                typeface = Typeface.MONOSPACE
                setLineSpacing(dp(2).toFloat(), 1f)
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ))

            setOnClickListener {
                if (option.isCorrect) {
                    selectedCorrect = true
                    background = roundedStrokeDrawable(
                        ContextCompat.getColor(this@LessonFiveActivity, R.color.algoplay_blue_soft),
                        ContextCompat.getColor(this@LessonFiveActivity, R.color.algoplay_green_dark),
                        dp(18)
                    )
                    showSuccess()
                } else {
                    animate()
                        .translationX(dp(8).toFloat())
                        .setDuration(60)
                        .withEndAction {
                            animate()
                                .translationX((-dp(8)).toFloat())
                                .setDuration(60)
                                .withEndAction {
                                    animate().translationX(0f).setDuration(60).start()
                                }
                                .start()
                        }
                        .start()
                    showFeedbackDialog(
                        "Ups!",
                        "Coba perhatikan lagi. Pseudocode yang benar harus dimulai dari MULAI dan langkahnya harus urut.",
                        R.drawable.hai_materi,
                        false
                    )
                }
            }
        }
    }

    private fun showSuccess() {
        showFeedbackDialog(
            "Hebat!",
            "Kamu memilih pseudocode yang rapi dan benar. Tangan Robot Algo jadi bersih!",
            R.drawable.sorakan_leaderboard,
            true
        )
    }

    private data class PseudocodeOption(
        val title: String,
        val code: String,
        val isCorrect: Boolean
    )
}
