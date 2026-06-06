package com.algoplay.app

class LessonTwoActivity : LessonOneActivity() {

    override val layoutResId: Int = R.layout.activity_lesson_two
    override val lessonNumber: Int = 2
    override val nextLessonNumber: Int = 3

    override val correctSteps: List<String> = listOf(
        "Bangun tidur",
        "Mandi",
        "Memakai seragam",
        "Sarapan",
        "Memakai sepatu",
        "Berangkat ke sekolah"
    )

    override val shuffledSteps: List<String> = listOf(
        "Memakai sepatu",
        "Bangun tidur",
        "Berangkat ke sekolah",
        "Mandi",
        "Sarapan",
        "Memakai seragam"
    )

    override val quizOneOptions: List<String> = listOf(
        "A. Langkah-langkah yang berurutan",
        "B. Warna pelangi",
        "C. Suara musik",
        "D. Gambar mainan"
    )
    override val quizOneCorrectIndex: Int = 0
    override val quizOneCorrectMessage: String =
        "Benar sekali! Algoritma itu seperti langkah-langkah yang berurutan."
    override val quizOneWrongMessage: String =
        "Belum tepat. Algoritma itu seperti langkah-langkah untuk melakukan sesuatu."

    override val quizTwoOptions: List<String> = listOf(
        "A. Berangkat dulu, lalu mandi",
        "B. Bangun tidur, mandi, memakai seragam, lalu berangkat",
        "C. Memakai sepatu, lalu tidur lagi",
        "D. Sarapan, lalu bangun tidur"
    )
    override val quizTwoCorrectIndex: Int = 1
    override val quizTwoCorrectMessage: String = "Pintar! Kamu sudah tahu urutan yang benar."
    override val quizTwoWrongMessage: String =
        "Coba ingat lagi. Sebelum berangkat, kita harus bersiap-siap dulu."

    override val gameSuccessTitle: String = "Yeay!"
    override val gameSuccessMessage: String =
        "Robot Algo siap berangkat ke sekolah. Urutanmu benar!"
    override val gameWrongTitle: String = "Ups"
    override val gameWrongMessage: String =
        "Sepertinya masih ada langkah yang tertukar. Coba bantu Algo lagi ya!"
}
