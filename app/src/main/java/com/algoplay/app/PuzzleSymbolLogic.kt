package com.algoplay.app

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.time.LocalDate
import java.time.ZoneId

data class PuzzleQuestion(
    val id: Int = 0,
    val target: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val answer: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "target" to target,
        "question" to question,
        "options" to options,
        "answer" to answer
    )

    companion object {
        fun fromMap(data: Map<String, Any?>?): PuzzleQuestion {
            return PuzzleQuestion(
                id = data.intValue("id"),
                target = data.stringValue("target"),
                question = data.stringValue("question"),
                options = data.listValue("options"),
                answer = data.stringValue("answer")
            )
        }
    }
}

data class PuzzleBlock(
    val row: Int = 0,
    val col: Int = 0,
    val colorHex: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "row" to row,
        "col" to col,
        "colorHex" to colorHex
    )
}

data class PuzzlePatternQuestion(
    val id: Int = 0,
    val title: String = "",
    val question: String = "",
    val blocks: List<PuzzleBlock> = emptyList()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "title" to title,
        "question" to question,
        "blocks" to blocks.map { it.toMap() }
    )
}

data class PuzzleStats(
    val totalMainPuzzle: Int = 0,
    val totalSkorPuzzle: Int = 0,
    val totalPoinLeaderboardPuzzle: Int = 0,
    val skorTertinggiPuzzle: Int = 0,
    val rataRataSkorPuzzle: Double = 0.0,
    val jumlahPercobaanPuzzleHariIni: Int = 0,
    val tanggalResetPuzzle: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "totalMainPuzzle" to totalMainPuzzle,
        "totalSkorPuzzle" to totalSkorPuzzle,
        "totalPoinLeaderboardPuzzle" to totalPoinLeaderboardPuzzle,
        "skorTertinggiPuzzle" to skorTertinggiPuzzle,
        "rataRataSkorPuzzle" to rataRataSkorPuzzle,
        "jumlahPercobaanPuzzleHariIni" to jumlahPercobaanPuzzleHariIni,
        "tanggalResetPuzzle" to tanggalResetPuzzle
    )

    companion object {
        fun fromMap(data: Map<String, Any?>?): PuzzleStats {
            return PuzzleStats(
                totalMainPuzzle = data.intValue("totalMainPuzzle"),
                totalSkorPuzzle = data.intValue("totalSkorPuzzle"),
                totalPoinLeaderboardPuzzle = data.intValue("totalPoinLeaderboardPuzzle"),
                skorTertinggiPuzzle = data.intValue("skorTertinggiPuzzle"),
                rataRataSkorPuzzle = data.doubleValue("rataRataSkorPuzzle"),
                jumlahPercobaanPuzzleHariIni = data.intValue("jumlahPercobaanPuzzleHariIni"),
                tanggalResetPuzzle = data.stringValue("tanggalResetPuzzle")
            )
        }
    }
}

data class PuzzleSessionResult(
    val userId: String = "",
    val correctAnswer: Int = 0,
    val wrongAnswer: Int = 0,
    val sessionScore: Int = 0,
    val difficulty: String = PuzzleDifficulty.EASY.key,
    val bonusPoint: Int = 0,
    val leaderboardPointEarned: Int = 0,
    val isRewarded: Boolean = false,
    val playedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "correctAnswer" to correctAnswer,
        "wrongAnswer" to wrongAnswer,
        "sessionScore" to sessionScore,
        "difficulty" to difficulty,
        "bonusPoint" to bonusPoint,
        "leaderboardPointEarned" to leaderboardPointEarned,
        "isRewarded" to isRewarded,
        "playedAt" to playedAt
    )
}

data class PuzzleSessionOutput(
    val userId: String = "",
    val jumlahBenar: Int = 0,
    val jumlahSalah: Int = 0,
    val skorSesi: Int = 0,
    val bonusLevel: Int = 0,
    val poinLeaderboardDidapat: Int = 0,
    val statusBerpoin: Boolean = false,
    val totalScoreLeaderboardTerbaru: Int = 0,
    val skorTertinggiPuzzle: Int = 0,
    val rataRataSkorPuzzle: Double = 0.0,
    val jumlahPercobaanPuzzleHariIni: Int = 0,
    val updatedStats: PuzzleStats = PuzzleStats(),
    val sessionResult: PuzzleSessionResult = PuzzleSessionResult()
)

