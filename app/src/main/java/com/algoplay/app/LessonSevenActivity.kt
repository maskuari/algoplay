package com.algoplay.app

import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

class LessonSevenActivity : LessonOneActivity() {

    override val layoutResId: Int = R.layout.activity_lesson_seven
    override val lessonNumber: Int = 7
    override val nextLessonNumber: Int = 8

    override val quizOneOptions: List<String> = listOf(
        "A. Mulai dan selesai",
        "B. Memilih ya atau tidak",
        "C. Menunjukkan arah",
        "D. Menghapus langkah"
    )
    override val quizOneCorrectIndex: Int = 0
    override val quizOneCorrectMessage: String =
        "Benar! Simbol oval digunakan untuk MULAI dan SELESAI."
    override val quizOneWrongMessage: String =
        "Belum tepat. Simbol oval biasanya dipakai untuk awal dan akhir flowchart."

    override val quizTwoOptions: List<String> = listOf(
        "A. Langkah biasa",
        "B. Pilihan atau keputusan",
        "C. Menampilkan warna",
        "D. Menggambar robot"
    )
    override val quizTwoCorrectIndex: Int = 1
    override val quizTwoCorrectMessage: String =
        "Pintar! Belah ketupat digunakan untuk pilihan atau keputusan."
    override val quizTwoWrongMessage: String =
        "Coba ingat lagi. Simbol belah ketupat dipakai saat ada pilihan, seperti YA atau TIDAK."

    private val symbols = listOf(
        MatchCard("oval", "Oval"),
        MatchCard("process", "Persegi panjang"),
        MatchCard("io", "Jajar genjang"),
        MatchCard("decision", "Belah ketupat"),
        MatchCard("arrow", "Panah")
    )

    private val meanings = listOf(
        MatchCard("decision", "Pilihan ya atau tidak"),
        MatchCard("arrow", "Arah langkah berikutnya"),
        MatchCard("oval", "Mulai atau selesai"),
        MatchCard("io", "Input atau output"),
        MatchCard("process", "Proses atau langkah")
    )

    private val matchedKeys = mutableSetOf<String>()
    private var selectedSymbol: MatchCard? = null
    private var hintText: TextView? = null

