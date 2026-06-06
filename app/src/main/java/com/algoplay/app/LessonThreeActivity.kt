package com.algoplay.app

import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

class LessonThreeActivity : LessonOneActivity() {

    override val layoutResId: Int = R.layout.activity_lesson_three
    override val lessonNumber: Int = 3
    override val nextLessonNumber: Int = 4

    override val quizOneOptions: List<String> = listOf(
        "A. Langkah-langkah dari awal sampai selesai",
        "B. Gambar yang berwarna-warni",
        "C. Permainan lompat-lompat",
        "D. Suara robot Algo"
    )
    override val quizOneCorrectIndex: Int = 0
    override val quizOneCorrectMessage: String =
        "Benar! Urutan kegiatan adalah langkah-langkah dari awal sampai selesai."
    override val quizOneWrongMessage: String =
        "Belum tepat. Urutan kegiatan itu langkah-langkah yang dilakukan secara berurutan."

    override val quizTwoOptions: List<String> = listOf(
        "A. Warnai dulu, lalu buat gambar",
        "B. Siapkan kertas, buat gambar, lalu warnai",
        "C. Simpan pensil, lalu menggambar",
        "D. Robek kertas, lalu warnai"
    )
    override val quizTwoCorrectIndex: Int = 1
    override val quizTwoCorrectMessage: String = "Hebat! Kamu tahu urutan menggambar yang benar."
    override val quizTwoWrongMessage: String =
        "Coba pikir lagi. Sebelum mewarnai, kita harus membuat gambar dulu."

    private val correctClickSteps = listOf(
        ClickStep("Ambil roti", R.drawable.img_bread_plain),
        ClickStep("Ambil selai", R.drawable.img_jam_jar),
        ClickStep("Oleskan selai ke roti", R.drawable.img_spread_jam),
        ClickStep("Tutup dengan roti lain", R.drawable.img_sandwich_stack),
        ClickStep("Roti selai siap dimakan", R.drawable.img_sandwich_ready)
    )

    private val shuffledClickSteps = listOf(
        correctClickSteps[2],
        correctClickSteps[0],
        correctClickSteps[4],
        correctClickSteps[1],
        correctClickSteps[3]
    )

    private val indicatorViews = mutableListOf<TextView>()
    private var progressText: TextView? = null
    private var nextStepIndex = 0

    override fun buildGame() {
        gameChoiceContainer.removeAllViews()
        gameSlotContainer.removeAllViews()
        indicatorViews.clear()
        nextStepIndex = 0

        btnResetGame.text = "Ulangi"
        btnCheckGame.text = "Cek Urutan"

        progressText = TextView(this).apply {
            text = "Klik gambar langkah pertama: Ambil roti"
            gravity = Gravity.CENTER
            includeFontPadding = false
            setTextColor(ContextCompat.getColor(this@LessonThreeActivity, R.color.algoplay_text))
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            background = roundedStrokeDrawable(
                ContextCompat.getColor(this@LessonThreeActivity, R.color.algoplay_blue_soft),
                ContextCompat.getColor(this@LessonThreeActivity, R.color.algoplay_blue_dark),
                dp(16)
            )
            setPadding(dp(12), dp(10), dp(12), dp(10))
        }
        gameSlotContainer.addView(progressText, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        val indicatorRow = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            orientation = LinearLayout.HORIZONTAL
        }
        gameSlotContainer.addView(indicatorRow, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(36)
        ).apply {
            topMargin = dp(10)
        })

        correctClickSteps.forEachIndexed { index, _ ->
            val indicator = TextView(this).apply {
                text = (index + 1).toString()
                gravity = Gravity.CENTER
                includeFontPadding = false
                setTextColor(ContextCompat.getColor(this@LessonThreeActivity, R.color.algoplay_subtext))
                textSize = 12f
                setTypeface(null, Typeface.BOLD)
                background = roundedStrokeDrawable(
                    ContextCompat.getColor(this@LessonThreeActivity, R.color.white),
                    ContextCompat.getColor(this@LessonThreeActivity, R.color.algoplay_blue_soft),
                    dp(18)
                )
            }
            indicatorViews.add(indicator)
            indicatorRow.addView(indicator, LinearLayout.LayoutParams(dp(34), dp(34)).apply {
                leftMargin = dp(4)
                rightMargin = dp(4)
            })
        }

        val rows = shuffledClickSteps.chunked(2)
        rows.forEach { rowItems ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            gameChoiceContainer.addView(row, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(9)
            })

            rowItems.forEach { step ->
                row.addView(createClickableStepCard(step), LinearLayout.LayoutParams(
                    0,
                    dp(142),
                    1f
                ).apply {
                    leftMargin = dp(4)
                    rightMargin = dp(4)
                })
            }