enum class PuzzleDifficulty(
    val key: String,
    val label: String,
    val timeLimitSeconds: Int,
    val perfectScoreBonus: Int
) {
    EASY("mudah", "Mudah", 180, 0),
    MEDIUM("sedang", "Sedang", 120, 20),
    HARD("susah", "Susah", 30, 50)
}

object PuzzleSymbolEngine {
    const val QUESTION_PER_SESSION = 5
    const val POINT_PER_CORRECT_ANSWER = 20
    const val MAX_DAILY_REWARDED_ATTEMPT = 2
    const val PATTERN_GRID_SIZE = 5

    private fun block(row: Int, col: Int, colorHex: String): PuzzleBlock {
        return PuzzleBlock(row, col, colorHex)
    }

    val puzzlePatternQuestions = listOf(
        PuzzlePatternQuestion(
            id = 1,
            title = "Tanda Tambah",
            question = "Tiru pola tanda tambah di papan 5x5.",
            blocks = listOf(
                block(1, 2, "#38BDF8"),
                block(2, 1, "#4ADE80"),
                block(2, 2, "#FACC15"),
                block(2, 3, "#F97316"),
                block(3, 2, "#EC4899")
            )
        ),
        PuzzlePatternQuestion(
            id = 2,
            title = "Kepala Rumah",
            question = "Susun balok agar membentuk kepala rumah.",
            blocks = listOf(
                block(1, 2, "#FACC15"),
                block(2, 1, "#F97316"),
                block(2, 2, "#F97316"),
                block(2, 3, "#F97316"),
                block(3, 1, "#38BDF8"),
                block(3, 3, "#38BDF8")
            )
        ),
        PuzzlePatternQuestion(
            id = 3,
            title = "Huruf L",
            question = "Tiru pola huruf L dengan warna yang sama.",
            blocks = listOf(
                block(0, 1, "#4ADE80"),
                block(1, 1, "#4ADE80"),
                block(2, 1, "#4ADE80"),
                block(3, 1, "#FACC15"),
                block(3, 2, "#FACC15"),
                block(3, 3, "#FACC15")
            )
        ),
        PuzzlePatternQuestion(
            id = 4,
            title = "Huruf T",
            question = "Tiru pola huruf T di papan kosong.",
            blocks = listOf(
                block(1, 1, "#38BDF8"),
                block(1, 2, "#38BDF8"),
                block(1, 3, "#38BDF8"),
                block(2, 2, "#8B5CF6"),
                block(3, 2, "#8B5CF6")
            )
        ),
        PuzzlePatternQuestion(
            id = 5,
            title = "Tangga Naik",
            question = "Susun balok menjadi tangga naik.",
            blocks = listOf(
                block(4, 0, "#F97316"),
                block(3, 1, "#FACC15"),
                block(2, 2, "#4ADE80"),
                block(1, 3, "#38BDF8"),
                block(0, 4, "#8B5CF6")
            )
        ),
        PuzzlePatternQuestion(
            id = 6,
            title = "Tangga Turun",
            question = "Susun balok menjadi tangga turun.",
            blocks = listOf(
                block(0, 0, "#8B5CF6"),
                block(1, 1, "#38BDF8"),
                block(2, 2, "#4ADE80"),
                block(3, 3, "#FACC15"),
                block(4, 4, "#F97316")
            )
        ),
        PuzzlePatternQuestion(
            id = 7,
            title = "Garis Tengah",
            question = "Tiru garis mendatar di tengah papan.",
            blocks = listOf(
                block(2, 0, "#EF4444"),
                block(2, 1, "#F97316"),
                block(2, 2, "#FACC15"),
                block(2, 3, "#4ADE80"),
                block(2, 4, "#38BDF8")
            )
        ),
        PuzzlePatternQuestion(
            id = 8,
            title = "Menara",
            question = "Buat menara tegak seperti contoh.",
            blocks = listOf(
                block(0, 2, "#EC4899"),
                block(1, 2, "#EC4899"),
                block(2, 2, "#8B5CF6"),
                block(3, 2, "#38BDF8"),
                block(4, 2, "#4ADE80")
            )
        ),
        PuzzlePatternQuestion(
            id = 9,
            title = "Sudut Kiri Atas",
            question = "Tiru pola sudut kiri atas.",
            blocks = listOf(
                block(0, 0, "#FACC15"),
                block(0, 1, "#FACC15"),
                block(0, 2, "#F97316"),
                block(1, 0, "#38BDF8"),
                block(2, 0, "#38BDF8")
            )
        ),
        PuzzlePatternQuestion(
            id = 10,
            title = "Sudut Kanan Bawah",
            question = "Tiru pola sudut kanan bawah.",
            blocks = listOf(
                block(2, 4, "#38BDF8"),
                block(3, 4, "#38BDF8"),
                block(4, 2, "#F97316"),
                block(4, 3, "#FACC15"),
                block(4, 4, "#FACC15")
            )
        ),
        PuzzlePatternQuestion(
            id = 11,
            title = "Panah Kanan",
            question = "Bentuk panah mengarah ke kanan.",
            blocks = listOf(
                block(2, 0, "#4ADE80"),
                block(2, 1, "#4ADE80"),
                block(2, 2, "#4ADE80"),
                block(1, 3, "#FACC15"),
                block(2, 3, "#FACC15"),
                block(3, 3, "#FACC15")
            )
        ),
        PuzzlePatternQuestion(
            id = 12,
            title = "Panah Kiri",
            question = "Bentuk panah mengarah ke kiri.",
            blocks = listOf(
                block(2, 4, "#38BDF8"),
                block(2, 3, "#38BDF8"),
                block(2, 2, "#38BDF8"),
                block(1, 1, "#F97316"),
                block(2, 1, "#F97316"),
                block(3, 1, "#F97316")
            )
        ),
        PuzzlePatternQuestion(
            id = 13,
            title = "Jembatan",
            question = "Susun pola seperti jembatan kecil.",
            blocks = listOf(
                block(3, 0, "#8B5CF6"),
                block(2, 1, "#38BDF8"),
                block(2, 2, "#38BDF8"),
                block(2, 3, "#38BDF8"),
                block(3, 4, "#8B5CF6")
            )
        ),
        PuzzlePatternQuestion(
            id = 14,
            title = "Pintu",
            question = "Tiru pola pintu berwarna.",
            blocks = listOf(
                block(1, 1, "#F97316"),
                block(1, 2, "#FACC15"),
                block(1, 3, "#F97316"),
                block(2, 1, "#38BDF8"),
                block(2, 3, "#38BDF8"),
                block(3, 1, "#4ADE80"),
                block(3, 2, "#4ADE80"),
                block(3, 3, "#4ADE80")
            )
        ),
        PuzzlePatternQuestion(
            id = 15,
            title = "Mahkota",
            question = "Buat pola mahkota kecil.",
            blocks = listOf(
                block(1, 0, "#FACC15"),
                block(2, 1, "#F97316"),
                block(1, 2, "#FACC15"),
                block(2, 3, "#F97316"),
                block(1, 4, "#FACC15"),
                block(3, 1, "#FACC15"),
                block(3, 2, "#FACC15"),
                block(3, 3, "#FACC15")
            )
        ),
        PuzzlePatternQuestion(
            id = 16,
            title = "Tangga Kecil",
            question = "Tiru pola tangga kecil.",
            blocks = listOf(
                block(4, 1, "#4ADE80"),
                block(4, 2, "#4ADE80"),
                block(3, 2, "#38BDF8"),
                block(3, 3, "#38BDF8"),
                block(2, 3, "#FACC15"),
                block(2, 4, "#FACC15")
            )
        ),
        PuzzlePatternQuestion(
            id = 17,
            title = "Pola X",
            question = "Susun balok menyerupai huruf X.",
            blocks = listOf(
                block(1, 1, "#EF4444"),
                block(1, 3, "#EF4444"),
                block(2, 2, "#FACC15"),
                block(3, 1, "#38BDF8"),
                block(3, 3, "#38BDF8")
            )
        ),
        PuzzlePatternQuestion(
            id = 18,
            title = "Perahu",
            question = "Tiru bentuk perahu kecil.",
            blocks = listOf(
                block(2, 1, "#38BDF8"),
                block(2, 2, "#38BDF8"),
                block(2, 3, "#38BDF8"),
                block(3, 0, "#F97316"),
                block(3, 1, "#F97316"),
                block(3, 2, "#F97316"),
                block(3, 3, "#F97316"),
                block(3, 4, "#F97316")
            )
        ),
        PuzzlePatternQuestion(
            id = 19,
            title = "Piramida",
            question = "Buat piramida dari balok warna.",
            blocks = listOf(
                block(1, 2, "#FACC15"),
                block(2, 1, "#F97316"),
                block(2, 2, "#F97316"),
                block(2, 3, "#F97316"),
                block(3, 0, "#38BDF8"),
                block(3, 1, "#38BDF8"),
                block(3, 2, "#38BDF8"),
                block(3, 3, "#38BDF8"),
                block(3, 4, "#38BDF8")
            )
        ),
        PuzzlePatternQuestion(
            id = 20,
            title = "Kunci",
            question = "Tiru pola seperti kunci.",
            blocks = listOf(
                block(1, 1, "#FACC15"),
                block(1, 2, "#FACC15"),
                block(2, 1, "#FACC15"),
                block(2, 2, "#FACC15"),
                block(2, 3, "#38BDF8"),
                block(2, 4, "#38BDF8")
            )
        ),
        PuzzlePatternQuestion(
            id = 21,
            title = "Kincir",
            question = "Tiru pola kincir warna-warni.",
            blocks = listOf(
                block(0, 2, "#38BDF8"),
                block(2, 0, "#4ADE80"),
                block(2, 2, "#FACC15"),
                block(2, 4, "#F97316"),
                block(4, 2, "#EC4899")
            )
        ),
        PuzzlePatternQuestion(
            id = 22,
            title = "Ular Zigzag",
            question = "Susun pola zigzag seperti ular kecil.",
            blocks = listOf(
                block(1, 0, "#4ADE80"),
                block(1, 1, "#4ADE80"),
                block(2, 1, "#38BDF8"),
                block(2, 2, "#38BDF8"),
                block(3, 2, "#FACC15"),
                block(3, 3, "#FACC15")
            )
        ),
        PuzzlePatternQuestion(
            id = 23,
            title = "Tangga Warna",
            question = "Tiru tangga warna di contoh.",
            blocks = listOf(
                block(0, 0, "#EF4444"),
                block(1, 0, "#F97316"),
                block(1, 1, "#FACC15"),
                block(2, 1, "#4ADE80"),
                block(2, 2, "#38BDF8"),
                block(3, 2, "#8B5CF6"),
                block(3, 3, "#EC4899")
            )
        ),
        PuzzlePatternQuestion(
            id = 24,
            title = "Kotak Kecil",
            question = "Tiru kotak kecil di tengah papan.",
            blocks = listOf(
                block(1, 1, "#38BDF8"),
                block(1, 2, "#4ADE80"),
                block(2, 1, "#FACC15"),
                block(2, 2, "#F97316")
            )
        ),
        PuzzlePatternQuestion(
            id = 25,
            title = "Jalan Belok",
            question = "Tiru pola jalan yang berbelok.",
            blocks = listOf(
                block(0, 2, "#38BDF8"),
                block(1, 2, "#38BDF8"),
                block(2, 2, "#FACC15"),
                block(2, 3, "#F97316"),
                block(2, 4, "#F97316")
            )
        ),
        PuzzlePatternQuestion(
            id = 26,
            title = "Bendera",
            question = "Tiru pola bendera kecil.",
            blocks = listOf(
                block(0, 1, "#8B5CF6"),
                block(1, 1, "#8B5CF6"),
                block(2, 1, "#8B5CF6"),
                block(0, 2, "#EF4444"),
                block(0, 3, "#EF4444"),
                block(1, 2, "#FACC15")
            )
        ),
        PuzzlePatternQuestion(
            id = 27,
            title = "Kursi",
            question = "Susun pola seperti kursi kecil.",
            blocks = listOf(
                block(1, 1, "#38BDF8"),
                block(2, 1, "#38BDF8"),
                block(2, 2, "#4ADE80"),
                block(2, 3, "#4ADE80"),
                block(3, 1, "#F97316"),
                block(3, 3, "#F97316")
            )
        ),
        PuzzlePatternQuestion(
            id = 28,
            title = "Jam Pasir",
            question = "Tiru pola jam pasir.",
            blocks = listOf(
                block(1, 1, "#FACC15"),
                block(1, 2, "#FACC15"),
                block(1, 3, "#FACC15"),
                block(2, 2, "#F97316"),
                block(3, 1, "#38BDF8"),
                block(3, 2, "#38BDF8"),
                block(3, 3, "#38BDF8")
            )
        ),
        PuzzlePatternQuestion(
            id = 29,
            title = "Sayap",
            question = "Susun pola seperti sayap kecil.",
            blocks = listOf(
                block(1, 0, "#38BDF8"),
                block(2, 1, "#38BDF8"),
                block(2, 2, "#FACC15"),
                block(2, 3, "#EC4899"),
                block(1, 4, "#EC4899")
            )
        ),
        PuzzlePatternQuestion(
            id = 30,
            title = "Robot Mini",
            question = "Tiru pola robot mini.",
            blocks = listOf(
                block(1, 1, "#38BDF8"),
                block(1, 2, "#38BDF8"),
                block(1, 3, "#38BDF8"),
                block(2, 1, "#FACC15"),
                block(2, 2, "#FACC15"),
                block(2, 3, "#FACC15"),
                block(3, 1, "#4ADE80"),
                block(3, 3, "#4ADE80")
            )
        )
    )