    override fun buildGame() {
        gameChoiceContainer.removeAllViews()
        gameSlotContainer.removeAllViews()
        matchedKeys.clear()
        selectedSymbol = null

        btnResetGame.text = "Ulangi"
        btnCheckGame.text = "Cek Pasangan"

        hintText = TextView(this).apply {
            text = "Pilih satu simbol di kiri, lalu pilih artinya di kanan."
            gravity = Gravity.CENTER
            includeFontPadding = false
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setTextColor(ContextCompat.getColor(this@LessonSevenActivity, R.color.algoplay_text))
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            background = roundedStrokeDrawable(
                ContextCompat.getColor(this@LessonSevenActivity, R.color.algoplay_blue_soft),
                ContextCompat.getColor(this@LessonSevenActivity, R.color.algoplay_blue_dark),
                dp(16)
            )
        }
        gameSlotContainer.addView(hintText, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        val board = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        gameChoiceContainer.addView(board, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(8)
        })

        val symbolColumn = createColumn("Simbol")
        val meaningColumn = createColumn("Arti")
        board.addView(symbolColumn, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            rightMargin = dp(6)
        })
        board.addView(meaningColumn, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
            leftMargin = dp(6)
        })

        symbols.forEach { card ->
            symbolColumn.addView(createMatchCard(card, isSymbol = true), LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(54)
            ).apply {
                topMargin = dp(8)
            })
        }

        meanings.forEach { card ->
            meaningColumn.addView(createMatchCard(card, isSymbol = false), LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(54)
            ).apply {
                topMargin = dp(8)
            })
        }
    }

    override fun checkGameAnswer() {
        if (matchedKeys.size == symbols.size) {
            showSuccess()
        } else {
            showFeedbackDialog(
                "Belum selesai",
                "Cocokkan semua simbol dengan artinya dulu, ya.",
                R.drawable.hai_materi,
                false
            )
        }
    }

    private fun createColumn(title: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(this@LessonSevenActivity).apply {
                text = title
                gravity = Gravity.CENTER
                includeFontPadding = false
                setTextColor(ContextCompat.getColor(this@LessonSevenActivity, R.color.algoplay_blue_dark))
                textSize = 14f
                setTypeface(null, Typeface.BOLD)
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ))
        }
    }

    private fun createMatchCard(card: MatchCard, isSymbol: Boolean): TextView {
        return TextView(this).apply {
            text = card.label
            card.view = this
            gravity = Gravity.CENTER
            includeFontPadding = false
            setPadding(dp(8), 0, dp(8), 0)
            setTextColor(ContextCompat.getColor(this@LessonSevenActivity, R.color.algoplay_text))
            textSize = 12f
            setTypeface(null, Typeface.BOLD)
            background = normalBackground()
            setOnClickListener {
                if (matchedKeys.contains(card.key)) return@setOnClickListener
                if (isSymbol) {
                    selectSymbol(card)
                } else {
                    selectMeaning(card)
                }
            }
        }
    }

    private fun selectSymbol(card: MatchCard) {
        selectedSymbol?.view?.background = normalBackground()
        selectedSymbol = card
        card.view?.background = roundedStrokeDrawable(
            ContextCompat.getColor(this, R.color.algoplay_blue_soft),
            ContextCompat.getColor(this, R.color.algoplay_blue_dark),
            dp(16)
        )
        hintText?.text = "Sekarang pilih arti untuk: ${card.label}"
    }

    private fun selectMeaning(meaning: MatchCard) {
        val symbol = selectedSymbol
        if (symbol == null) {
            hintText?.text = "Pilih simbolnya dulu di sebelah kiri."
            shakeCard(meaning.view)
            return
        }

        if (symbol.key == meaning.key) {
            matchedKeys.add(symbol.key)
            lockCard(symbol.view)
            lockCard(meaning.view)
            selectedSymbol = null
            hintText?.text = if (matchedKeys.size == symbols.size) {
                "Semua simbol sudah cocok!"
            } else {
                "Bagus! Pilih simbol berikutnya."
            }
            if (matchedKeys.size == symbols.size) {
                showSuccess()
            }
        } else {
            shakeCard(symbol.view)
            shakeCard(meaning.view)
            showFeedbackDialog(
                "Ups!",
                "Simbol itu belum cocok. Coba lihat bentuknya lagi ya!",
                R.drawable.hai_materi,
                false
            )
        }
    }

    private fun lockCard(view: TextView?) {
        view?.apply {
            isEnabled = false
            setTextColor(ContextCompat.getColor(this@LessonSevenActivity, R.color.algoplay_green_dark))
            background = roundedStrokeDrawable(
                ContextCompat.getColor(this@LessonSevenActivity, R.color.algoplay_blue_soft),
                ContextCompat.getColor(this@LessonSevenActivity, R.color.algoplay_green_dark),
                dp(16)
            )
        }
    }

    private fun shakeCard(view: View?) {
        view?.animate()
            ?.translationX(dp(8).toFloat())
            ?.setDuration(60)
            ?.withEndAction {
                view.animate()
                    .translationX((-dp(8)).toFloat())
                    .setDuration(60)
                    .withEndAction {
                        view.animate().translationX(0f).setDuration(60).start()
                    }
                    .start()
            }
            ?.start()
    }

    private fun normalBackground() = roundedStrokeDrawable(
        ContextCompat.getColor(this, R.color.white),
        ContextCompat.getColor(this, R.color.algoplay_blue_soft),
        dp(16)
    )

    private fun showSuccess() {
        showFeedbackDialog(
            "Yeay!",
            "Kamu berhasil mencocokkan simbol flowchart dengan benar!",
            R.drawable.sorakan_leaderboard,
            true
        )
    }

    private data class MatchCard(
        val key: String,
        val label: String,
        var view: TextView? = null
    )
}
