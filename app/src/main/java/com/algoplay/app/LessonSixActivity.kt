package com.algoplay.app

import android.graphics.Typeface
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

class LessonSixActivity : LessonOneActivity() {

    override val layoutResId: Int = R.layout.activity_lesson_six
    override val lessonNumber: Int = 6
    override val nextLessonNumber: Int = 7

    override val quizOneOptions: List<String> = listOf(
        "A. Gambar alur langkah-langkah",
        "B. Lagu untuk robot",
        "C. Warna pada layar",
        "D. Nama makanan"
    )
    override val quizOneCorrectIndex: Int = 0
    override val quizOneCorrectMessage: String =
        "Benar! Flowchart adalah gambar alur langkah-langkah."
    override val quizOneWrongMessage: String =
        "Belum tepat. Flowchart itu gambar yang menunjukkan urutan langkah."

    override val quizTwoOptions: List<String> = listOf(
        "A. Menunjukkan arah langkah berikutnya",
        "B. Menghias layar saja",
        "C. Membuat robot tidur",
        "D. Menghapus langkah"
    )
    override val quizTwoCorrectIndex: Int = 0
    override val quizTwoCorrectMessage: String =
        "Pintar! Panah menunjukkan langkah mana yang harus diikuti berikutnya."
    override val quizTwoWrongMessage: String =
        "Coba ingat lagi. Panah membantu kita tahu arah langkah selanjutnya."

    private val correctPath = listOf(
        "MULAI",
        "Basahi tangan",
        "Ambil sabun",
        "Gosok tangan",
        "Bilas tangan",
        "Keringkan tangan",
        "SELESAI"
    )

    private val displayBoxes = listOf(
        "Gosok tangan",
        "MULAI",
        "Bilas tangan",
        "Ambil sabun",
        "SELESAI",
        "Basahi tangan",
        "Keringkan tangan"
    )

    private val selectedBoxes = mutableListOf<String>()
    private var pathHint: TextView? = null

    override fun buildGame() {
        gameChoiceContainer.removeAllViews()
        gameSlotContainer.removeAllViews()
        selectedBoxes.clear()

        btnResetGame.text = "Ulangi"
        btnCheckGame.text = "Cek Alur"

        pathHint = TextView(this).apply {
            text = "Mulai dari kotak: MULAI"
            gravity = Gravity.CENTER
            includeFontPadding = false
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setTextColor(ContextCompat.getColor(this@LessonSixActivity, R.color.algoplay_text))
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            background = roundedStrokeDrawable(
                ContextCompat.getColor(this@LessonSixActivity, R.color.algoplay_blue_soft),
                ContextCompat.getColor(this@LessonSixActivity, R.color.algoplay_blue_dark),
                dp(16)
            )
        }
        gameSlotContainer.addView(pathHint, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        gameSlotContainer.addView(TextView(this).apply {
            text = "Alur panah: MULAI -> Basahi tangan -> Ambil sabun -> Gosok tangan -> Bilas tangan -> Keringkan tangan -> SELESAI"
            gravity = Gravity.CENTER
            setPadding(dp(10), dp(10), dp(10), dp(10))
            setTextColor(ContextCompat.getColor(this@LessonSixActivity, R.color.algoplay_subtext))
            textSize = 12f
            setLineSpacing(dp(2).toFloat(), 1f)
            background = roundedStrokeDrawable(
                ContextCompat.getColor(this@LessonSixActivity, R.color.white),
                ContextCompat.getColor(this@LessonSixActivity, R.color.algoplay_blue_soft),
                dp(16)
            )
        }, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(8)
        })

        displayBoxes.chunked(2).forEach { rowItems ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            gameChoiceContainer.addView(row, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(9)
            })

            rowItems.forEach { label ->
                row.addView(createFlowBox(label), LinearLayout.LayoutParams(
                    0,
                    dp(72),
                    1f
                ).apply {
                    leftMargin = dp(4)
                    rightMargin = dp(4)
                })
            }
        }
    }

    override fun checkGameAnswer() {
        if (selectedBoxes == correctPath) {
            showSuccess()
        } else {
            showFeedbackDialog(
                "Belum selesai",
                "Ketuk kotak sesuai urutan panah dari MULAI sampai SELESAI dulu, ya.",
                R.drawable.hai_materi,
                false
            )
        }
    }

    private fun createFlowBox(label: String): TextView {
        return TextView(this).apply {
            text = label
            gravity = Gravity.CENTER
            includeFontPadding = false
            setPadding(dp(8), 0, dp(8), 0)
            setTextColor(ContextCompat.getColor(this@LessonSixActivity, R.color.algoplay_text))
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            background = roundedStrokeDrawable(
                ContextCompat.getColor(this@LessonSixActivity, R.color.white),
                ContextCompat.getColor(this@LessonSixActivity, R.color.algoplay_blue_soft),
                dp(16)
            )
            setOnClickListener {
                handleFlowBoxTap(label, this)
            }
        }
    }

    private fun handleFlowBoxTap(label: String, box: TextView) {
        if (!box.isEnabled) return

        val expected = correctPath.getOrNull(selectedBoxes.size) ?: return
        if (label == expected) {
            selectedBoxes.add(label)
            box.isEnabled = false
            box.background = roundedStrokeDrawable(
                ContextCompat.getColor(this, R.color.algoplay_blue_soft),
                ContextCompat.getColor(this, R.color.algoplay_green_dark),
                dp(16)
            )
            box.setTextColor(ContextCompat.getColor(this, R.color.algoplay_green_dark))
            updateHint()

            if (selectedBoxes == correctPath) {
                showSuccess()
            }
        } else {
            box.animate()
                .translationX(dp(8).toFloat())
                .setDuration(60)
                .withEndAction {
                    box.animate()
                        .translationX((-dp(8)).toFloat())
                        .setDuration(60)
                        .withEndAction {
                            box.animate().translationX(0f).setDuration(60).start()
                        }
                        .start()
                }
                .start()
            showFeedbackDialog(
                "Ups!",
                "Coba ikuti arah panahnya lagi ya. Panah membantu Robot Algo menemukan langkah berikutnya.",
                R.drawable.hai_materi,
                false
            )
        }
    }

    private fun updateHint() {
        pathHint?.text = if (selectedBoxes.size < correctPath.size) {
            "Lanjut ke kotak: ${correctPath[selectedBoxes.size]}"
        } else {
            "Flowchart sudah sampai SELESAI!"
        }
    }

    private fun showSuccess() {
        showFeedbackDialog(
            "Yeay!",
            "Robot Algo berhasil mengikuti flowchart sampai selesai!",
            R.drawable.sorakan_leaderboard,
            true
        )
    }
}