    val puzzleQuestions = listOf(
        PuzzleQuestion(
            id = 1,
            target = "Lingkaran",
            question = "Cocokkan bentuk yang sama dengan Lingkaran.",
            options = listOf("Lingkaran", "Persegi", "Segitiga", "Bintang"),
            answer = "Lingkaran"
        ),
        PuzzleQuestion(
            id = 2,
            target = "Persegi",
            question = "Cocokkan bentuk yang sama dengan Persegi.",
            options = listOf("Segitiga", "Persegi", "Oval", "Hati"),
            answer = "Persegi"
        ),
        PuzzleQuestion(
            id = 3,
            target = "Segitiga",
            question = "Cocokkan bentuk yang sama dengan Segitiga.",
            options = listOf("Bintang", "Lingkaran", "Segitiga", "Persegi Panjang"),
            answer = "Segitiga"
        ),
        PuzzleQuestion(
            id = 4,
            target = "Bintang",
            question = "Cocokkan bentuk yang sama dengan Bintang.",
            options = listOf("Bulan Sabit", "Bintang", "Oval", "Persegi"),
            answer = "Bintang"
        ),
        PuzzleQuestion(
            id = 5,
            target = "Hati",
            question = "Cocokkan bentuk yang sama dengan Hati.",
            options = listOf("Lingkaran", "Hati", "Segitiga", "Belah Ketupat"),
            answer = "Hati"
        ),
        PuzzleQuestion(
            id = 6,
            target = "Oval",
            question = "Cocokkan bentuk yang sama dengan Oval.",
            options = listOf("Oval", "Persegi", "Bintang", "Panah"),
            answer = "Oval"
        ),
        PuzzleQuestion(
            id = 7,
            target = "Belah Ketupat",
            question = "Cocokkan bentuk yang sama dengan Belah Ketupat.",
            options = listOf("Segitiga", "Belah Ketupat", "Lingkaran", "Hati"),
            answer = "Belah Ketupat"
        ),
        PuzzleQuestion(
            id = 8,
            target = "Persegi Panjang",
            question = "Cocokkan bentuk yang sama dengan Persegi Panjang.",
            options = listOf("Persegi", "Oval", "Persegi Panjang", "Bintang"),
            answer = "Persegi Panjang"
        ),
        PuzzleQuestion(
            id = 9,
            target = "Panah Kanan",
            question = "Cocokkan bentuk yang sama dengan Panah Kanan.",
            options = listOf("Panah Kiri", "Panah Atas", "Panah Kanan", "Panah Bawah"),
            answer = "Panah Kanan"
        ),
        PuzzleQuestion(
            id = 10,
            target = "Panah Kiri",
            question = "Cocokkan bentuk yang sama dengan Panah Kiri.",
            options = listOf("Panah Kanan", "Panah Kiri", "Panah Atas", "Lingkaran"),
            answer = "Panah Kiri"
        ),
        PuzzleQuestion(
            id = 11,
            target = "Panah Atas",
            question = "Cocokkan bentuk yang sama dengan Panah Atas.",
            options = listOf("Panah Bawah", "Panah Kanan", "Panah Atas", "Persegi"),
            answer = "Panah Atas"
        ),
        PuzzleQuestion(
            id = 12,
            target = "Panah Bawah",
            question = "Cocokkan bentuk yang sama dengan Panah Bawah.",
            options = listOf("Panah Atas", "Panah Bawah", "Segitiga", "Oval"),
            answer = "Panah Bawah"
        ),
        PuzzleQuestion(
            id = 13,
            target = "Bulan Sabit",
            question = "Cocokkan bentuk yang sama dengan Bulan Sabit.",
            options = listOf("Matahari", "Bintang", "Bulan Sabit", "Awan"),
            answer = "Bulan Sabit"
        ),
        PuzzleQuestion(
            id = 14,
            target = "Matahari",
            question = "Cocokkan bentuk yang sama dengan Matahari.",
            options = listOf("Matahari", "Lingkaran", "Hati", "Persegi"),
            answer = "Matahari"
        ),
        PuzzleQuestion(
            id = 15,
            target = "Awan",
            question = "Cocokkan bentuk yang sama dengan Awan.",
            options = listOf("Petir", "Awan", "Bintang", "Segitiga"),
            answer = "Awan"
        ),
        PuzzleQuestion(
            id = 16,
            target = "Petir",
            question = "Cocokkan bentuk yang sama dengan Petir.",
            options = listOf("Petir", "Bulan Sabit", "Oval", "Hati"),
            answer = "Petir"
        ),
        PuzzleQuestion(
            id = 17,
            target = "Tanda Tambah",
            question = "Cocokkan simbol yang sama dengan Tanda Tambah.",
            options = listOf("Tanda Tambah", "Tanda Kurang", "Tanda Kali", "Lingkaran"),
            answer = "Tanda Tambah"
        ),
        PuzzleQuestion(
            id = 18,
            target = "Tanda Kurang",
            question = "Cocokkan simbol yang sama dengan Tanda Kurang.",
            options = listOf("Tanda Kali", "Tanda Tambah", "Tanda Kurang", "Persegi"),
            answer = "Tanda Kurang"
        ),
        PuzzleQuestion(
            id = 19,
            target = "Tanda Kali",
            question = "Cocokkan simbol yang sama dengan Tanda Kali.",
            options = listOf("Tanda Kurang", "Tanda Kali", "Bintang", "Oval"),
            answer = "Tanda Kali"
        ),
        PuzzleQuestion(
            id = 20,
            target = "Tanda Sama Dengan",
            question = "Cocokkan simbol yang sama dengan Tanda Sama Dengan.",
            options = listOf("Tanda Tambah", "Tanda Sama Dengan", "Tanda Kali", "Panah Kanan"),
            answer = "Tanda Sama Dengan"
        ),
        PuzzleQuestion(
            id = 21,
            target = "Rumah",
            question = "Cocokkan gambar yang sama dengan Rumah.",
            options = listOf("Rumah", "Pohon", "Mobil", "Bintang"),
            answer = "Rumah"
        ),
        PuzzleQuestion(
            id = 22,
            target = "Pohon",
            question = "Cocokkan gambar yang sama dengan Pohon.",
            options = listOf("Awan", "Pohon", "Rumah", "Hati"),
            answer = "Pohon"
        ),
        PuzzleQuestion(
            id = 23,
            target = "Mobil",
            question = "Cocokkan gambar yang sama dengan Mobil.",
            options = listOf("Sepeda", "Pesawat", "Mobil", "Kapal"),
            answer = "Mobil"
        ),
        PuzzleQuestion(
            id = 24,
            target = "Sepeda",
            question = "Cocokkan gambar yang sama dengan Sepeda.",
            options = listOf("Mobil", "Sepeda", "Rumah", "Matahari"),
            answer = "Sepeda"
        ),
        PuzzleQuestion(
            id = 25,
            target = "Pesawat",
            question = "Cocokkan gambar yang sama dengan Pesawat.",
            options = listOf("Kapal", "Mobil", "Pesawat", "Pohon"),
            answer = "Pesawat"
        ),
        PuzzleQuestion(
            id = 26,
            target = "Kapal",
            question = "Cocokkan gambar yang sama dengan Kapal.",
            options = listOf("Pesawat", "Kapal", "Sepeda", "Awan"),
            answer = "Kapal"
        ),
        PuzzleQuestion(
            id = 27,
            target = "Apel",
            question = "Cocokkan gambar yang sama dengan Apel.",
            options = listOf("Apel", "Pisang", "Jeruk", "Semangka"),
            answer = "Apel"
        ),
        PuzzleQuestion(
            id = 28,
            target = "Pisang",
            question = "Cocokkan gambar yang sama dengan Pisang.",
            options = listOf("Jeruk", "Apel", "Pisang", "Anggur"),
            answer = "Pisang"
        ),
        PuzzleQuestion(
            id = 29,
            target = "Jeruk",
            question = "Cocokkan gambar yang sama dengan Jeruk.",
            options = listOf("Semangka", "Jeruk", "Pisang", "Apel"),
            answer = "Jeruk"
        ),
        PuzzleQuestion(
            id = 30,
            target = "Semangka",
            question = "Cocokkan gambar yang sama dengan Semangka.",
            options = listOf("Anggur", "Apel", "Semangka", "Jeruk"),
            answer = "Semangka"
        )
    )

