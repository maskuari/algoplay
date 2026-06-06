# Langkah Melengkapi AlgoPlay

Dokumen ini menyusun pekerjaan AlgoPlay dengan urutan: fitur aplikasi dulu, Firebase database dan authentication di tahap terakhir.

## Tahap 1 - Jadikan fitur utama bisa dipakai lokal

1. Buat daftar materi berurutan.
2. Materi yang belum terbuka harus terkunci.
3. User bisa membuka materi yang sudah tersedia.
4. User bisa menandai materi sebagai selesai.
5. Materi berikutnya terbuka setelah materi sebelumnya selesai.
6. Simpan progress lokal dengan `SharedPreferences`.

Status awal yang sudah dibuat:

- Progress materi lokal.
- Unlock materi bertahap.
- Detail materi muncul di layar.
- Tombol `Tandai Selesai`.

## Tahap 2 - Buat latihan dan reward berjalan

1. Buat mode latihan: Puzzle Simbol, Urutan Langkah, Quiz Cepat, Tantangan Harian.
2. Setiap mode punya reward bintang dan score.
3. Reward hanya diberikan sekali untuk mode yang sama.
4. Progress latihan dihitung dari jumlah mode selesai.
5. Profil membaca progress lokal.

Status awal yang sudah dibuat:

- Mode latihan memberi bintang dan score lokal.
- Score dan bintang tersimpan lokal.
- Profil menampilkan progress, score, badge, dan akurasi dari data lokal.

## Tahap 3 - Rapikan UI dan navigasi

1. Pastikan klik kartu menampilkan detail, bukan hanya toast.
2. Tampilkan loading, empty state, dan error state untuk daftar data.
3. Ubah logout dari long press menjadi tombol yang jelas di halaman profil.
4. Rapikan ukuran fixed `dp` yang rawan sempit di layar kecil.
5. Pastikan teks tetap aman saat font size Android diperbesar.
6. Tambahkan feedback benar/salah untuk quiz dan puzzle.

Status awal yang sudah dibuat:

- Kartu latihan dan leaderboard sudah membuka panel detail.
- Progress card muncul di tab Materi, Latihan, dan Leaderboard.

## Tahap 4 - Buat konten belajar lebih lengkap

1. Pisahkan materi menjadi model data: `Lesson`.
2. Pisahkan quiz menjadi model data: `QuizQuestion`.
3. Tambahkan penjelasan setelah jawaban quiz.
4. Tambahkan evaluasi akhir setelah semua materi selesai.
5. Tambahkan badge khusus setelah menyelesaikan target tertentu.

## Tahap 5 - Rapikan arsitektur Android

1. Pecah `MainActivity` menjadi beberapa bagian.
2. Gunakan model data Kotlin untuk user, lesson, progress, quiz, dan leaderboard.
3. Buat repository lokal sebagai penghubung data.
4. Setelah stabil, pindahkan ke MVVM: Activity/Fragment, ViewModel, Repository.
5. Gunakan RecyclerView untuk daftar materi dan leaderboard jika data makin panjang.

## Tahap 6 - Testing dan standar release

1. Jalankan build debug.
2. Test alur login, onboarding, home, materi, latihan, leaderboard, profil.
3. Test layar kecil dan font besar.
4. Test aplikasi saat offline.
5. Siapkan icon, versi aplikasi, privacy policy, dan release build.
6. Aktifkan ProGuard/minify untuk release setelah fitur stabil.

## Tahap 7 - Firebase database dan authentication

Kerjakan tahap ini terakhir, setelah fitur lokal dan UI sudah stabil.

1. Finalkan Firebase Authentication.
2. Ganti `default_web_client_id` dengan Web Client ID dari Firebase Console.
3. Pastikan SHA-1 dan SHA-256 Android app sudah terdaftar di Firebase.
4. Tambahkan forgot password.
5. Tambahkan status onboarding agar user tidak selalu masuk onboarding.
6. Buat struktur Firestore:

```text
users/{uid}
  name
  email
  level
  stars
  totalScore
  streak
  onboardingDone
  createdAt
  updatedAt

users/{uid}/progress/{lessonId}
  status
  completedAt
  score

users/{uid}/activities/{activityId}
  completed
  stars
  score
  completedAt

lessons/{lessonId}
  title
  order
  body
  steps
  isActive

quizzes/{quizId}
  lessonId
  question
  options
  correctAnswer
  explanation

leaderboard/{uid}
  name
  totalScore
  stars
  level
  updatedAt
```

7. Buat repository Firebase untuk membaca/menulis progress.
8. Migrasikan data lokal ke Firestore saat user login.
9. Buat Firestore Security Rules.
10. Pastikan leaderboard tidak menampilkan email atau data sensitif.

## Tahap 8 - Setelah Firebase aktif

1. Ubah sumber data dari lokal ke Firestore.
2. Tetap simpan cache lokal agar aplikasi nyaman saat koneksi lambat.
3. Tambahkan sync status jika data belum terkirim.
4. Tambahkan analytics untuk melihat materi mana yang sering dibuka.
5. Tambahkan crash reporting sebelum rilis.
