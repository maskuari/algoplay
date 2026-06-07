package com.algoplay.app

import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

class LessonEightActivity : LessonOneActivity() {

    override val layoutResId: Int = R.layout.activity_lesson_eight
    override val lessonNumber: Int = 8
    override val nextLessonNumber: Int = 9

    override val quizOneOptions: List<String> = listOf(
        "A. Panah yang jelas",
        "B. Panah yang berantakan",
        "C. Tulisan yang sangat panjang",
        "D. Simbol yang acak"
    )
    override val quizOneCorrectIndex: Int = 0
    override val quizOneCorrectMessage: String =
        "Benar! Panah yang jelas membuat flowchart mudah diikuti."
    override val quizOneWrongMessage: String =
        "Belum tepat. Flowchart yang rapi harus punya panah yang jelas."

    override val quizTwoOptions: List<String> = listOf(
        "A. Singkat dan jelas",
        "B. Sangat panjang",
        "C. Acak-acakan",
        "D. Tidak bisa dibaca"
    )
    override val quizTwoCorrectIndex: Int = 0
    override val quizTwoCorrectMessage: String =
        "Pintar! Teks yang singkat membuat flowchart lebih mudah dibaca."
    override val quizTwoWrongMessage: String =
        "Coba ingat lagi. Teks di flowchart sebaiknya pendek dan jelas."

    private val wrongParts = setOf("wrong_arrow", "long_text", "missing_finish")
    private val foundParts = mutableSetOf<String>()
    private var hintText: TextView? = null
    private var tidyFlowchartText: TextView? = null

    override fun buildGame() {
        gameChoiceContainer.removeAllViews()
        gameSlotContainer.removeAllViews()
        foundParts.clear()

        btnResetGame.text = "Ulangi"
        btnCheckGame.text = "Cek Temuan"

        hintText = TextView(this).apply {
            text = "Temukan 3 bagian yang membuat flowchart ini belum rapi."
            gravity = Gravity.CENTER
            includeFontPadding = false
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setTextColor(ContextCompat.getColor(this@LessonEightActivity, R.color.algoplay_text))
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            background = roundedStrokeDrawable(
                ContextCompat.getColor(this@LessonEightActivity, R.color.algoplay_blue_soft),
                ContextCompat.getColor(this@LessonEightActivity, R.color.algoplay_blue_dark),
                dp(16)
            )
        }
        gameSlotContainer.addView(hintText, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        listOf(
            GamePart("start", "[MULAI]", false),
            GamePart("arrow_down_1", "|", false),
            GamePart("soap", "[Ambil sabun]", false),
            GamePart("wrong_arrow", "^ Panah ke atas", true),
            GamePart("long_text", "[Basahi tangan dengan air bersih yang mengalir dari keran kamar mandi]", true),
            GamePart("arrow_down_2", "|", false),
            GamePart("rub", "[Gosok tangan]", false),
            GamePart("arrow_down_3", "|", false),
            GamePart("rinse", "[Bilas tangan]", false),
            GamePart("missing_finish", "SELESAI belum ada", true)
        ).forEach { part ->
            gameChoiceContainer.addView(createFlowPart(part), LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                if (part.key.startsWith("arrow")) dp(32) else LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(6)
            })
        }

        tidyFlowchartText = TextView(this).apply {
            visibility = View.GONE
            text = "Versi rapi:\n[MULAI]\n|\n[Basahi tangan]\n|\n[Ambil sabun]\n|\n[Gosok tangan]\n|\n[Bilas tangan]\n|\n[SELESAI]"
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setTextColor(ContextCompat.getColor(this@LessonEightActivity, R.color.algoplay_green_dark))
            textSize = 13f
            typeface = Typeface.MONOSPACE
            setLineSpacing(dp(2).toFloat(), 1f)
            background = roundedStrokeDrawable(
                ContextCompat.getColor(this@LessonEightActivity, R.color.algoplay_blue_soft),
                ContextCompat.getColor(this@LessonEightActivity, R.color.algoplay_green_dark),
                dp(18)
            )
        }
        gameSlotContainer.addView(tidyFlowchartText, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(10)
        })
    }

    override fun checkGameAnswer() {
        if (foundParts.containsAll(wrongParts)) {
            showSuccess()
        } else {
            showFeedbackDialog(
                "Belum lengkap",
                "Masih ada bagian yang perlu diperbaiki. Cari panah salah, tulisan terlalu panjang, dan bagian selesai.",
                R.drawable.hai_materi,
                false
            )
        }
    }

    private fun createFlowPart(part: GamePart): TextView {
        return TextView(this).apply {
            text = part.label
            gravity = Gravity.CENTER
            includeFontPadding = false
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setTextColor(ContextCompat.getColor(this@LessonEightActivity, R.color.algoplay_text))
            textSize = if (part.key.startsWith("arrow")) 18f else 13f
            setTypeface(null, Typeface.BOLD)
            background = roundedStrokeDrawable(
                ContextCompat.getColor(this@LessonEightActivity, R.color.white),
                ContextCompat.getColor(this@LessonEightActivity, R.color.algoplay_blue_soft),
                dp(16)
            )
            setOnClickListener {
                if (part.isWrong) {
                    foundParts.add(part.key)
                    isEnabled = false
                    background = roundedStrokeDrawable(
                        ContextCompat.getColor(this@LessonEightActivity, R.color.algoplay_blue_soft),
                        ContextCompat.getColor(this@LessonEightActivity, R.color.algoplay_green_dark),
                        dp(16)
                    )
                    setTextColor(ContextCompat.getColor(this@LessonEightActivity, R.color.algoplay_green_dark))
                    hintText?.text = "Bagus! Ketemu ${foundParts.size}/3 bagian yang perlu dirapikan."
                    if (foundParts.containsAll(wrongParts)) {
                        tidyFlowchartText?.visibility = View.VISIBLE
                        showSuccess()
                    }
                } else {
                    shakePart(this)
                    showFeedbackDialog(
                        "Ups!",
                        "Bagian itu sudah oke. Coba lihat lagi: flowchart rapi harus punya panah jelas, tulisan singkat, dan bagian selesai.",
                        R.drawable.hai_materi,
                        false
                    )
                }
            }
        }
    }

    private fun shakePart(view: View) {
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
        tidyFlowchartText?.visibility = View.VISIBLE
        showFeedbackDialog(
            "Yeay!",
            "Flowchart sudah rapi. Robot Algo sekarang bisa mengikuti langkahnya dengan mudah!",
            R.drawable.sorakan_leaderboard,
            true
        )
    }

    private data class GamePart(
        val key: String,
        val label: String,
        val isWrong: Boolean
    )
}