    fun getRandomPuzzleQuestions(allQuestions: List<PuzzleQuestion> = puzzleQuestions): List<PuzzleQuestion> {
        val uniqueQuestions = allQuestions.distinctBy { it.id }
        require(uniqueQuestions.size >= QUESTION_PER_SESSION) {
            "Bank soal Puzzle Simbol harus berisi minimal $QUESTION_PER_SESSION soal unik."
        }
        return uniqueQuestions.shuffled().take(QUESTION_PER_SESSION)
    }

    fun getRandomPuzzlePatternQuestions(
        allQuestions: List<PuzzlePatternQuestion> = puzzlePatternQuestions
    ): List<PuzzlePatternQuestion> {
        val uniqueQuestions = allQuestions.distinctBy { it.id }
        require(uniqueQuestions.size >= QUESTION_PER_SESSION) {
            "Bank pola Puzzle Simbol harus berisi minimal $QUESTION_PER_SESSION soal unik."
        }
        return uniqueQuestions.shuffled().take(QUESTION_PER_SESSION)
    }

    fun calculatePuzzleScore(correctAnswer: Int): Int {
        return correctAnswer.coerceIn(0, QUESTION_PER_SESSION) * POINT_PER_CORRECT_ANSWER
    }

    fun todayDate(zoneId: ZoneId = ZoneId.systemDefault()): String {
        return LocalDate.now(zoneId).toString()
    }

