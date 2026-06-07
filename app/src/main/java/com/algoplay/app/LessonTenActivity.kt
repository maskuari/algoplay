package com.algoplay.app

import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

class LessonTenActivity : LessonOneActivity() {

    override val layoutResId: Int = R.layout.activity_lesson_ten
    override val lessonNumber: Int = 10
    override val nextLessonNumber: Int = 11

    override val quizOneOptions: List<String> = listOf(
        "A. Melakukan sesuatu berkali-kali",
        "B. Tidur sepanjang hari",
        "C. Menghapus semua langkah",
        "D. Menggambar tanpa urutan"
    )
    override val quizOneCorrectIndex: Int = 0
    override val quizOneCorrectMessage: String =
        "Benar! Pengulangan berarti melakukan sesuatu berkali-kali."
    override val quizOneWrongMessage: String =
        "Belum tepat. Pengulangan adalah melakukan langkah yang sama lebih dari satu kali."

    override val quizTwoOptions: List<String> = listOf(
        "A. Tepuk tangan 1 kali",
        "B. Tepuk tangan 3 kali",
        "C. Tepuk tangan 10 kali",
        "D. Tidak tepuk tangan"
    )
    override val quizTwoCorrectIndex: Int = 1
    override val quizTwoCorrectMessage: String =
        "Pintar! Kalau diminta 3 kali, berarti dilakukan sampai hitungan 3."
    override val quizTwoWrongMessage: String =
        "Coba hitung lagi. Kalau diminta 3 kali, berarti harus dilakukan sebanyak 3 kali."

    private val targetJumpCount = 5
    private var jumpCount = 0
    private var jumpedTooMuch = false
    private var counterText: TextView? = null
    private var jumpButton: TextView? = null
    private var robotImage: ImageView? = null

    override fun buildGame() {
        gameChoiceContainer.removeAllViews()
        gameSlotContainer.removeAllViews()
        jumpCount = 0
        jumpedTooMuch = false

        btnResetGame.text = "Ulangi"
        btnCheckGame.text = "Cek Lompatan"

        val prompt = TextView(this).apply {
            text = "Bantu Algo melompat 5 kali!"
            gravity = Gravity.CENTER
            includeFontPadding = false
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setTextColor(ContextCompat.getColor(this@LessonTenActivity, R.color.algoplay_text))
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            background = roundedStrokeDrawable(
                ContextCompat.getColor(this@LessonTenActivity, R.color.algoplay_blue_soft),
                ContextCompat.getColor(this@LessonTenActivity, R.color.algoplay_blue_dark),
                dp(16)
            )
        }
        gameSlotContainer.addView(prompt, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        robotImage = ImageView(this).apply {
            setImageResource(R.drawable.hai_materi)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = "Robot Algo sedang olahraga"
        }
        gameSlotContainer.addView(robotImage, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(132)
        ).apply {
            topMargin = dp(12)
        })

        counterText = TextView(this).apply {
            text = jumpLabel()
            gravity = Gravity.CENTER
            includeFontPadding = false
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setTextColor(ContextCompat.getColor(this@LessonTenActivity, R.color.algoplay_blue_dark))
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            background = roundedStrokeDrawable(
                ContextCompat.getColor(this@LessonTenActivity, R.color.white),
                ContextCompat.getColor(this@LessonTenActivity, R.color.algoplay_blue_soft),
                dp(18)
            )
        }
        gameSlotContainer.addView(counterText, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(10)
        })

        jumpButton = TextView(this).apply {
            text = "LOMPAT"
            gravity = Gravity.CENTER
            includeFontPadding = false
            setTextColor(ContextCompat.getColor(this@LessonTenActivity, R.color.white))
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            background = roundedDrawable(
                ContextCompat.getColor(this@LessonTenActivity, R.color.algoplay_green_dark),
                dp(18)
            )
            setOnClickListener { jumpOnce() }
        }
        gameChoiceContainer.addView(jumpButton, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(58)
        ).apply {
            topMargin = dp(12)
        })

        val note = TextView(this).apply {
            text = "Jangan kurang, jangan lebih. Berhenti tepat di angka 5 ya."
            gravity = Gravity.CENTER
            includeFontPadding = false
            setTextColor(ContextCompat.getColor(this@LessonTenActivity, R.color.algoplay_subtext))
            textSize = 12f
        }
        gameChoiceContainer.addView(note, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(9)
        })
    }

    override fun checkGameAnswer() {
        when {
            jumpedTooMuch || jumpCount > targetJumpCount -> showFeedbackDialog(
                "Ups!",
                "Robot Algo kebanyakan melompat. Coba ulangi dan berhenti tepat di angka 5 ya!",
                R.drawable.hai_materi,
                false
            )
            jumpCount == targetJumpCount -> showSuccess()
            else -> showFeedbackDialog(
                "Ayo lanjut!",
                "Robot Algo belum melompat 5 kali. Ketuk lagi sampai cukup ya!",
                R.drawable.hai_materi,
                false
            )
        }
    }

    private fun jumpOnce() {
        jumpCount++
        if (jumpCount > targetJumpCount) {
            jumpedTooMuch = true
            jumpButton?.text = "TERLALU BANYAK"
            updateCounter()
            animateRobot()
            showFeedbackDialog(
                "Ups!",
                "Robot Algo kebanyakan melompat. Coba ulangi dan berhenti tepat di angka 5 ya!",
                R.drawable.hai_materi,
                false
            )
            return
        }

        jumpButton?.text = if (jumpCount == targetJumpCount) "SELESAI" else "LOMPAT"
        updateCounter()
        animateRobot()
        if (jumpCount == targetJumpCount) showSuccess()
    }

    private fun updateCounter() {
        counterText?.text = jumpLabel()
        counterText?.background = roundedStrokeDrawable(
            ContextCompat.getColor(this, if (jumpedTooMuch) R.color.algoplay_bg else R.color.white),
            ContextCompat.getColor(
                this,
                when {
                    jumpedTooMuch -> R.color.algoplay_red_dark
                    jumpCount == targetJumpCount -> R.color.algoplay_green_dark
                    else -> R.color.algoplay_blue_soft
                }
            ),
            dp(18)
        )
    }

    private fun jumpLabel(): String = "Lompatan: $jumpCount / $targetJumpCount"

    private fun animateRobot() {
        robotImage?.animate()
            ?.translationY((-dp(22)).toFloat())
            ?.setDuration(100)
            ?.withEndAction {
                robotImage?.animate()
                    ?.translationY(0f)
                    ?.setDuration(130)
                    ?.start()
            }
            ?.start()
    }

    private fun showSuccess() {
        showFeedbackDialog(
            "Yeay!",
            "Robot Algo berhasil melompat 5 kali. Pengulanganmu tepat!",
            R.drawable.sorakan_leaderboard,
            true
        )
    }
}
