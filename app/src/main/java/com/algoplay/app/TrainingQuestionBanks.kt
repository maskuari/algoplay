package com.algoplay.app

data class SequenceQuestion(
    val id: Int,
    val title: String,
    val question: String,
    val steps: List<String>
)

data class QuickQuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)

object TrainingQuestionBanks {
    const val SEQUENCE_QUESTION_PER_SESSION = 5
    const val QUIZ_QUESTION_PER_SESSION = 10
    const val CHALLENGE_QUESTION_PER_SESSION = 10

    private fun sequence(id: Int, title: String, vararg steps: String): SequenceQuestion {
        return SequenceQuestion(
            id = id,
            title = title,
            question = "Susun langkah \"$title\" dari awal sampai selesai.",
            steps = steps.toList()
        )
    }

    val sequenceQuestions = listOf(
        sequence(1, "Menyikat gigi", "Ambil sikat gigi", "Beri pasta gigi", "Sikat gigi", "Berkumur", "Simpan sikat gigi"),
        sequence(2, "Memakai sepatu", "Ambil kaus kaki", "Pakai kaus kaki", "Ambil sepatu", "Pakai sepatu", "Ikat tali sepatu"),
        sequence(3, "Membuat susu", "Siapkan gelas", "Masukkan susu", "Tuang air", "Aduk sampai rata", "Susu siap diminum"),
        sequence(4, "Pergi ke sekolah", "Bangun tidur", "Mandi", "Memakai seragam", "Sarapan", "Memakai sepatu", "Berangkat"),
        sequence(5, "Mencuci tangan", "Basahi tangan", "Ambil sabun", "Gosok tangan", "Bilas dengan air", "Keringkan tangan"),
        sequence(6, "Merapikan mainan", "Ambil mainan", "Masukkan ke kotak", "Susun dengan rapi", "Tutup kotak", "Ruangan rapi"),
        sequence(7, "Membuat teh manis", "Siapkan gelas", "Masukkan teh", "Tuang air hangat", "Masukkan gula", "Aduk", "Teh siap"),
        sequence(8, "Membuat roti selai", "Ambil roti", "Ambil selai", "Oleskan selai", "Tutup dengan roti lain", "Roti siap dimakan"),
        sequence(9, "Menggambar", "Siapkan kertas", "Siapkan pensil", "Buat gambar", "Warnai gambar", "Rapikan hasil"),
        sequence(10, "Menanam bunga", "Siapkan pot", "Masukkan tanah", "Masukkan biji", "Siram air", "Letakkan di tempat terang"),
        sequence(11, "Menyalakan televisi", "Ambil remote", "Arahkan ke televisi", "Tekan tombol power", "Pilih acara"),
        sequence(12, "Membaca buku", "Ambil buku", "Buka buku", "Baca buku", "Tutup buku", "Simpan buku"),
        sequence(13, "Membuat jus", "Ambil buah", "Potong buah", "Masukkan ke blender", "Tambahkan air", "Nyalakan blender", "Tuang ke gelas"),
        sequence(14, "Mengisi botol minum", "Ambil botol", "Buka tutup botol", "Isi air", "Tutup botol", "Masukkan ke tas"),
        sequence(15, "Membersihkan meja", "Ambil kain", "Basahi kain", "Lap meja", "Keringkan meja", "Simpan kain"),
        sequence(16, "Mengirim pesan", "Buka aplikasi pesan", "Pilih kontak", "Tulis pesan", "Tekan kirim"),
        sequence(17, "Menghidupkan lampu", "Masuk kamar", "Cari saklar", "Tekan saklar", "Lampu menyala"),
        sequence(18, "Membuka pintu", "Pegang gagang pintu", "Putar gagang", "Dorong pintu", "Masuk ruangan"),
        sequence(19, "Memasak mi instan", "Didihkan air", "Masukkan mi", "Tunggu matang", "Masukkan bumbu", "Aduk", "Mi siap"),
        sequence(20, "Mandi pagi", "Masuk kamar mandi", "Siram badan", "Pakai sabun", "Bilas badan", "Keringkan badan"),
        sequence(21, "Mengumpulkan tugas", "Selesaikan tugas", "Periksa jawaban", "Tulis nama", "Kumpulkan tugas"),
        sequence(22, "Memakai jaket", "Ambil jaket", "Masukkan tangan kanan", "Masukkan tangan kiri", "Tutup resleting"),
        sequence(23, "Mengisi baterai", "Ambil charger", "Colok ke listrik", "Sambungkan ke perangkat", "Tunggu baterai penuh"),
        sequence(24, "Membeli makanan", "Pilih makanan", "Ambil uang", "Bayar makanan", "Terima makanan"),
        sequence(25, "Bermain bola", "Ambil bola", "Pergi ke lapangan", "Tendang bola", "Simpan bola setelah selesai"),
        sequence(26, "Menyapu lantai", "Ambil sapu", "Sapu lantai", "Kumpulkan sampah", "Buang sampah", "Simpan sapu"),
        sequence(27, "Mencuci piring", "Ambil piring kotor", "Beri sabun", "Gosok piring", "Bilas piring", "Tiriskan piring"),
        sequence(28, "Membuat origami", "Siapkan kertas", "Lipat bagian pertama", "Lipat bagian kedua", "Rapikan lipatan", "Origami selesai"),
        sequence(29, "Bersiap tidur", "Gosok gigi", "Ganti baju tidur", "Matikan lampu", "Naik ke tempat tidur", "Tidur"),
        sequence(30, "Membuka aplikasi", "Ambil ponsel", "Buka kunci layar", "Cari ikon aplikasi", "Ketuk ikon aplikasi")
    )