            if (rowItems.size == 1) {
                row.addView(View(this), LinearLayout.LayoutParams(
                    0,
                    dp(142),
                    1f
                ).apply {
                    leftMargin = dp(4)
                    rightMargin = dp(4)
                })
            }
        }
    }

    override fun checkGameAnswer() {
        if (nextStepIndex == correctClickSteps.size) {
            showGameSuccess()
        } else {
            showFeedbackDialog(
                "Belum selesai",
                "Klik gambar sesuai urutan sampai angka 1 sampai 5 muncul semua, ya.",
                R.drawable.hai_materi,
                false
            )
        }
    }

    private fun createClickableStepCard(step: ClickStep): FrameLayout {
        val badge = TextView(this).apply {
            visibility = View.GONE
            gravity = Gravity.CENTER
            includeFontPadding = false
            setTextColor(ContextCompat.getColor(this@LessonThreeActivity, R.color.white))
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            background = roundedDrawable(
                ContextCompat.getColor(this@LessonThreeActivity, R.color.algoplay_green_dark),
                dp(16)
            )
        }

        val cardContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = roundedStrokeDrawable(
                ContextCompat.getColor(this@LessonThreeActivity, R.color.white),
                ContextCompat.getColor(this@LessonThreeActivity, R.color.algoplay_blue_soft),
                dp(18)
            )
            setPadding(dp(10), dp(8), dp(10), dp(8))
        }

        cardContent.addView(ImageView(this).apply {
            setImageResource(step.imageRes)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = step.label
        }, LinearLayout.LayoutParams(dp(76), dp(76)))

        cardContent.addView(TextView(this).apply {
            text = step.label
            gravity = Gravity.CENTER
            includeFontPadding = false
            setTextColor(ContextCompat.getColor(this@LessonThreeActivity, R.color.algoplay_text))
            textSize = 12f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(8), 0, 0)
        }, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        return FrameLayout(this).apply {
            isClickable = true
            isFocusable = true
            addView(cardContent, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ))
            addView(badge, FrameLayout.LayoutParams(dp(32), dp(32), Gravity.TOP or Gravity.END).apply {
                topMargin = dp(6)
                rightMargin = dp(6)
            })
            setOnClickListener {
                handleStepClick(step, this, badge, cardContent)
            }
        }
    }

    private fun handleStepClick(
        step: ClickStep,
        card: FrameLayout,
        badge: TextView,
        cardContent: LinearLayout
    ) {
        if (nextStepIndex >= correctClickSteps.size || !card.isEnabled) {
            return
        }

        val expected = correctClickSteps[nextStepIndex]
        if (step.label == expected.label) {
            val selectedNumber = nextStepIndex + 1
            badge.text = selectedNumber.toString()
            badge.visibility = View.VISIBLE
            card.isEnabled = false
            cardContent.background = roundedStrokeDrawable(
                ContextCompat.getColor(this, R.color.algoplay_blue_soft),
                ContextCompat.getColor(this, R.color.algoplay_green_dark),
                dp(18)
            )
            indicatorViews[nextStepIndex].background = roundedDrawable(
                ContextCompat.getColor(this, R.color.algoplay_green_dark),
                dp(18)
            )
            indicatorViews[nextStepIndex].setTextColor(ContextCompat.getColor(this, R.color.white))
            nextStepIndex++
            updateProgressHint()

            if (nextStepIndex == correctClickSteps.size) {
                showGameSuccess()
            }
        } else {
            card.animate()
                .translationX(dp(8).toFloat())
                .setDuration(60)
                .withEndAction {
                    card.animate()
                        .translationX((-dp(8)).toFloat())
                        .setDuration(60)
                        .withEndAction {
                            card.animate().translationX(0f).setDuration(60).start()
                        }
                        .start()
                }
                .start()
            showFeedbackDialog(
                "Ups!",
                "Sepertinya ada langkah yang tertukar. Coba klik dari langkah pertama lagi ya!",
                R.drawable.hai_materi,
                false
            ) {
                buildGame()
            }
        }
    }

    private fun updateProgressHint() {
        progressText?.text = if (nextStepIndex < correctClickSteps.size) {
            "Lanjut klik langkah ${nextStepIndex + 1}: ${correctClickSteps[nextStepIndex].label}"
        } else {
            "Semua langkah sudah benar. Roti selai siap!"
        }
    }

    private fun showGameSuccess() {
        showFeedbackDialog(
            "Nyam nyam!",
            "Roti selai berhasil dibuat. Urutanmu benar!",
            R.drawable.sorakan_leaderboard,
            true
        )
    }

    private data class ClickStep(
        val label: String,
        val imageRes: Int
    )
}
