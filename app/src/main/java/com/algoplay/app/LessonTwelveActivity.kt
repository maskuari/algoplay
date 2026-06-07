package com.algoplay.app

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

class LessonTwelveActivity : LessonOneActivity() {

    override val layoutResId: Int = R.layout.activity_lesson_twelve
    override val lessonNumber: Int = 12
    override val nextLessonNumber: Int = 12
    override val quizOneOptions: List<String> = emptyList()
    override val quizTwoOptions: List<String> = emptyList()

    private lateinit var examNumberRow: LinearLayout
    private lateinit var examResultPanel: LinearLayout
    private lateinit var txtExamProgress: TextView
    private lateinit var txtExamQuestion: TextView
    private lateinit var txtExamType: TextView
    private lateinit var txtExamReview: TextView
    private lateinit var txtExamResultTitle: TextView
    private lateinit var txtExamResultDetail: TextView
    private lateinit var imgExamResult: ImageView
    private lateinit var examContentContainer: LinearLayout
    private lateinit var answerContainer: LinearLayout
    private lateinit var btnExamPrev: TextView
    private lateinit var btnExamSkip: TextView
    private lateinit var btnExamNext: TextView
    private lateinit var btnRemedial: TextView
    private lateinit var btnFinishExam: TextView

    private var currentIndex = 0
    private var hasResult = false
    private var passedExam = false
    private var finalScore = 0
    private var correctCount = 0
    private val answers = MutableList<UserAnswer?>(questions.size) { null }
    private val numberViews = mutableListOf<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindExamViews()
        buildNumberRow()
        renderQuestion()
        updateNavigation()
    }

    override fun buildGame() {
        gameChoiceContainer.removeAllViews()
        gameSlotContainer.removeAllViews()
        btnResetGame.visibility = View.GONE
        btnCheckGame.visibility = View.GONE
    }

    override fun setupActions() {
        btnFinishExam = findViewById(R.id.btnFinishLesson)
        btnFinishExam.visibility = View.GONE
        btnFinishExam.setOnClickListener { finishWithResult() }
    }

    private fun bindExamViews() {
        examNumberRow = findViewById(R.id.examNumberRow)
        examResultPanel = findViewById(R.id.examResultPanel)
        txtExamProgress = findViewById(R.id.txtExamProgress)
        txtExamQuestion = findViewById(R.id.txtExamQuestion)
        txtExamType = findViewById(R.id.txtExamType)
        txtExamReview = findViewById(R.id.txtExamReview)
        txtExamResultTitle = findViewById(R.id.txtExamResultTitle)
        txtExamResultDetail = findViewById(R.id.txtExamResultDetail)
        imgExamResult = findViewById(R.id.imgExamResult)
        examContentContainer = findViewById(R.id.examContentContainer)
        answerContainer = findViewById(R.id.answerContainer)
        btnExamPrev = findViewById(R.id.btnExamPrev)
        btnExamSkip = findViewById(R.id.btnExamSkip)
        btnExamNext = findViewById(R.id.btnExamNext)
        btnRemedial = findViewById(R.id.btnRemedial)

        btnExamPrev.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                renderQuestion()
            }
        }
        btnExamSkip.setOnClickListener {
            currentIndex = nextIndex()
            renderQuestion()
        }
        btnExamNext.setOnClickListener {
            if (hasResult) {
                currentIndex = nextIndex()
                renderQuestion()
                return@setOnClickListener
            }
            if (allAnswered()) {
                showResult()
            } else {
                currentIndex = nextIndex()
                renderQuestion()
            }
        }
        btnRemedial.setOnClickListener { startRemedial() }
    }

    private fun buildNumberRow() {
        examNumberRow.removeAllViews()
        numberViews.clear()
        questions.forEachIndexed { index, _ ->
            val number = TextView(this).apply {
                text = (index + 1).toString()
                gravity = Gravity.CENTER
                includeFontPadding = false
                textSize = 13f
                setTypeface(null, Typeface.BOLD)
                setOnClickListener {
                    currentIndex = index
                    renderQuestion()
                }
            }
            numberViews.add(number)
            examNumberRow.addView(number, LinearLayout.LayoutParams(dp(42), dp(42)).apply {
                marginEnd = dp(8)
            })
        }
        updateNumberRow()
    }

    private fun renderQuestion() {
        val question = questions[currentIndex]
        txtExamProgress.text = "Soal ${currentIndex + 1} dari ${questions.size}"
        txtExamType.text = question.typeLabel
        txtExamQuestion.text = question.prompt
        examContentContainer.removeAllViews()
        answerContainer.removeAllViews()

        when (question) {
            is ExamQuestion.Choice -> renderChoice(question)
            is ExamQuestion.Sequence -> renderSequence(question)
            is ExamQuestion.Matching -> renderMatching(question)
            is ExamQuestion.TapCount -> renderTapCount(question)
        }
        renderReview()
        updateNumberRow()
        updateNavigation()
    }

    private fun renderChoice(question: ExamQuestion.Choice) {
        question.options.forEachIndexed { index, option ->
            val selected = (answers[currentIndex] as? UserAnswer.Choice)?.index == index
            answerContainer.addView(answerButton(option, selected).apply {
                if (!hasResult) {
                    setOnClickListener {
                        answers[currentIndex] = UserAnswer.Choice(index)
                        renderQuestion()
                    }
                }
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(8)
            })
        }
    }

    private fun renderSequence(question: ExamQuestion.Sequence) {
        val selected = (answers[currentIndex] as? UserAnswer.Sequence)?.values.orEmpty()
        val sequenceLabel = if (selected.isEmpty()) "belum mulai" else selected.joinToString(" -> ")
        examContentContainer.addView(infoCard("Ketuk balok sesuai urutan. Urutanmu: $sequenceLabel"))

        question.items.forEach { item ->
            val alreadyPicked = selected.contains(item)
            answerContainer.addView(answerButton(item, alreadyPicked).apply {
                isEnabled = !hasResult && !alreadyPicked
                alpha = if (alreadyPicked) 0.62f else 1f
                setOnClickListener {
                    val nextValues = selected + item
                    answers[currentIndex] = UserAnswer.Sequence(nextValues)
                    renderQuestion()
                }
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(46)
            ).apply {
                topMargin = dp(8)
            })
        }

        if (!hasResult && selected.isNotEmpty()) {
            answerContainer.addView(smallAction("Ulangi urutan").apply {
                setOnClickListener {
                    answers[currentIndex] = null
                    renderQuestion()
                }
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(42)
            ).apply {
                topMargin = dp(10)
            })
        }
    }

    private fun renderMatching(question: ExamQuestion.Matching) {
        val selectedPairs = (answers[currentIndex] as? UserAnswer.Matching)?.pairs.orEmpty()
        examContentContainer.addView(infoCard("Pilih arti yang cocok untuk setiap simbol flowchart."))

        question.symbols.forEach { symbol ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = roundedStrokeDrawable(
                    ContextCompat.getColor(this@LessonTwelveActivity, R.color.white),
                    ContextCompat.getColor(this@LessonTwelveActivity, R.color.algoplay_blue_soft),
                    dp(18)
                )
                setPadding(dp(12), dp(10), dp(12), dp(12))
            }
            row.addView(compactText(symbol, 14, R.color.algoplay_text, true, Gravity.START))
            val choices = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
            }
            question.meanings.forEach { meaning ->
                val selected = selectedPairs[symbol] == meaning
                choices.addView(answerButton(meaning, selected).apply {
                    textSize = 12f
                    if (!hasResult) {
                        setOnClickListener {
                            val nextPairs = selectedPairs.toMutableMap()
                            nextPairs[symbol] = meaning
                            answers[currentIndex] = UserAnswer.Matching(nextPairs)
                            renderQuestion()
                        }
                    }
                }, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(6)
                })
            }
            row.addView(choices, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(6)
            })
            answerContainer.addView(row, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(9)
            })
        }
    }

    private fun renderTapCount(question: ExamQuestion.TapCount) {
        val count = (answers[currentIndex] as? UserAnswer.Count)?.count ?: 0
        val robot = ImageView(this).apply {
            setImageResource(R.drawable.hai_materi)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = "Robot Algo melompat"
        }
        examContentContainer.addView(robot, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(128)
        ))

        examContentContainer.addView(infoCard("Lompatan: $count / ${question.targetCount}"))

        answerContainer.addView(answerButton("LOMPAT", count > 0).apply {
            textSize = 16f
            setTextColor(ContextCompat.getColor(this@LessonTwelveActivity, R.color.white))
            background = roundedDrawable(
                ContextCompat.getColor(this@LessonTwelveActivity, R.color.algoplay_green_dark),
                dp(18)
            )
            if (!hasResult) {
                setOnClickListener {
                    val nextCount = count + 1
                    answers[currentIndex] = UserAnswer.Count(nextCount)
                    robot.animate()
                        .translationY((-dp(22)).toFloat())
                        .setDuration(90)
                        .withEndAction { robot.animate().translationY(0f).setDuration(120).start() }
                        .start()
                    renderQuestion()
                }
            }
        }, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(58)
        ).apply {
            topMargin = dp(10)
        })

        if (!hasResult && count > 0) {
            val hint = when {
                count < question.targetCount -> "Algo belum selesai melompat."
                count == question.targetCount -> "Pas! Kamu bisa lanjut."
                else -> "Ups! Algo terlalu banyak melompat."
            }
            answerContainer.addView(infoCard(hint), LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(10)
            })
            answerContainer.addView(smallAction("Ulangi lompatan").apply {
                setOnClickListener {
                    answers[currentIndex] = null
                    renderQuestion()
                }
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(42)
            ).apply {
                topMargin = dp(10)
            })
        }
    }

    private fun renderReview() {
        if (!hasResult) {
            txtExamReview.visibility = View.GONE
            return
        }

        val correct = isCorrect(currentIndex)
        val question = questions[currentIndex]
        txtExamReview.visibility = View.VISIBLE
        txtExamReview.text = if (correct) {
            "Jawaban kamu benar!"
        } else {
            "Jawaban kamu: ${answerSummary(question, answers[currentIndex])}\nJawaban benar: ${correctSummary(question)}"
        }
        txtExamReview.setTextColor(
            ContextCompat.getColor(this, if (correct) R.color.algoplay_green_dark else R.color.algoplay_red_dark)
        )
        txtExamReview.background = roundedStrokeDrawable(
            ContextCompat.getColor(this, R.color.white),
            ContextCompat.getColor(this, if (correct) R.color.algoplay_green_dark else R.color.algoplay_red_dark),
            dp(18)
        )
    }

    private fun updateNumberRow() {
        numberViews.forEachIndexed { index, view ->
            val fillColor = when {
                hasResult && isCorrect(index) -> R.color.algoplay_green_dark
                hasResult -> R.color.algoplay_red_dark
                isAnswered(index) -> R.color.algoplay_blue_dark
                index == currentIndex -> R.color.algoplay_blue_soft
                else -> R.color.white
            }
            val textColor = when {
                hasResult || isAnswered(index) -> R.color.white
                else -> R.color.algoplay_text
            }
            view.setTextColor(ContextCompat.getColor(this, textColor))
            view.background = roundedStrokeDrawable(
                ContextCompat.getColor(this, fillColor),
                ContextCompat.getColor(this, if (index == currentIndex) R.color.algoplay_blue_dark else R.color.algoplay_blue_soft),
                dp(14)
            )
        }
    }

    private fun updateNavigation() {
        btnExamPrev.alpha = if (currentIndex == 0) 0.45f else 1f
        btnExamPrev.isEnabled = currentIndex > 0
        btnExamSkip.visibility = if (hasResult || allAnswered()) View.GONE else View.VISIBLE
        btnExamNext.text = when {
            hasResult -> if (currentIndex == questions.lastIndex) "Ke Awal" else "Berikutnya"
            allAnswered() -> "Lihat Hasil"
            currentIndex == questions.lastIndex -> "Ke Awal"
            else -> "Berikutnya"
        }
    }

    private fun showResult() {
        hasResult = true
        correctCount = questions.indices.count { isCorrect(it) }
        finalScore = ((correctCount / questions.size.toFloat()) * 100).roundToInt()
        passedExam = finalScore >= PASSING_SCORE

        examResultPanel.visibility = View.VISIBLE
        imgExamResult.setImageResource(if (passedExam) R.drawable.sorakan_leaderboard else R.drawable.hai_materi)
        txtExamResultTitle.text = if (passedExam) {
            "Yeay! Kamu lulus Ujian Seru Algoritma!"
        } else {
            "Remedial"
        }
        txtExamResultDetail.text = if (passedExam) {
            "Jawaban benar: $correctCount / ${questions.size}\nNilai: $finalScore\nTotal score bertambah: $finalScore"
        } else {
            "Tidak apa-apa! Robot Algo akan membantumu belajar lagi.\n\nJawaban benar: $correctCount / ${questions.size}\nNilai: $finalScore\nStatus: Remedial\n\nAyo coba lagi. Kamu pasti bisa lebih baik!"
        }
        btnRemedial.visibility = if (passedExam) View.GONE else View.VISIBLE
        btnFinishExam.visibility = if (passedExam) View.VISIBLE else View.GONE
        btnFinishExam.text = "Simpan Nilai"

        if (passedExam) {
            setResult(Activity.RESULT_OK, resultIntent())
        }

        showFeedbackDialog(
            if (passedExam) "Lulus!" else "Remedial",
            if (passedExam) {
                "Jawaban benar: $correctCount / ${questions.size}. Nilai kamu $finalScore."
            } else {
                "Nilai kamu $finalScore. Yuk mulai remedial supaya makin paham."
            },
            if (passedExam) R.drawable.sorakan_leaderboard else R.drawable.hai_materi,
            passedExam
        )
        renderQuestion()
    }

    private fun startRemedial() {
        answers.indices.forEach { answers[it] = null }
        currentIndex = 0
        hasResult = false
        passedExam = false
        finalScore = 0
        correctCount = 0
        examResultPanel.visibility = View.GONE
        btnFinishExam.visibility = View.GONE
        setResult(Activity.RESULT_CANCELED)
        renderQuestion()
    }

    private fun finishWithResult() {
        if (passedExam) {
            setResult(Activity.RESULT_OK, resultIntent())
        }
        finish()
    }

    private fun resultIntent(): Intent {
        return Intent()
            .putExtra(EXTRA_COMPLETED_LESSON, lessonNumber)
            .putExtra(EXTRA_EXAM_SCORE, finalScore)
            .putExtra(EXTRA_EXAM_CORRECT, correctCount)
    }

    private fun answerButton(value: String, selected: Boolean): TextView {
        return TextView(this).apply {
            text = value
            gravity = Gravity.CENTER_VERTICAL
            minHeight = dp(46)
            includeFontPadding = false
            setPadding(dp(14), dp(8), dp(14), dp(8))
            setTextColor(ContextCompat.getColor(this@LessonTwelveActivity, R.color.algoplay_text))
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            background = roundedStrokeDrawable(
                ContextCompat.getColor(
                    this@LessonTwelveActivity,
                    if (selected) R.color.algoplay_blue_soft else R.color.white
                ),
                ContextCompat.getColor(
                    this@LessonTwelveActivity,
                    if (selected) R.color.algoplay_blue_dark else R.color.algoplay_blue_soft
                ),
                dp(16)
            )
        }
    }

    private fun smallAction(value: String): TextView {
        return TextView(this).apply {
            text = value
            gravity = Gravity.CENTER
            includeFontPadding = false
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@LessonTwelveActivity, R.color.algoplay_blue_dark))
            background = roundedStrokeDrawable(
                ContextCompat.getColor(this@LessonTwelveActivity, R.color.white),
                ContextCompat.getColor(this@LessonTwelveActivity, R.color.algoplay_blue_dark),
                dp(16)
            )
        }
    }

    private fun infoCard(value: String): TextView {
        return TextView(this).apply {
            text = value
            gravity = Gravity.CENTER
            includeFontPadding = false
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setTextColor(ContextCompat.getColor(this@LessonTwelveActivity, R.color.algoplay_subtext))
            textSize = 12f
            background = roundedDrawable(
                ContextCompat.getColor(this@LessonTwelveActivity, R.color.algoplay_bg),
                dp(16)
            )
        }
    }

    private fun nextIndex(): Int {
        return if (currentIndex == questions.lastIndex) 0 else currentIndex + 1
    }

    private fun allAnswered(): Boolean = questions.indices.all { isAnswered(it) }

    private fun isAnswered(index: Int): Boolean {
        val question = questions[index]
        val answer = answers[index]
        return when {
            answer == null -> false
            question is ExamQuestion.Sequence && answer is UserAnswer.Sequence -> answer.values.size == question.correctOrder.size
            question is ExamQuestion.Matching && answer is UserAnswer.Matching -> answer.pairs.size == question.correctPairs.size
            question is ExamQuestion.TapCount && answer is UserAnswer.Count -> answer.count > 0
            answer is UserAnswer.Choice -> true
            else -> false
        }
    }

    private fun isCorrect(index: Int): Boolean {
        val question = questions[index]
        val answer = answers[index] ?: return false
        return when {
            question is ExamQuestion.Choice && answer is UserAnswer.Choice -> answer.index == question.correctIndex
            question is ExamQuestion.Sequence && answer is UserAnswer.Sequence -> answer.values == question.correctOrder
            question is ExamQuestion.Matching && answer is UserAnswer.Matching -> answer.pairs == question.correctPairs
            question is ExamQuestion.TapCount && answer is UserAnswer.Count -> answer.count == question.targetCount
            else -> false
        }
    }

    private fun answerSummary(question: ExamQuestion, answer: UserAnswer?): String {
        return when {
            question is ExamQuestion.Choice && answer is UserAnswer.Choice -> question.options.getOrNull(answer.index).orEmpty()
            question is ExamQuestion.Sequence && answer is UserAnswer.Sequence -> answer.values.joinToString(" -> ")
            question is ExamQuestion.Matching && answer is UserAnswer.Matching -> question.symbols.joinToString("; ") { symbol ->
                "$symbol = ${answer.pairs[symbol] ?: "--"}"
            }
            question is ExamQuestion.TapCount && answer is UserAnswer.Count -> "Lompatan: ${answer.count}"
            else -> "--"
        }
    }

    private fun correctSummary(question: ExamQuestion): String {
        return when (question) {
            is ExamQuestion.Choice -> question.options[question.correctIndex]
            is ExamQuestion.Sequence -> question.correctOrder.joinToString(" -> ")
            is ExamQuestion.Matching -> question.symbols.joinToString("; ") { symbol ->
                "$symbol = ${question.correctPairs[symbol]}"
            }
            is ExamQuestion.TapCount -> "Lompatan: ${question.targetCount}"
        }
    }

    private sealed class ExamQuestion(
        val typeLabel: String,
        val prompt: String
    ) {
        class Choice(
            typeLabel: String,
            prompt: String,
            val options: List<String>,
            val correctIndex: Int
        ) : ExamQuestion(typeLabel, prompt)

        class Sequence(
            typeLabel: String,
            prompt: String,
            val items: List<String>,
            val correctOrder: List<String>
        ) : ExamQuestion(typeLabel, prompt)

        class Matching(
            typeLabel: String,
            prompt: String,
            val symbols: List<String>,
            val meanings: List<String>,
            val correctPairs: Map<String, String>
        ) : ExamQuestion(typeLabel, prompt)

        class TapCount(
            typeLabel: String,
            prompt: String,
            val targetCount: Int
        ) : ExamQuestion(typeLabel, prompt)
    }

    private sealed class UserAnswer {
        data class Choice(val index: Int) : UserAnswer()
        data class Sequence(val values: List<String>) : UserAnswer()
        data class Matching(val pairs: Map<String, String>) : UserAnswer()
        data class Count(val count: Int) : UserAnswer()
    }

    companion object {
        const val EXTRA_EXAM_SCORE = "extra_exam_score"
        const val EXTRA_EXAM_CORRECT = "extra_exam_correct"
        private const val PASSING_SCORE = 60

        private val questions = listOf(
            ExamQuestion.Choice(
                "Pilihan Ganda",
                "Algoritma adalah...",
                listOf(
                    "A. Urutan langkah-langkah untuk menyelesaikan sesuatu",
                    "B. Gambar lucu di komputer",
                    "C. Warna pada layar",
                    "D. Nama makanan robot"
                ),
                0
            ),
            ExamQuestion.Choice(
                "Tap Jawaban Benar",
                "Kegiatan mana yang memiliki langkah-langkah berurutan?",
                listOf("Menyikat gigi", "Tidur tanpa bangun", "Melihat awan saja", "Diam di kursi"),
                0
            ),
            ExamQuestion.Sequence(
                "Susun Urutan",
                "Klik gambar sesuai urutan membuat susu.",
                listOf("Aduk susu", "Siapkan gelas", "Masukkan susu", "Tuang air", "Susu siap diminum"),
                listOf("Siapkan gelas", "Masukkan susu", "Tuang air", "Aduk susu", "Susu siap diminum")
            ),
            ExamQuestion.Choice(
                "Pilihan Ganda",
                "Mengapa langkah dalam algoritma harus berurutan?",
                listOf("A. Supaya hasilnya benar", "B. Supaya membingungkan", "C. Supaya lebih lama", "D. Supaya robot tidur"),
                0
            ),
            ExamQuestion.Choice(
                "Lengkapi Kata",
                "___ sikat gigi, lalu beri pasta gigi.",
                listOf("Ambil", "Tidur", "Lempar", "Sembunyikan"),
                0
            ),
            ExamQuestion.Choice(
                "Benar atau Salah",
                "Pseudocode adalah langkah-langkah seperti kode, tetapi masih mudah dibaca manusia.",
                listOf("Benar", "Salah"),
                0
            ),
            ExamQuestion.Choice(
                "Pilihan Ganda",
                "Pseudocode biasanya diawali dengan kata...",
                listOf("A. SELESAI", "B. MULAI", "C. LOMPAT", "D. TIDUR"),
                1
            ),
            ExamQuestion.Choice(
                "Pilih Pseudocode yang Benar",
                "Pilih pseudocode yang benar untuk membaca buku.",
                listOf(
                    "A.\nMULAI\nAmbil buku\nBuka buku\nBaca buku\nTutup buku\nSELESAI",
                    "B.\nSELESAI\nBaca buku\nMULAI\nAmbil buku",
                    "C.\nBaca buku\nTidur\nLempar buku\nSELESAI"
                ),
                0
            ),
            ExamQuestion.Matching(
                "Cocokkan Pasangan",
                "Cocokkan simbol flowchart dengan artinya.",
                listOf("Oval", "Persegi panjang", "Belah ketupat", "Panah"),
                listOf("Mulai atau selesai", "Proses atau langkah", "Pilihan", "Arah langkah berikutnya"),
                mapOf(
                    "Oval" to "Mulai atau selesai",
                    "Persegi panjang" to "Proses atau langkah",
                    "Belah ketupat" to "Pilihan",
                    "Panah" to "Arah langkah berikutnya"
                )
            ),
            ExamQuestion.Choice(
                "Pilihan Ganda",
                "Simbol belah ketupat dalam flowchart digunakan untuk...",
                listOf("A. Pilihan atau keputusan", "B. Mulai dan selesai", "C. Menghapus langkah", "D. Menggambar bebas"),
                0
            ),
            ExamQuestion.Choice(
                "Klik Bagian yang Salah",
                "[MULAI]\n|\n[Tuang air]\n|\n[Ambil gelas]\n|\n[Aduk]\n|\n[SELESAI]\n\nBagian mana yang salah?",
                listOf(
                    "A. Tuang air dilakukan sebelum ambil gelas",
                    "B. Ada simbol selesai",
                    "C. Ada simbol mulai",
                    "D. Ada panah ke bawah"
                ),
                0
            ),
            ExamQuestion.Choice(
                "Pilih Jalur Iya atau Tidak",
                "Apakah hari ini hujan?\n\nCuaca: HUJAN",
                listOf("IYA -> Bawa payung", "TIDAK -> Pergi tanpa payung"),
                0
            ),
            ExamQuestion.Choice(
                "Pilihan Ganda",
                "Jika kamu lapar, langkah yang tepat adalah...",
                listOf("A. Makan dulu", "B. Buang piring", "C. Tidur di jalan", "D. Menyembunyikan sendok"),
                0
            ),
            ExamQuestion.TapCount(
                "Tap Sesuai Jumlah",
                "Bantu Robot Algo melompat 4 kali.",
                4
            ),
            ExamQuestion.Choice(
                "Pilih Jawaban Akhir",
                "Pengulangan artinya...",
                listOf(
                    "A. Melakukan sesuatu berkali-kali sampai selesai",
                    "B. Melakukan sesuatu tanpa urutan",
                    "C. Menghapus semua langkah",
                    "D. Tidak melakukan apa-apa"
                ),
                0
            )
        )
    }
}