    val quickQuizQuestions = listOf(
        QuickQuizQuestion(1, "Algoritma adalah...", listOf("Urutan langkah", "Warna layar", "Nama robot", "Suara musik"), 0),
        QuickQuizQuestion(2, "Langkah algoritma harus...", listOf("Acak", "Berurutan", "Disembunyikan", "Dihapus"), 1),
        QuickQuizQuestion(3, "Contoh algoritma sehari-hari adalah...", listOf("Menyikat gigi", "Melamun saja", "Warna biru", "Awan lewat"), 0),
        QuickQuizQuestion(4, "Pseudocode mudah dibaca oleh...", listOf("Manusia", "Meja", "Sepatu", "Pensil"), 0),
        QuickQuizQuestion(5, "Pseudocode biasanya dimulai dengan...", listOf("SELESAI", "MULAI", "TIDUR", "LOMPAT"), 1),
        QuickQuizQuestion(6, "Flowchart adalah...", listOf("Gambar alur langkah", "Makanan", "Suara", "Warna"), 0),
        QuickQuizQuestion(7, "Panah pada flowchart menunjukkan...", listOf("Arah langkah", "Nama anak", "Waktu tidur", "Warna baju"), 0),
        QuickQuizQuestion(8, "Oval pada flowchart untuk...", listOf("Mulai/Selesai", "Pilihan", "Input", "Panah"), 0),
        QuickQuizQuestion(9, "Persegi panjang pada flowchart untuk...", listOf("Proses", "Cuaca", "Nama", "Hiasan"), 0),
        QuickQuizQuestion(10, "Belah ketupat pada flowchart untuk...", listOf("Pilihan", "Mulai", "Selesai", "Arah"), 0),
        QuickQuizQuestion(11, "Input/output biasanya memakai simbol...", listOf("Jajar genjang", "Oval", "Panah", "Bintang"), 0),
        QuickQuizQuestion(12, "Jika hujan, langkah tepat adalah...", listOf("Bawa payung", "Buang sepatu", "Tidur di jalan", "Lari tanpa arah"), 0),
        QuickQuizQuestion(13, "Jawaban pilihan dalam algoritma sering berupa...", listOf("IYA/TIDAK", "Merah saja", "Tidur saja", "Lompat saja"), 0),
        QuickQuizQuestion(14, "Pengulangan berarti...", listOf("Melakukan berkali-kali", "Menghapus semua", "Tidak berurutan", "Diam saja"), 0),
        QuickQuizQuestion(15, "Jika diminta lompat 3 kali, maka lompat...", listOf("1 kali", "3 kali", "10 kali", "0 kali"), 1),
        QuickQuizQuestion(16, "Urutan membuat susu yang benar dimulai dari...", listOf("Siapkan gelas", "Minum dulu", "Simpan sendok", "Tidur"), 0),
        QuickQuizQuestion(17, "Langkah terakhir menyikat gigi biasanya...", listOf("Simpan sikat gigi", "Ambil buku", "Pakai sepatu", "Membuka pintu"), 0),
        QuickQuizQuestion(18, "Flowchart yang rapi harus punya...", listOf("MULAI dan SELESAI", "Banyak panah acak", "Teks panjang sekali", "Tanpa arah"), 0),
        QuickQuizQuestion(19, "Simbol keputusan berisi...", listOf("Pertanyaan", "Nama makanan", "Warna sepatu", "Nomor rumah"), 0),
        QuickQuizQuestion(20, "Contoh pertanyaan keputusan adalah...", listOf("Apakah hujan?", "Ambil buku", "Simpan tas", "Buka pintu"), 0),
        QuickQuizQuestion(21, "Kalau langkah acak, hasil bisa...", listOf("Membingungkan", "Pasti benar", "Selalu rapi", "Tidak berubah"), 0),
        QuickQuizQuestion(22, "Kata kerja yang baik untuk langkah adalah...", listOf("Ambil", "Pokoknya", "Itu", "Sesuatu"), 0),
        QuickQuizQuestion(23, "Kalimat langkah yang jelas adalah...", listOf("Ambil sikat gigi", "Pokoknya selesai", "Benda itu", "Lakukan saja"), 0),
        QuickQuizQuestion(24, "Pseudocode biasanya diakhiri dengan...", listOf("SELESAI", "MULAI", "BINGUNG", "ACAK"), 0),
        QuickQuizQuestion(25, "Flowchart membantu kita melihat...", listOf("Alur langkah", "Rasa makanan", "Bau bunga", "Suara angin"), 0),
        QuickQuizQuestion(26, "Jika lapar, langkah tepat adalah...", listOf("Makan dulu", "Buang piring", "Tidur di jalan", "Sembunyikan sendok"), 0),
        QuickQuizQuestion(27, "Pengulangan berhenti saat...", listOf("Tugas selesai", "Baru mulai", "Belum cukup", "Tidak dihitung"), 0),
        QuickQuizQuestion(28, "Contoh pengulangan adalah...", listOf("Tepuk 3 kali", "Tidur tanpa bangun", "Gambar acak", "Hapus langkah"), 0),
        QuickQuizQuestion(29, "Sebelum mewarnai gambar, kita harus...", listOf("Membuat gambar", "Membuang kertas", "Menutup mata", "Tidur"), 0),
        QuickQuizQuestion(30, "Saat membuat teh, air dituangkan sebelum...", listOf("Diminum", "Gelas disiapkan", "Bangun tidur", "Pakai sepatu"), 0),
        QuickQuizQuestion(31, "Kotak proses pada flowchart berisi...", listOf("Langkah kerja", "Pilihan IYA/TIDAK", "Arah panah", "Judul lagu"), 0),
        QuickQuizQuestion(32, "Panah flowchart sebaiknya...", listOf("Jelas", "Acak", "Bertabrakan", "Hilang"), 0),
        QuickQuizQuestion(33, "Teks dalam simbol flowchart sebaiknya...", listOf("Singkat dan jelas", "Sangat panjang", "Kosong semua", "Tidak terbaca"), 0),
        QuickQuizQuestion(34, "Jika lampu mati, langkah tepat adalah...", listOf("Tekan saklar", "Buang lampu", "Tidur di lantai", "Sembunyi"), 0),
        QuickQuizQuestion(35, "Urutan kegiatan punya bagian...", listOf("Awal sampai akhir", "Akhir dulu saja", "Acak saja", "Tanpa langkah"), 0),
        QuickQuizQuestion(36, "Algoritma dekat dengan...", listOf("Kegiatan sehari-hari", "Hal menakutkan", "Warna saja", "Suara saja"), 0),
        QuickQuizQuestion(37, "Membuat roti selai dimulai dari...", listOf("Ambil roti", "Makan dulu", "Cuci sepatu", "Tutup buku"), 0),
        QuickQuizQuestion(38, "Robot Algo bisa bingung jika...", listOf("Langkah tidak jelas", "Langkah urut", "Flowchart rapi", "Panah jelas"), 0),
        QuickQuizQuestion(39, "Contoh output adalah...", listOf("Hasil yang ditampilkan", "Langkah acak", "Pertanyaan saja", "Tidak ada hasil"), 0),
        QuickQuizQuestion(40, "Contoh input adalah...", listOf("Data yang dimasukkan", "Jawaban akhir", "Selesai", "Tidur"), 0),
        QuickQuizQuestion(41, "Jika belum sampai 5 lompatan, Algo harus...", listOf("Lompat lagi", "Berhenti selamanya", "Tidur", "Hapus hitungan"), 0),
        QuickQuizQuestion(42, "Jika sudah selesai, pengulangan harus...", listOf("Berhenti", "Mulai lagi terus", "Acak", "Hilang"), 0),
        QuickQuizQuestion(43, "Simbol mulai dan selesai berbentuk...", listOf("Oval", "Belah ketupat", "Panah", "Jajar genjang"), 0),
        QuickQuizQuestion(44, "Simbol pilihan berbentuk...", listOf("Belah ketupat", "Oval", "Panah", "Persegi panjang"), 0),
        QuickQuizQuestion(45, "Simbol proses berbentuk...", listOf("Persegi panjang", "Oval", "Panah", "Awan"), 0),
        QuickQuizQuestion(46, "Sebelum berangkat sekolah, biasanya kita...", listOf("Bersiap-siap", "Tidur di jalan", "Membuang tas", "Makan sepatu"), 0),
        QuickQuizQuestion(47, "Langkah 'Tuang air' sebaiknya setelah...", listOf("Siapkan gelas", "Minum dulu", "Selesai", "Tidur"), 0),
        QuickQuizQuestion(48, "Jika jawaban keputusan berbeda, langkah berikutnya bisa...", listOf("Berbeda", "Selalu sama", "Hilang", "Tidak ada"), 0),
        QuickQuizQuestion(49, "Algoritma membantu tugas menjadi...", listOf("Rapi dan mudah", "Selalu kacau", "Tidak selesai", "Acak"), 0),
        QuickQuizQuestion(50, "Belajar algoritma dilakukan...", listOf("Langkah demi langkah", "Sekaligus tanpa urutan", "Dengan menutup semua", "Tanpa mencoba"), 0)
    )

    fun getRandomSequenceQuestions(): List<SequenceQuestion> {
        return sequenceQuestions.shuffled().take(SEQUENCE_QUESTION_PER_SESSION)
    }

    fun getRandomQuickQuizQuestions(): List<QuickQuizQuestion> {
        return quickQuizQuestions.shuffled().take(QUIZ_QUESTION_PER_SESSION)
    }

    fun calculateSequenceScore(correctAnswer: Int): Int {
        return correctAnswer.coerceIn(0, SEQUENCE_QUESTION_PER_SESSION) * 20
    }

    fun calculateQuickQuizScore(correctAnswer: Int): Int {
        return correctAnswer.coerceIn(0, QUIZ_QUESTION_PER_SESSION) * 10
    }
}