    fun finishPuzzleSession(
        userId: String,
        correctAnswer: Int,
        currentStats: PuzzleStats,
        currentTotalLeaderboardScore: Int,
        todayDate: String,
        difficulty: PuzzleDifficulty = PuzzleDifficulty.EASY
    ): PuzzleSessionResult {
        return finishPuzzleSessionUpdate(
            userId = userId,
            correctAnswer = correctAnswer,
            currentStats = currentStats,
            currentTotalLeaderboardScore = currentTotalLeaderboardScore,
            todayDate = todayDate,
            difficulty = difficulty
        ).sessionResult
    }

    fun finishPuzzleSessionUpdate(
        userId: String,
        correctAnswer: Int,
        currentStats: PuzzleStats,
        currentTotalLeaderboardScore: Int,
        todayDate: String,
        difficulty: PuzzleDifficulty = PuzzleDifficulty.EASY,
        playedAt: Long = System.currentTimeMillis()
    ): PuzzleSessionOutput {
        val safeCorrectAnswer = correctAnswer.coerceIn(0, QUESTION_PER_SESSION)
        val sessionScore = calculatePuzzleScore(safeCorrectAnswer)
        val bonusPoint = if (sessionScore == 100) difficulty.perfectScoreBonus else 0
        val isNewDay = currentStats.tanggalResetPuzzle != todayDate
        val todayAttempt = if (isNewDay) 0 else currentStats.jumlahPercobaanPuzzleHariIni
        val isRewarded = todayAttempt < MAX_DAILY_REWARDED_ATTEMPT
        val leaderboardPoint = if (isRewarded) sessionScore + bonusPoint else 0

        val newTotalMain = currentStats.totalMainPuzzle + 1
        val newTotalScore = currentStats.totalSkorPuzzle + sessionScore
        val newHighestScore = maxOf(currentStats.skorTertinggiPuzzle, sessionScore)
        val newAverageScore = newTotalScore.toDouble() / newTotalMain.toDouble()
        val newTodayAttempt = todayAttempt + 1
        val newTotalLeaderboardPuzzle = currentStats.totalPoinLeaderboardPuzzle + leaderboardPoint
        val newTotalLeaderboardScore = currentTotalLeaderboardScore + leaderboardPoint

        val updatedStats = PuzzleStats(
            totalMainPuzzle = newTotalMain,
            totalSkorPuzzle = newTotalScore,
            totalPoinLeaderboardPuzzle = newTotalLeaderboardPuzzle,
            skorTertinggiPuzzle = newHighestScore,
            rataRataSkorPuzzle = newAverageScore,
            jumlahPercobaanPuzzleHariIni = newTodayAttempt,
            tanggalResetPuzzle = todayDate
        )

        val result = PuzzleSessionResult(
            userId = userId,
            correctAnswer = safeCorrectAnswer,
            wrongAnswer = QUESTION_PER_SESSION - safeCorrectAnswer,
            sessionScore = sessionScore,
            difficulty = difficulty.key,
            bonusPoint = if (isRewarded) bonusPoint else 0,
            leaderboardPointEarned = leaderboardPoint,
            isRewarded = isRewarded,
            playedAt = playedAt
        )

        return PuzzleSessionOutput(
            userId = userId,
            jumlahBenar = result.correctAnswer,
            jumlahSalah = result.wrongAnswer,
            skorSesi = result.sessionScore,
            bonusLevel = result.bonusPoint,
            poinLeaderboardDidapat = result.leaderboardPointEarned,
            statusBerpoin = result.isRewarded,
            totalScoreLeaderboardTerbaru = newTotalLeaderboardScore,
            skorTertinggiPuzzle = updatedStats.skorTertinggiPuzzle,
            rataRataSkorPuzzle = updatedStats.rataRataSkorPuzzle,
            jumlahPercobaanPuzzleHariIni = updatedStats.jumlahPercobaanPuzzleHariIni,
            updatedStats = updatedStats,
            sessionResult = result
        )
    }
}

class PuzzleSymbolRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val zoneId: ZoneId = ZoneId.systemDefault()
) {

    fun seedPuzzleQuestions(
        questions: List<PuzzlePatternQuestion> = PuzzleSymbolEngine.puzzlePatternQuestions
    ): Task<Void> {
        val batch = firestore.batch()
        val collection = firestore.collection(PUZZLE_QUESTIONS_COLLECTION)
        questions.forEach { question ->
            batch.set(
                collection.document(question.id.toString()),
                question.toMap(),
                SetOptions.merge()
            )
        }
        return batch.commit()
    }

    fun getRandomPuzzleQuestions(
        questions: List<PuzzlePatternQuestion> = PuzzleSymbolEngine.puzzlePatternQuestions
    ): List<PuzzlePatternQuestion> {
        return PuzzleSymbolEngine.getRandomPuzzlePatternQuestions(questions)
    }

    fun finishSession(
        userId: String,
        correctAnswer: Int,
        difficulty: PuzzleDifficulty = PuzzleDifficulty.EASY
    ): Task<PuzzleSessionOutput> {
        val todayDate = PuzzleSymbolEngine.todayDate(zoneId)
        val userRef = firestore.collection(USERS_COLLECTION).document(userId)
        val historyRef = userRef.collection(PUZZLE_HISTORY_COLLECTION).document()

        return firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentStats = PuzzleStats.fromMap(snapshot.get(PUZZLE_STATS_FIELD).stringKeyMap())
            val currentTotalLeaderboardScore = snapshot.getLong(TOTAL_SCORE_LEADERBOARD_FIELD)?.toInt() ?: 0

            val output = PuzzleSymbolEngine.finishPuzzleSessionUpdate(
                userId = userId,
                correctAnswer = correctAnswer,
                currentStats = currentStats,
                currentTotalLeaderboardScore = currentTotalLeaderboardScore,
                todayDate = todayDate,
                difficulty = difficulty
            )

            transaction.set(
                userRef,
                mapOf(
                    "userId" to userId,
                    PUZZLE_STATS_FIELD to output.updatedStats.toMap()
                ),
                SetOptions.merge()
            )
            transaction.set(historyRef, output.sessionResult.toMap())
            output
        }
    }

    companion object {
        const val USERS_COLLECTION = "users"
        const val PUZZLE_QUESTIONS_COLLECTION = "puzzleQuestions"
        const val PUZZLE_STATS_FIELD = "puzzleStats"
        const val PUZZLE_HISTORY_COLLECTION = "puzzleHistory"
        const val TOTAL_SCORE_LEADERBOARD_FIELD = "totalScoreLeaderboard"
    }
}

private fun Map<String, Any?>?.intValue(key: String): Int {
    return when (val value = this?.get(key)) {
        is Number -> value.toInt()
        is String -> value.toIntOrNull() ?: 0
        else -> 0
    }
}

private fun Map<String, Any?>?.doubleValue(key: String): Double {
    return when (val value = this?.get(key)) {
        is Number -> value.toDouble()
        is String -> value.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }
}

private fun Map<String, Any?>?.stringValue(key: String): String {
    return this?.get(key)?.toString().orEmpty()
}

private fun Map<String, Any?>?.listValue(key: String): List<String> {
    val rawList = this?.get(key) as? List<*> ?: return emptyList()
    return rawList.mapNotNull { it?.toString() }
}

private fun Any?.stringKeyMap(): Map<String, Any?>? {
    val rawMap = this as? Map<*, *> ?: return null
    return rawMap.entries.associate { entry ->
        entry.key.toString() to entry.value
    }
}
