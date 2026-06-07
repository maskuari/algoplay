package com.algoplay.app

import android.app.AlertDialog
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.PopupMenu
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private enum class MainTab {
        HOME, MATERI, LATIHAN, LEADERBOARD, PROFIL
    }

    private data class CardContent(
        val title: String,
        val desc: String,
        val iconRes: Int,
        val iconBgRes: Int,
        val detailTitle: String = title,
        val detailBody: String = desc,
        val detailSteps: String = "",
        val actionText: String = "Mulai",
        val targetTab: MainTab? = null,
        val rewardKey: String? = null,
        val scoreReward: Int = 0
    )

    private data class LessonContent(
        val number: Int,
        val title: String
    )

    private data class LeaderboardEntry(
        val name: String,
        val score: Int,
        val iconRes: Int,
        val iconBgRes: Int,
        val note: String,
        val isCurrentUser: Boolean = false,
        val photoUrl: String? = null
    )

    private data class ScoreRank(
        val level: Int,
        val status: String,
        val colorHex: String
    )

    private data class TrainingModeStat(
        val key: String,
        val title: String,
        val shortLabel: String,
        val maxScore: Int,
        val bestScore: Int,
        val averageScore: Int,
        val accuracy: Int,
        val colorRes: Int
    )

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var progressStore: SharedPreferences

    private lateinit var mainRoot: FrameLayout
    private lateinit var mainScroll: ScrollView
    private lateinit var topHeader: LinearLayout
    private lateinit var heroCard: FrameLayout
    private lateinit var mascotTalkCard: LinearLayout
    private lateinit var homeDashboard: LinearLayout
    private lateinit var materiProgressTop: LinearLayout
    private lateinit var materiHeroCard: LinearLayout
    private lateinit var materiInfoCard: LinearLayout
    private lateinit var materiMascotCard: LinearLayout
    private lateinit var lessonList: LinearLayout
    private lateinit var cardGrid: GridLayout
    private lateinit var detailCard: LinearLayout
    private lateinit var progressCard: LinearLayout
    private lateinit var profileDashboard: LinearLayout
    private lateinit var leaderboardDashboard: LinearLayout
    private lateinit var leaderboardPodiumRow: LinearLayout
    private lateinit var leaderboardList: LinearLayout
    private lateinit var txtWelcome: TextView
    private lateinit var txtSubtitle: TextView
    private lateinit var txtHeroTitle: TextView
    private lateinit var txtHeroDesc: TextView
    private lateinit var txtHeroChip: TextView
    private lateinit var txtMascotTitle: TextView
    private lateinit var txtMascotSpeech: TextView
    private lateinit var txtSectionTitle: TextView
    private lateinit var txtDetailEyebrow: TextView
    private lateinit var txtDetailTitle: TextView
    private lateinit var txtDetailBody: TextView
    private lateinit var txtDetailSteps: TextView
    private lateinit var txtDetailAction: TextView
    private lateinit var txtProgressTitle: TextView
    private lateinit var txtProgressPercent: TextView
    private lateinit var statsRow: LinearLayout
    private lateinit var txtStatLevel: TextView
    private lateinit var txtStatActivities: TextView
    private lateinit var txtStatMission: TextView
    private lateinit var txtProfileGreeting: TextView
    private lateinit var txtProfileProgressCaption: TextView
    private lateinit var txtProfileProgressPercent: TextView
    private lateinit var profileMaterialProgress: ProgressBar
    private lateinit var txtAccuracy: TextView
    private lateinit var txtRewardScore: TextView
    private lateinit var txtStreakBadge: TextView
    private lateinit var txtProfileAverageScore: TextView
    private lateinit var profilePhotoButton: View
    private lateinit var imgProfilePhoto: ImageView
    private lateinit var txtProfileRankBadge: TextView
    private lateinit var imgProfileLeaderboardBadge: ImageView
    private lateinit var imgProfileMaterialBadge: ImageView
    private lateinit var imgProfileLevelBadge: ImageView
    private lateinit var txtProfileModes: TextView
    private lateinit var txtProfileLessons: TextView
    private lateinit var txtProfileActivities: TextView
    private lateinit var txtProfileBadge: TextView
    private lateinit var txtProfileRankStatus: TextView
    private lateinit var profileStreakWeek: LinearLayout
    private lateinit var profileScoreChart: LinearLayout
    private lateinit var profileTrainingStatsList: LinearLayout
    private lateinit var homeContinueCard: View
    private lateinit var txtHomeContinueTitle: TextView
    private lateinit var txtHomeContinueCaption: TextView
    private lateinit var homeContinueProgress: ProgressBar
    private lateinit var homeChallengeCard: View
    private lateinit var txtHomeChallengeCaption: TextView
    private lateinit var btnHomeContinue: TextView
    private lateinit var btnHomeChallenge: TextView
    private lateinit var homeActivityMateri: View
    private lateinit var homeActivityFlowchart: View
    private lateinit var homeActivityGames: View
    private lateinit var homeActivityLeaderboard: View
    private lateinit var imgHeroIcon: ImageView
    private lateinit var imgTopHeader: ImageView
    private lateinit var imgMascotTalk: ImageView
    private lateinit var progressLearning: ProgressBar
    private lateinit var homeProfileMenuButton: View
    private lateinit var homeProfileAvatarWrap: View
    private lateinit var imgHomeProfilePhoto: ImageView
    private lateinit var txtHomeUserName: TextView
    private lateinit var txtHomeDay: TextView
    private lateinit var txtHomeDateTime: TextView
    private lateinit var homeRankBadge: LinearLayout
    private lateinit var txtHomeRankLevel: TextView
    private lateinit var txtHomeRankStatus: TextView
    private lateinit var txtHomeAccuracy: TextView
    private lateinit var txtHomeStreak: TextView
    private lateinit var txtHomeLeaderboardRank: TextView
    private lateinit var materiTopProgress: ProgressBar
    private lateinit var txtMateriTopPercent: TextView

    private lateinit var cardViews: List<View>
    private lateinit var iconWraps: List<FrameLayout>
    private lateinit var cardIcons: List<ImageView>
    private lateinit var cardTitles: List<TextView>
    private lateinit var cardDescs: List<TextView>

    private lateinit var navItems: Map<MainTab, LinearLayout>
    private lateinit var navIcons: Map<MainTab, ImageView>
    private lateinit var navTexts: Map<MainTab, TextView>

    private var currentTab = MainTab.HOME
    private var currentCards: List<CardContent> = emptyList()
    private var activeCard: CardContent? = null
    private var userName = "Teman"
    private var userLevel = 1
    private var userPhotoUrl: String? = null
    private var userLocalPhotoUri: Uri? = null
    private var isGuestMode = false
    private var lastContinueType: String? = null
    private var lastContinueKey: String? = null
    private var finalExamScore = 0
    private val completedLessons = mutableSetOf<Int>()
    private val completedActivities = mutableSetOf<String>()

    private val lessonLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val lessonNumber = result.data?.getIntExtra(LessonOneActivity.EXTRA_COMPLETED_LESSON, 0) ?: 0
            val examScore = result.data?.getIntExtra(LessonTwelveActivity.EXTRA_EXAM_SCORE, 0) ?: 0
            if (lessonNumber > 0) {
                if (lessonNumber == FINAL_EXAM_LESSON_NUMBER && examScore > 0) {
                    markFinalExamCompletedFromPage(examScore)
                } else {
                    markLessonCompletedFromPage(lessonNumber)
                }
            }
        }
    }

    private val trainingLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val mode = result.data?.getStringExtra(PuzzleSymbolActivity.EXTRA_TRAINING_MODE).orEmpty()
            val score = result.data?.getIntExtra(PuzzleSymbolActivity.EXTRA_SESSION_SCORE, 0) ?: 0
            val correct = result.data?.getIntExtra(PuzzleSymbolActivity.EXTRA_CORRECT_ANSWER, 0) ?: 0
            if (mode.isNotBlank()) {
                if (!isGuestMode) {
                    completedActivities.add(mode)
                }
                saveContinueTarget(CONTINUE_LATIHAN, mode)
                updateContent(MainTab.LATIHAN)
                Toast.makeText(this, "Latihan selesai: $correct benar, skor $score.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val profilePhotoPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { saveProfilePhoto(it) }
    }

    private val clockHandler = Handler(Looper.getMainLooper())
    private val homeClockTicker = object : Runnable {
        override fun run() {
            if (::txtHomeDay.isInitialized) {
                updateHomeStatusBar()
            }
            clockHandler.postDelayed(this, CLOCK_REFRESH_MS)
        }
    }

    private val lessons = listOf(
        LessonContent(
            1,
            "Yuk Kenalan dengan Algoritma"
        ),
        LessonContent(
            2,
            "Algoritma Itu Seperti Langkah-langkah"
        ),
        LessonContent(
            3,
            "Belajar Urutan Kegiatan"
        ),
        LessonContent(
            4,
            "Menulis Langkah dengan Kata-kata Sendiri"
        ),
        LessonContent(
            5,
            "Mengenal Pseudocode dengan Mudah"
        ),
        LessonContent(
            6,
            "Yuk Kenalan dengan Flowchart"
        ),
        LessonContent(
            7,
            "Mengenal Simbol-simbol Flowchart"
        ),
        LessonContent(
            8,
            "Cara Membuat Flowchart yang Rapi"
        ),
        LessonContent(
            9,
            "Belajar Pilihan: Jika Iya atau Tidak"
        ),
        LessonContent(
            10,
            "Belajar Pengulangan: Lakukan Lagi"
        ),
        LessonContent(
            11,
            "Kesimpulan: Apa yang Sudah Kita Pelajari"
        ),
        LessonContent(
            12,
            "Ujian Seru / Kuis Akhir Materi"
        )
    )

    private val displayLevel: Int
        get() = maxOf(userLevel, scoreRank(totalScore).level)

    private val lessonProgress: Int
        get() = if (lessons.isEmpty()) 0 else (completedLessons.size * 100) / lessons.size

    private val trainingProgress: Int
        get() = (completedActivities.size * 25).coerceAtMost(100)

    private val trainingTotalScore: Int
        get() = trainingModeStats().sumOf { it.bestScore }

    private val materialScore: Int
        get() = completedLessons.count { it != FINAL_EXAM_LESSON_NUMBER } * LESSON_SCORE_REWARD

    private val totalScore: Int
        get() = materialScore + trainingTotalScore + finalExamScore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        isGuestMode = intent.getBooleanExtra(LoginActivity.EXTRA_GUEST_MODE, false)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        progressStore = getSharedPreferences("algoplay_local_progress", MODE_PRIVATE)

        bindViews()
        loadLocalProgress()
        setupNavigation()
        setupCardActions()
        loadUserData()
    }

    override fun onResume() {
        super.onResume()
        clockHandler.post(homeClockTicker)
    }

    override fun onPause() {
        clockHandler.removeCallbacks(homeClockTicker)
        super.onPause()
    }

    private fun bindViews() {
        mainRoot = findViewById(R.id.mainRoot)
        mainScroll = findViewById(R.id.mainScroll)
        topHeader = findViewById(R.id.topHeader)
        heroCard = findViewById(R.id.heroCard)
        mascotTalkCard = findViewById(R.id.mascotTalkCard)
        homeDashboard = findViewById(R.id.homeDashboard)
        materiProgressTop = findViewById(R.id.materiProgressTop)
        materiHeroCard = findViewById(R.id.materiHeroCard)
        materiInfoCard = findViewById(R.id.materiInfoCard)
        materiMascotCard = findViewById(R.id.materiMascotCard)
        lessonList = findViewById(R.id.lessonList)
        cardGrid = findViewById(R.id.cardGrid)
        detailCard = findViewById(R.id.detailCard)
        progressCard = findViewById(R.id.progressCard)
        profileDashboard = findViewById(R.id.profileDashboard)
        leaderboardDashboard = findViewById(R.id.leaderboardDashboard)
        leaderboardPodiumRow = findViewById(R.id.leaderboardPodiumRow)
        leaderboardList = findViewById(R.id.leaderboardList)
        txtWelcome = findViewById(R.id.txtWelcome)
        txtSubtitle = findViewById(R.id.txtSubtitle)
        txtHeroTitle = findViewById(R.id.txtHeroTitle)
        txtHeroDesc = findViewById(R.id.txtHeroDesc)
        txtHeroChip = findViewById(R.id.txtHeroChip)
        txtMascotTitle = findViewById(R.id.txtMascotTitle)
        txtMascotSpeech = findViewById(R.id.txtMascotSpeech)
        txtSectionTitle = findViewById(R.id.txtSectionTitle)
        txtDetailEyebrow = findViewById(R.id.txtDetailEyebrow)
        txtDetailTitle = findViewById(R.id.txtDetailTitle)
        txtDetailBody = findViewById(R.id.txtDetailBody)
        txtDetailSteps = findViewById(R.id.txtDetailSteps)
        txtDetailAction = findViewById(R.id.txtDetailAction)
        txtProgressTitle = findViewById(R.id.txtProgressTitle)
        txtProgressPercent = findViewById(R.id.txtProgressPercent)
        statsRow = findViewById(R.id.statsRow)
        txtStatLevel = findViewById(R.id.txtStatLevel)
        txtStatActivities = findViewById(R.id.txtStatActivities)
        txtStatMission = findViewById(R.id.txtStatMission)
        txtProfileGreeting = findViewById(R.id.txtProfileGreeting)
        txtProfileProgressCaption = findViewById(R.id.txtProfileProgressCaption)
        txtProfileProgressPercent = findViewById(R.id.txtProfileProgressPercent)
        profileMaterialProgress = findViewById(R.id.profileMaterialProgress)
        txtAccuracy = findViewById(R.id.txtAccuracy)
        txtRewardScore = findViewById(R.id.txtRewardScore)
        txtStreakBadge = findViewById(R.id.txtStreakBadge)
        txtProfileAverageScore = findViewById(R.id.txtProfileAverageScore)
        profilePhotoButton = findViewById(R.id.profilePhotoButton)
        imgProfilePhoto = findViewById(R.id.imgProfilePhoto)
        txtProfileRankBadge = findViewById(R.id.txtProfileRankBadge)
        imgProfileLeaderboardBadge = findViewById(R.id.imgProfileLeaderboardBadge)
        imgProfileMaterialBadge = findViewById(R.id.imgProfileMaterialBadge)
        imgProfileLevelBadge = findViewById(R.id.imgProfileLevelBadge)
        txtProfileModes = findViewById(R.id.txtProfileModes)
        txtProfileLessons = findViewById(R.id.txtProfileLessons)
        txtProfileActivities = findViewById(R.id.txtProfileActivities)
        txtProfileBadge = findViewById(R.id.txtProfileBadge)
        txtProfileRankStatus = findViewById(R.id.txtProfileRankStatus)
        profileStreakWeek = findViewById(R.id.profileStreakWeek)
        profileScoreChart = findViewById(R.id.profileScoreChart)
        profileTrainingStatsList = findViewById(R.id.profileTrainingStatsList)
        homeContinueCard = findViewById(R.id.homeContinueCard)
        txtHomeContinueTitle = findViewById(R.id.txtHomeContinueTitle)
        txtHomeContinueCaption = findViewById(R.id.txtHomeContinueCaption)
        homeContinueProgress = findViewById(R.id.homeContinueProgress)
        homeChallengeCard = findViewById(R.id.homeChallengeCard)
        txtHomeChallengeCaption = findViewById(R.id.txtHomeChallengeCaption)
        btnHomeContinue = findViewById(R.id.btnHomeContinue)
        btnHomeChallenge = findViewById(R.id.btnHomeChallenge)
        homeActivityMateri = findViewById(R.id.homeActivityMateri)
        homeActivityFlowchart = findViewById(R.id.homeActivityFlowchart)
        homeActivityGames = findViewById(R.id.homeActivityGames)
        homeActivityLeaderboard = findViewById(R.id.homeActivityLeaderboard)
        imgHeroIcon = findViewById(R.id.imgHeroIcon)
        imgTopHeader = findViewById(R.id.imgTopHeader)
        imgMascotTalk = findViewById(R.id.imgMascotTalk)
        progressLearning = findViewById(R.id.progressLearning)
        homeProfileMenuButton = findViewById(R.id.homeProfileMenuButton)
        homeProfileAvatarWrap = findViewById(R.id.homeProfileAvatarWrap)
        imgHomeProfilePhoto = findViewById(R.id.imgHomeProfilePhoto)
        txtHomeUserName = findViewById(R.id.txtHomeUserName)
        txtHomeDay = findViewById(R.id.txtHomeDay)
        txtHomeDateTime = findViewById(R.id.txtHomeDateTime)
        homeRankBadge = findViewById(R.id.homeRankBadge)
        txtHomeRankLevel = findViewById(R.id.txtHomeRankLevel)
        txtHomeRankStatus = findViewById(R.id.txtHomeRankStatus)
        txtHomeAccuracy = findViewById(R.id.txtHomeAccuracy)
        txtHomeStreak = findViewById(R.id.txtHomeStreak)
        txtHomeLeaderboardRank = findViewById(R.id.txtHomeLeaderboardRank)
        materiTopProgress = findViewById(R.id.materiTopProgress)
        txtMateriTopPercent = findViewById(R.id.txtMateriTopPercent)

        cardViews = listOf(
            findViewById(R.id.cardOne),
            findViewById(R.id.cardTwo),
            findViewById(R.id.cardThree),
            findViewById(R.id.cardFour)
        )
        iconWraps = listOf(
            findViewById(R.id.iconWrapOne),
            findViewById(R.id.iconWrapTwo),
            findViewById(R.id.iconWrapThree),
            findViewById(R.id.iconWrapFour)
        )
        cardIcons = listOf(
            findViewById(R.id.imgCardOne),
            findViewById(R.id.imgCardTwo),
            findViewById(R.id.imgCardThree),
            findViewById(R.id.imgCardFour)
        )
        cardTitles = listOf(
            findViewById(R.id.txtCardOneTitle),
            findViewById(R.id.txtCardTwoTitle),
            findViewById(R.id.txtCardThreeTitle),
            findViewById(R.id.txtCardFourTitle)
        )
        cardDescs = listOf(
            findViewById(R.id.txtCardOneDesc),
            findViewById(R.id.txtCardTwoDesc),
            findViewById(R.id.txtCardThreeDesc),
            findViewById(R.id.txtCardFourDesc)
        )

        navItems = mapOf(
            MainTab.HOME to findViewById(R.id.navHome),
            MainTab.MATERI to findViewById(R.id.navMateri),
            MainTab.LATIHAN to findViewById(R.id.navLatihan),
            MainTab.LEADERBOARD to findViewById(R.id.navLeaderboard),
            MainTab.PROFIL to findViewById(R.id.navProfil)
        )
        navIcons = mapOf(
            MainTab.HOME to findViewById(R.id.imgNavHome),
            MainTab.MATERI to findViewById(R.id.imgNavMateri),
            MainTab.LATIHAN to findViewById(R.id.imgNavLatihan),
            MainTab.LEADERBOARD to findViewById(R.id.imgNavLeaderboard),
            MainTab.PROFIL to findViewById(R.id.imgNavProfil)
        )
        navTexts = mapOf(
            MainTab.HOME to findViewById(R.id.txtNavHome),
            MainTab.MATERI to findViewById(R.id.txtNavMateri),
            MainTab.LATIHAN to findViewById(R.id.txtNavLatihan),
            MainTab.LEADERBOARD to findViewById(R.id.txtNavLeaderboard),
            MainTab.PROFIL to findViewById(R.id.txtNavProfil)
        )
    }

    private fun setupNavigation() {
        navItems[MainTab.HOME]?.setOnClickListener { selectTab(MainTab.HOME) }
        navItems[MainTab.MATERI]?.setOnClickListener { selectTab(MainTab.MATERI) }
        navItems[MainTab.LATIHAN]?.setOnClickListener { selectTab(MainTab.LATIHAN) }
        navItems[MainTab.LEADERBOARD]?.setOnClickListener { selectTab(MainTab.LEADERBOARD) }
        navItems[MainTab.PROFIL]?.setOnClickListener { selectTab(MainTab.PROFIL) }

        navItems[MainTab.PROFIL]?.setOnLongClickListener {
            openAccountMenu(navItems[MainTab.PROFIL] ?: bottomNavFallback())
            true
        }
    }

    private fun setupCardActions() {
        btnHomeContinue.setOnClickListener { continueLearning() }
        btnHomeChallenge.setOnClickListener { openChallengeFromHome() }
        homeChallengeCard.setOnClickListener { openChallengeFromHome() }
        homeProfileMenuButton.setOnClickListener { openAccountMenu(homeProfileMenuButton) }
        profilePhotoButton.setOnClickListener {
            if (isGuestMode) {
                showLoginRequiredDialog("Login untuk mengatur foto profil.")
            } else {
                profilePhotoPicker.launch(arrayOf("image/*"))
            }
        }
        homeActivityMateri.setOnClickListener { selectTab(MainTab.MATERI) }
        homeActivityFlowchart.setOnClickListener { showFlowchartDialog() }
        homeActivityGames.setOnClickListener { selectTab(MainTab.LATIHAN) }
        homeActivityLeaderboard.setOnClickListener { selectTab(MainTab.LEADERBOARD) }

        cardViews.forEachIndexed { index, card ->
            card.setOnClickListener {
                when (currentTab) {
                    MainTab.HOME -> {
                        when (index) {
                            0 -> selectTab(MainTab.MATERI)
                            3 -> selectTab(MainTab.PROFIL)
                            else -> selectTab(MainTab.LATIHAN)
                        }
                    }
                    MainTab.MATERI -> showCardDetail(index)
                    MainTab.LATIHAN -> openTrainingMode(index)
                    MainTab.LEADERBOARD -> showCardDetail(index)
                    MainTab.PROFIL -> {
                        if (index == 3) {
                            Toast.makeText(this, "Tahan tombol Profil di navbar untuk logout", Toast.LENGTH_SHORT).show()
                        } else {
                            showCardDetail(index)
                        }
                    }
                }
            }
        }

        txtDetailAction.setOnClickListener {
            handleDetailAction()
        }
    }

    private fun openAccountMenu(anchor: View) {
        PopupMenu(this, anchor).apply {
            menu.add("Logout")
            menu.add("Ganti akun")
            setOnMenuItemClickListener { item ->
                when (item.title.toString()) {
                    "Logout" -> {
                        goToLogin()
                        true
                    }
                    "Ganti akun" -> {
                        goToLogin()
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun goToLogin() {
        if (!isGuestMode) {
            auth.signOut()
        }
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showLoginRequiredDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Login dulu")
            .setMessage(message)
            .setPositiveButton("Login") { _, _ -> goToLogin() }
            .setNegativeButton("Nanti", null)
            .show()
    }

    private fun showFlowchartDialog() {
        val dialog = Dialog(this)
        val root = FrameLayout(this).apply {
            setPadding(dp(18), 0, dp(18), 0)
        }
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = roundedDrawable(ContextCompat.getColor(this@MainActivity, R.color.white), dp(28))
            elevation = dp(10).toFloat()
            setPadding(dp(16), dp(16), dp(16), dp(14))
            alpha = 0f
            scaleX = 0.94f
            scaleY = 0.94f
        }
        root.addView(card, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        ))

        val hero = FrameLayout(this).apply {
            background = gradientDrawable(
                ContextCompat.getColor(this@MainActivity, R.color.algoplay_blue_soft),
                ContextCompat.getColor(this@MainActivity, R.color.white),
                dp(22)
            )
            setPadding(dp(16), dp(14), dp(12), dp(14))
        }

        val titleGroup = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, dp(76), 0)
        }
        titleGroup.addView(createCompactText("Algoritma & Flowchart", 21, R.color.algoplay_text, true, Gravity.START))
        titleGroup.addView(createCompactText(
            "Lihat alur langkah dengan cara yang lebih visual.",
            13,
            R.color.algoplay_subtext,
            false,
            Gravity.START
        ), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(6)
        })
        hero.addView(titleGroup, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL))

        val heroImage = ImageView(this).apply {
            setImageResource(R.drawable.flowchart_home)
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
        }
        hero.addView(heroImage, FrameLayout.LayoutParams(dp(74), dp(74), Gravity.END or Gravity.CENTER_VERTICAL))
        card.addView(hero, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(104)))

        card.addView(createCompactText(
            "Algoritma adalah urutan langkah untuk menyelesaikan masalah. Flowchart menggambar langkah itu dengan simbol dan panah supaya alurnya mudah dibaca.",
            14,
            R.color.algoplay_text,
            false,
            Gravity.START
        ).apply {
            setLineSpacing(dp(2).toFloat(), 1f)
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(14)
        })

        val blocks = listOf(
            createDialogStep("1", "Mulai", "Tentukan titik awal masalah.", R.color.algoplay_blue_dark),
            createDialogStep("2", "Proses", "Susun langkah secara berurutan.", R.color.algoplay_green_dark),
            createDialogStep("3", "Selesai", "Akhiri ketika tujuan sudah tercapai.", R.color.algoplay_gold_dark)
        )
        blocks.forEach { block ->
            block.apply {
                alpha = 0f
                translationY = dp(14).toFloat()
            }
            card.addView(block, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(66)).apply {
                topMargin = dp(9)
            })
        }

        val actionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val closeButton = createCompactText("Tutup", 14, R.color.algoplay_subtext, true, Gravity.CENTER).apply {
            background = roundedStrokeDrawable(
                ContextCompat.getColor(this@MainActivity, R.color.white),
                ContextCompat.getColor(this@MainActivity, R.color.algoplay_blue_soft),
                dp(16)
            )
            setOnClickListener { dialog.dismiss() }
        }
        val openButton = createCompactText("Buka Materi", 14, R.color.white, true, Gravity.CENTER).apply {
            background = roundedDrawable(ContextCompat.getColor(this@MainActivity, R.color.algoplay_blue_dark), dp(16))
            setOnClickListener {
                dialog.dismiss()
                selectTab(MainTab.MATERI)
            }
        }
        actionRow.addView(closeButton, LinearLayout.LayoutParams(0, dp(48), 1f).apply {
            marginEnd = dp(8)
        })
        actionRow.addView(openButton, LinearLayout.LayoutParams(0, dp(48), 1f).apply {
            marginStart = dp(8)
        })
        card.addView(actionRow, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(16)
        })

        dialog.setContentView(root)
        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setDimAmount(0.42f)
            dialog.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            card.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(260)
                .setInterpolator(OvershootInterpolator(1.05f))
                .start()
            heroImage.animate()
                .rotationBy(4f)
                .setDuration(380)
                .withEndAction {
                    heroImage.animate().rotation(0f).setDuration(260).start()
                }
                .start()
            blocks.forEachIndexed { index, block ->
                block.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay((120 + index * 120).toLong())
                    .setDuration(280)
                    .setInterpolator(OvershootInterpolator(0.9f))
                    .start()
            }
        }
        dialog.show()
    }

    private fun createDialogStep(number: String, title: String, body: String, colorRes: Int): LinearLayout {
        val color = ContextCompat.getColor(this, colorRes)
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = roundedStrokeDrawable(
                ContextCompat.getColor(this@MainActivity, R.color.white),
                ContextCompat.getColor(this@MainActivity, R.color.algoplay_blue_soft),
                dp(18)
            )
            setPadding(dp(12), dp(8), dp(12), dp(8))

            addView(createCompactText(number, 15, R.color.white, true, Gravity.CENTER).apply {
                background = roundedDrawable(color, dp(15))
            }, LinearLayout.LayoutParams(dp(36), dp(36)))

            val textGroup = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
            }
            textGroup.addView(createCompactText(title, 15, R.color.algoplay_text, true, Gravity.START))
            textGroup.addView(createCompactText(body, 12, R.color.algoplay_subtext, false, Gravity.START).apply {
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = dp(4)
            })
            addView(textGroup, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(12)
            })
        }
    }

    private fun bottomNavFallback(): View = mainRoot

    private fun loadUserData() {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            if (isGuestMode) {
                userName = "Guest"
                userPhotoUrl = null
                userLocalPhotoUri = null
                selectTab(MainTab.HOME, shouldScroll = false)
            } else {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            return
        }

        userName = currentUser.displayName ?: "Teman"
        userPhotoUrl = currentUser.photoUrl?.toString()
        selectTab(MainTab.HOME, shouldScroll = false)

        firestore.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    userName = document.getString("name") ?: userName
                    userPhotoUrl = document.getString("photoUrl")
                        ?: document.getString("photoURL")
                        ?: document.getString("profileImage")
                        ?: document.getString("profilePhoto")
                        ?: userPhotoUrl
                    userLevel = (document.getLong("level") ?: 1L).toInt()
                }
                selectTab(currentTab, shouldScroll = false)
            }
            .addOnFailureListener {
                selectTab(currentTab, shouldScroll = false)
            }
    }

    private fun loadLocalProgress() {
        completedLessons.clear()
        completedActivities.clear()
        finalExamScore = 0
        if (isGuestMode) {
            userLocalPhotoUri = null
            lastContinueType = null
            lastContinueKey = null
            return
        }

        completedLessons.addAll(
            progressStore.getString(KEY_COMPLETED_LESSONS, "")
                .orEmpty()
                .split(",")
                .mapNotNull { it.toIntOrNull() }
        )
        completedActivities.addAll(
            progressStore.getString(KEY_COMPLETED_ACTIVITIES, "")
                .orEmpty()
                .split(",")
                .filter { it.isNotBlank() }
        )
        finalExamScore = progressStore.getInt(KEY_FINAL_EXAM_SCORE, 0)
        userLocalPhotoUri = progressStore.getString(KEY_PROFILE_PHOTO_URI, null)
            ?.let { Uri.parse(it) }
        lastContinueType = progressStore.getString(KEY_LAST_CONTINUE_TYPE, null)
        lastContinueKey = progressStore.getString(KEY_LAST_CONTINUE_KEY, null)
    }

    private fun saveLocalProgress() {
        if (isGuestMode) return
        progressStore.edit()
            .putString(KEY_COMPLETED_LESSONS, completedLessons.sorted().joinToString(","))
            .putString(KEY_COMPLETED_ACTIVITIES, completedActivities.sorted().joinToString(","))
            .putInt(KEY_FINAL_EXAM_SCORE, finalExamScore)
            .putString(KEY_LAST_CONTINUE_TYPE, lastContinueType)
            .putString(KEY_LAST_CONTINUE_KEY, lastContinueKey)
            .apply()
    }

    private fun saveProfilePhoto(uri: Uri) {
        runCatching {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        userLocalPhotoUri = uri
        progressStore.edit()
            .putString(KEY_PROFILE_PHOTO_URI, uri.toString())
            .apply()
        updateProfilePhoto()
        updateHomeProfilePhoto()
        Toast.makeText(this, "Foto profil diperbarui", Toast.LENGTH_SHORT).show()
    }

    private fun selectTab(tab: MainTab, shouldScroll: Boolean = true) {
        if (isGuestMode && (tab == MainTab.LEADERBOARD || tab == MainTab.PROFIL)) {
            showLoginRequiredDialog(
                if (tab == MainTab.LEADERBOARD) {
                    "Login untuk membuka leaderboard dan menyimpan score."
                } else {
                    "Login untuk membuka profil dan menyimpan progress."
                }
            )
            return
        }
        currentTab = tab
        activeCard = null
        updatePageBackground(tab)
        updateNavState(tab)
        updateContent(tab)

        if (shouldScroll) {
            mainScroll.post { mainScroll.smoothScrollTo(0, 0) }
        }
    }

    private fun updateNavState(activeTab: MainTab) {
        MainTab.values().forEach { tab ->
            val isActive = tab == activeTab
            val activeColor = tab.activeColor()
            val inactiveColor = ContextCompat.getColor(this, R.color.algoplay_subtext)

            navItems[tab]?.setBackgroundResource(if (isActive) R.drawable.bg_nav_active else 0)
            navIcons[tab]?.apply {
                clearColorFilter()
                alpha = if (isActive) 1f else 0.68f
                scaleX = if (isActive) 1.08f else 0.94f
                scaleY = if (isActive) 1.08f else 0.94f
            }
            navTexts[tab]?.apply {
                setTextColor(if (isActive) activeColor else inactiveColor)
                setTypeface(null, if (isActive) Typeface.BOLD else Typeface.NORMAL)
            }
        }
    }

    private fun updateContent(tab: MainTab) {
        updateSectionVisibility(tab)
        updateHomeStatusBar()

        val cards: List<CardContent>
        val progress: Int

        when (tab) {
            MainTab.HOME -> {
                txtWelcome.text = "Halo, $userName!"
                txtSubtitle.text = "Siap lanjut main sambil belajar?"
                txtHeroTitle.text = "Petualangan Hari Ini"
                txtHeroDesc.text = "Materi selesai $lessonProgress%, latihan selesai $trainingProgress%, dan score latihanmu $totalScore."
                txtHeroChip.text = "Mulai Sekarang"
                txtMascotTitle.text = "Albi bilang"
                txtMascotSpeech.text = "Hari ini cukup mulai dari satu misi kecil. Kalau login, progress dan score kamu tersimpan."
                txtSectionTitle.text = "Pilihan Cepat"
                txtProgressTitle.text = "Ritme Belajar Minggu Ini"
                statsRow.visibility = View.GONE
                txtStatLevel.text = displayLevel.toString()
                txtStatActivities.text = completedActivities.size.toString()
                txtStatMission.text = "2"
                imgHeroIcon.setImageResource(R.drawable.ic_star)
                cards = listOf(
                    CardContent(
                        "Lanjut Materi",
                        "Mulai dari dasar",
                        R.drawable.ic_code,
                        R.drawable.bg_icon_blue,
                        "Lanjut Materi Algoritma",
                        "Pelajari urutan langkah sederhana sebelum masuk ke flowchart.",
                        "1. Baca contoh pendek\n2. Lihat urutan langkah\n3. Tekan Latihan Cepat",
                        "Buka Materi",
                        targetTab = MainTab.MATERI
                    ),
                    CardContent(
                        "Latihan Cepat",
                        "Susun flowchart",
                        R.drawable.ic_flow,
                        R.drawable.bg_icon_green,
                        "Latihan Cepat",
                        "Coba susun simbol flowchart dari mulai sampai selesai.",
                        "1. Pilih simbol\n2. Cocokkan urutan\n3. Cek jawaban",
                        "Mulai Latihan",
                        targetTab = MainTab.LATIHAN
                    ),
                    CardContent(
                        "Kuis Harian",
                        "Jawab 3 soal",
                        R.drawable.ic_quiz,
                        R.drawable.bg_icon_purple,
                        "Kuis Harian",
                        "Jawab soal singkat untuk menguji ingatanmu hari ini.",
                        "1. Baca pertanyaan\n2. Pilih jawaban\n3. Dapatkan score",
                        "Buka Kuis",
                        targetTab = MainTab.LATIHAN
                    ),
                    CardContent(
                        "Peringkat",
                        "Lihat score",
                        R.drawable.ic_star,
                        R.drawable.bg_icon_yellow,
                        "Status Leaderboard",
                        "Leaderboard memakai total score dari semua mode latihan.",
                        "Rank: ${homeLeaderboardRankText()}\nScore: $totalScore\nStatus: ${scoreRank(totalScore).status}",
                        "Lihat Leaderboard",
                        targetTab = MainTab.LEADERBOARD
                    )
                )
                updateHomeDashboardState()
                progress = ((lessonProgress + trainingProgress) / 2).coerceAtLeast(8)
            }

            MainTab.MATERI -> {
                txtWelcome.text = "Materi Belajar"
                txtSubtitle.text = "Buka materi berurutan dari awal sampai ujian akhir."
                txtHeroTitle.text = "Jalur Belajar"
                txtHeroDesc.text = "Selesaikan satu materi untuk membuka materi berikutnya."
                txtHeroChip.text = "${completedLessons.size}/${lessons.size} Selesai"
                txtMascotTitle.text = "Albi menjelaskan"
                txtMascotSpeech.text = "Aku susun materinya seperti tangga. Selesai satu, naik ke langkah berikutnya."
                txtSectionTitle.text = "Daftar Materi"
                txtProgressTitle.text = "Materi selesai"
                statsRow.visibility = View.GONE
                imgHeroIcon.setImageResource(R.drawable.ic_code)
                buildLessonList()
                cards = listOf(
                    CardContent(
                        "Algoritma",
                        "Urutan langkah",
                        R.drawable.ic_code,
                        R.drawable.bg_icon_blue,
                        "Apa itu algoritma?",
                        "Algoritma adalah urutan langkah untuk menyelesaikan masalah. Seperti resep, langkahnya harus jelas dan berurutan.",
                        "Contoh membuat minum:\n1. Siapkan gelas\n2. Tuang air\n3. Aduk\n4. Minum",
                        "Tandai Paham"
                    ),
                    CardContent(
                        "Contoh Harian",
                        "Langkah sederhana",
                        R.drawable.ic_home,
                        R.drawable.bg_icon_yellow,
                        "Algoritma di rumah",
                        "Banyak kegiatan sehari-hari punya algoritma, misalnya cuci tangan atau berangkat sekolah.",
                        "Cuci tangan:\n1. Basahi tangan\n2. Pakai sabun\n3. Gosok tangan\n4. Bilas\n5. Keringkan",
                        "Coba Contoh"
                    ),
                    CardContent(
                        "Flowchart",
                        "Gambar langkah",
                        R.drawable.ic_flow,
                        R.drawable.bg_icon_green,
                        "Apa itu flowchart?",
                        "Flowchart adalah gambar alur. Algoritma yang tadinya berupa teks bisa digambar dengan simbol dan panah.",
                        "Mulai -> Proses -> Keputusan -> Selesai\nPanah menunjukkan arah langkah.",
                        "Lihat Simbol"
                    ),
                    CardContent(
                        "Simbol Penting",
                        "Oval dan proses",
                        R.drawable.ic_diamond,
                        R.drawable.bg_icon_purple,
                        "Simbol flowchart",
                        "Setiap bentuk punya arti. Oval untuk mulai/selesai, kotak untuk proses, belah ketupat untuk pilihan.",
                        "Oval: Mulai/Selesai\nKotak: Proses\nBelah ketupat: Keputusan\nPanah: Arah",
                        "Latih Simbol"
                    )
                )
                progress = lessonProgress
            }

            MainTab.LATIHAN -> {
                txtWelcome.text = "Latihan Seru"
                txtSubtitle.text = "Asah logika lewat puzzle dan flowchart."
                imgTopHeader.setImageResource(R.drawable.hai_latihan)
                imgMascotTalk.setImageResource(R.drawable.menantang_latihan)
                txtMascotTitle.text = "Albi menantang"
                txtMascotSpeech.text = "Pilih satu game dulu. Menang challenge akan kasih special score!"
                txtSectionTitle.text = "Mode Latihan"
                txtProgressTitle.text = "Progress latihan"
                statsRow.visibility = View.GONE
                imgHeroIcon.setImageResource(R.drawable.ic_flow)
                cards = listOf(
                    CardContent(
                        "Puzzle Simbol",
                        "Cocokkan bentuk",
                        R.drawable.puzzle_latihan,
                        0,
                        "Puzzle Flowchart",
                        "Cocokkan simbol ke fungsi yang benar. Ini membantu kamu hafal bentuk flowchart.",
                        "Oval = Mulai/Selesai\nKotak = Proses\nBelah ketupat = Keputusan",
                        "Main Puzzle",
                        rewardKey = PUZZLE_SYMBOL_KEY,
                        scoreReward = 300
                    ),
                    CardContent(
                        "Urutan Langkah",
                        "Susun cerita",
                        R.drawable.urutan_latihan,
                        0,
                        "Puzzle Urutan",
                        "Susun langkah acak menjadi algoritma yang masuk akal.",
                        "Contoh:\n1. Mulai\n2. Siapkan buku\n3. Baca soal\n4. Tulis jawaban\n5. Selesai",
                        "Susun Sekarang",
                        rewardKey = SEQUENCE_ORDER_KEY,
                        scoreReward = 500
                    ),
                    CardContent(
                        "Quiz Cepat",
                        "Jawab pilihan",
                        R.drawable.quiz_latihan,
                        0,
                        "Quiz Cepat",
                        "Jawab pertanyaan singkat. Jika benar, score latihanmu naik.",
                        "Pertanyaan contoh:\nSimbol 'Mulai' berbentuk apa?\nJawaban: Oval",
                        "Jawab Quiz",
                        rewardKey = QUICK_QUIZ_KEY,
                        scoreReward = 700
                    ),
                    CardContent(
                        "Tantangan",
                        "Dapat reward",
                        R.drawable.tantangan_latihan,
                        0,
                        "Tantangan Harian",
                        "Selesaikan satu puzzle dan satu quiz untuk membuka reward baru.",
                        "Tantangan muncul ulang tiap 6 jam. Versi tampilan sekarang memakai 4 soal random sebagai simulasi.",
                        "Ambil Tantangan",
                        rewardKey = CHALLENGE_KEY,
                        scoreReward = 900
                    )
                )
                progress = trainingProgress
            }

            MainTab.LEADERBOARD -> {
                txtWelcome.text = "Leaderboard"
                txtSubtitle.text = "Lihat peringkat dan special score terbaik."
                imgTopHeader.setImageResource(R.drawable.leaderboard)
                imgMascotTalk.setImageResource(R.drawable.sorakan_leaderboard)
                txtMascotTitle.text = "Albi bersorak"
                txtMascotSpeech.text = "Kumpulkan special score dari challenge untuk masuk daftar terbaik!"
                txtSectionTitle.text = "Peringkat Minggu Ini"
                txtProgressTitle.text = "Total special score"
                statsRow.visibility = View.GONE
                imgHeroIcon.setImageResource(R.drawable.ic_star)
                cards = buildLeaderboardCards()
                buildLeaderboardDashboard()
                progress = ((totalScore * 100) / 5500).coerceAtMost(100)
            }

            MainTab.PROFIL -> {
                txtWelcome.text = "Profil Anak"
                txtSubtitle.text = "Lihat level, badge, dan statistik kamu."
                txtHeroTitle.text = "Level $displayLevel"
                txtHeroDesc.text = "$userName sudah mengumpulkan $totalScore score latihan. Terus lanjutkan misi!"
                txtHeroChip.text = "$totalScore Score"
                txtMascotTitle.text = "Albi bangga"
                txtMascotSpeech.text = "Semakin rajin latihan, levelmu naik. Menu pengaturan dan logout akan dirapikan di tahap berikutnya."
                txtSectionTitle.text = "Ringkasan"
                txtProgressTitle.text = "Progress level"
                statsRow.visibility = View.GONE
                updateProfileDashboard()
                imgHeroIcon.setImageResource(R.drawable.ic_user)
                cards = listOf(
                    CardContent("${completedLessons.size} Materi", "Sudah dipelajari", R.drawable.ic_code, R.drawable.bg_icon_blue, "Materi selesai", "Kamu sudah membuka banyak topik belajar.", "Progress: $lessonProgress%\nSelesai: ${completedLessons.size}/${lessons.size}", "Lihat Materi", targetTab = MainTab.MATERI),
                    CardContent("$totalScore Score", "Total latihan", R.drawable.ic_flow, R.drawable.bg_icon_green, "Score Latihan", "Leaderboard memakai total score dari Puzzle, Urutan, Quiz Cepat, dan Tantangan.", "Total score: $totalScore\nMode aktif: ${completedActivities.size}\nAkurasi: ${trainingModeStats().filter { it.bestScore > 0 }.averageOf { it.accuracy }}%", "Lihat Latihan", targetTab = MainTab.LATIHAN),
                    CardContent("Badge", "Lencana belajar", R.drawable.ic_star, R.drawable.bg_icon_yellow, "Koleksi Badge", "Badge muncul dari progress materi, leaderboard, dan tingkatan.", "Score: $totalScore\nLevel: $displayLevel\nBadge: ${currentBadge()}", "Buka Badge"),
                    CardContent("Pengaturan", "Profil dan suara", R.drawable.ic_user, R.drawable.bg_icon_purple, "Pengaturan Anak", "Nanti bagian ini bisa dipakai untuk nama profil, suara, dan tema warna.", "Nama: $userName\nMode: Anak\nTema: Cerah", "Atur Profil")
                )
                progress = ((lessonProgress + trainingProgress) / 2).coerceAtLeast(10)
            }
        }

        txtProgressPercent.text = "$progress%"
        progressLearning.progress = progress
        txtMateriTopPercent.text = "$progress%"
        materiTopProgress.progress = progress
        imgHeroIcon.setColorFilter(ContextCompat.getColor(this, R.color.algoplay_text))
        currentCards = cards
        bindCards(cards)
        updateDetail(cards.first())
    }

    private fun updateSectionVisibility(tab: MainTab) {
        val isHome = tab == MainTab.HOME
        val isProfile = tab == MainTab.PROFIL
        val isMateri = tab == MainTab.MATERI
        val isLatihan = tab == MainTab.LATIHAN
        val isLeaderboard = tab == MainTab.LEADERBOARD

        homeDashboard.visibility = if (isHome) View.VISIBLE else View.GONE
        materiProgressTop.visibility = if (isMateri) View.VISIBLE else View.GONE
        materiHeroCard.visibility = if (isMateri) View.VISIBLE else View.GONE
        materiInfoCard.visibility = if (isMateri) View.VISIBLE else View.GONE
        materiMascotCard.visibility = if (isMateri) View.VISIBLE else View.GONE
        topHeader.visibility = if (isHome || isProfile || isMateri) View.GONE else View.VISIBLE
        heroCard.visibility = View.GONE
        mascotTalkCard.visibility = if (isLatihan || isLeaderboard) View.VISIBLE else View.GONE
        txtSectionTitle.visibility = if (isHome || isProfile || isLeaderboard) View.GONE else View.VISIBLE
        statsRow.visibility = View.GONE

        lessonList.visibility = if (isMateri) View.VISIBLE else View.GONE
        cardGrid.visibility = if (isLatihan) View.VISIBLE else View.GONE
        leaderboardDashboard.visibility = if (isLeaderboard) View.VISIBLE else View.GONE
        detailCard.visibility = View.GONE
        progressCard.visibility = View.GONE
        profileDashboard.visibility = if (isProfile) View.VISIBLE else View.GONE
    }

    private fun updateHomeStatusBar() {
        val now = Date()
        val rank = scoreRank(totalScore)

        txtHomeUserName.text = if (isGuestMode) "Guest" else userName
        homeProfileAvatarWrap.visibility = if (isGuestMode) View.GONE else View.VISIBLE
        txtHomeDay.text = SimpleDateFormat("EEEE", Locale("id", "ID")).format(now)
        txtHomeDateTime.text = SimpleDateFormat("d MMMM yyyy - HH:mm", Locale("id", "ID")).format(now)
        homeRankBadge.visibility = if (isGuestMode) View.GONE else View.VISIBLE
        if (!isGuestMode) {
            txtHomeRankLevel.text = "Tingkat ${rank.level}"
            txtHomeRankStatus.text = rank.status
            homeRankBadge.background = roundedDrawable(Color.parseColor(rank.colorHex), dp(18))
            txtHomeRankLevel.setTextColor(ContextCompat.getColor(this, R.color.white))
            txtHomeRankStatus.setTextColor(ContextCompat.getColor(this, R.color.white))
        }
        updateHomeProfilePhoto()
    }

    private fun updateHomeProfilePhoto() {
        imgHomeProfilePhoto.clearColorFilter()
        imgHomeProfilePhoto.imageTintList = null

        if (isGuestMode) {
            imgHomeProfilePhoto.setImageResource(R.drawable.ic_user)
            imgHomeProfilePhoto.setColorFilter(ContextCompat.getColor(this, R.color.algoplay_blue_dark))
            imgHomeProfilePhoto.scaleType = ImageView.ScaleType.FIT_CENTER
            return
        }

        userLocalPhotoUri?.let { uri ->
            runCatching {
                imgHomeProfilePhoto.setImageURI(uri)
                imgHomeProfilePhoto.scaleType = ImageView.ScaleType.CENTER_CROP
            }.onSuccess {
                return
            }
        }

        imgHomeProfilePhoto.setImageResource(R.drawable.ic_user)
        imgHomeProfilePhoto.setColorFilter(ContextCompat.getColor(this, R.color.algoplay_blue_dark))
        imgHomeProfilePhoto.scaleType = ImageView.ScaleType.FIT_CENTER
        userPhotoUrl?.takeIf { it.isNotBlank() }?.let {
            loadRemotePhoto(it, imgHomeProfilePhoto)
        }
    }

    private fun updateHomeDashboardState() {
        updateContinueCard()
        homeChallengeCard.visibility = if (completedActivities.contains(CHALLENGE_KEY)) View.GONE else View.VISIBLE
        txtHomeChallengeCaption.text = if (isGuestMode) {
            "Guest bisa coba, tapi score tidak disimpan."
        } else {
            "4 soal random siap dibuka. Score masuk total leaderboard."
        }
        txtHomeAccuracy.text = "${trainingAccuracy()}%"
        txtHomeStreak.text = "${weeklyStreak()} hari"
        txtHomeLeaderboardRank.text = if (isGuestMode) {
            "Login untuk rank"
        } else {
            "Rank ${homeLeaderboardRankText()} - $totalScore score"
        }
    }

    private fun updateContinueCard() {
        val target = continueTarget()
        homeContinueCard.visibility = if (target == null) View.GONE else View.VISIBLE
        target ?: return

        when (target.first) {
            CONTINUE_MATERI -> {
                val lessonNumber = target.second.toIntOrNull() ?: nextLessonNumber()
                val lesson = lessons.firstOrNull { it.number == lessonNumber } ?: lessons.first()
                txtHomeContinueTitle.text = "Lanjutkan Materi"
                txtHomeContinueCaption.text = "Materi $lessonNumber - ${lesson.title}"
                homeContinueProgress.progress = lessonProgress
            }
            CONTINUE_LATIHAN -> {
                val stat = trainingModeStats().firstOrNull { it.key == target.second }
                txtHomeContinueTitle.text = "Lanjutkan Latihan"
                txtHomeContinueCaption.text = stat?.title ?: "Pilih mode latihan terakhir"
                homeContinueProgress.progress = trainingProgress
            }
        }
    }

    private fun continueTarget(): Pair<String, String>? {
        val type = lastContinueType
        val key = lastContinueKey
        if (!type.isNullOrBlank() && !key.isNullOrBlank()) return type to key
        if (completedLessons.isNotEmpty()) return CONTINUE_MATERI to nextLessonNumber().toString()
        if (completedActivities.isNotEmpty()) return CONTINUE_LATIHAN to completedActivities.last()
        return null
    }

    private fun continueLearning() {
        val target = continueTarget() ?: return
        when (target.first) {
            CONTINUE_MATERI -> {
                val lessonNumber = target.second.toIntOrNull() ?: nextLessonNumber()
                openLessonPage(lessonNumber)
            }
            CONTINUE_LATIHAN -> {
                val rewardKey = target.second
                when (rewardKey) {
                    PUZZLE_SYMBOL_KEY -> openPuzzleSymbolPage()
                    SEQUENCE_ORDER_KEY -> openSequenceOrderPage()
                    QUICK_QUIZ_KEY -> openQuickQuizPage()
                    CHALLENGE_KEY -> openChallengePage()
                    else -> selectTab(MainTab.LATIHAN)
                }
            }
        }
    }

    private fun openChallengeFromHome() {
        saveContinueTarget(CONTINUE_LATIHAN, CHALLENGE_KEY)
        openChallengePage()
    }

    private fun openTrainingMode(index: Int) {
        val card = currentCards.getOrNull(index) ?: return
        when (card.rewardKey) {
            PUZZLE_SYMBOL_KEY -> openPuzzleSymbolPage()
            SEQUENCE_ORDER_KEY -> openSequenceOrderPage()
            QUICK_QUIZ_KEY -> openQuickQuizPage()
            CHALLENGE_KEY -> openChallengePage()
            else -> Toast.makeText(this, "Halaman ${card.title} akan dibuat berikutnya.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openPuzzleSymbolPage() {
        saveContinueTarget(CONTINUE_LATIHAN, PUZZLE_SYMBOL_KEY)
        val uid = auth.currentUser?.uid.orEmpty()
        val intent = Intent(this, PuzzleSymbolActivity::class.java).apply {
            putExtra(PuzzleSymbolActivity.EXTRA_GUEST_MODE, isGuestMode)
            putExtra(PuzzleSymbolActivity.EXTRA_USER_ID, uid)
        }
        trainingLauncher.launch(intent)
    }

    private fun openSequenceOrderPage() {
        saveContinueTarget(CONTINUE_LATIHAN, SEQUENCE_ORDER_KEY)
        trainingLauncher.launch(Intent(this, SequenceOrderActivity::class.java))
    }

    private fun openQuickQuizPage() {
        saveContinueTarget(CONTINUE_LATIHAN, QUICK_QUIZ_KEY)
        trainingLauncher.launch(Intent(this, QuickQuizActivity::class.java))
    }

    private fun openChallengePage() {
        saveContinueTarget(CONTINUE_LATIHAN, CHALLENGE_KEY)
        trainingLauncher.launch(Intent(this, ChallengeActivity::class.java))
    }

    private fun nextLessonNumber(): Int {
        return ((completedLessons.maxOrNull() ?: 0) + 1).coerceAtMost(lessons.size).coerceAtLeast(1)
    }

    private fun saveContinueTarget(type: String, key: String) {
        lastContinueType = type
        lastContinueKey = key
        if (!isGuestMode) {
            saveLocalProgress()
        }
    }

    private fun trainingAccuracy(): Int {
        return trainingModeStats()
            .filter { it.bestScore > 0 }
            .averageOf { it.accuracy }
    }

    private fun weeklyStreak(): Int {
        return if (completedLessons.isEmpty() && completedActivities.isEmpty()) {
            0
        } else {
            (1 + (completedLessons.size / 2) + completedActivities.size).coerceAtMost(7)
        }
    }

    private fun homeLeaderboardRankText(): String {
        val rank = leaderboardEntries().indexOfFirst { it.isCurrentUser } + 1
        return if (rank in 1..10) "#$rank" else "--"
    }

    @Suppress("DEPRECATION")
    private fun updatePageBackground(tab: MainTab) {
        mainRoot.setBackgroundResource(tab.pageBackgroundRes())
        window.statusBarColor = ContextCompat.getColor(this, tab.statusColorRes())
        window.navigationBarColor = ContextCompat.getColor(this, R.color.white)
        window.decorView.systemUiVisibility = if (tab == MainTab.PROFIL) {
            0
        } else {
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    private fun buildLessonList() {
        lessonList.removeAllViews()

        lessons.forEach { lesson ->
            val isCompleted = completedLessons.contains(lesson.number)
            val isUnlocked = lesson.number == 1 || completedLessons.contains(lesson.number - 1) || isCompleted
            val state = when {
                isCompleted -> "Selesai"
                isUnlocked -> "Terbuka"
                else -> "Terkunci"
            }
            lessonList.addView(createLessonRow(lesson, state, isUnlocked, isCompleted))
        }
    }

    private fun createLessonRow(
        lesson: LessonContent,
        state: String,
        unlocked: Boolean,
        completed: Boolean
    ): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = ContextCompat.getDrawable(
                this@MainActivity,
                if (unlocked) R.drawable.bg_lesson_unlocked else R.drawable.bg_lesson_locked
            )
            elevation = if (unlocked) 2f else 0f
            setPadding(dp(14), dp(12), dp(14), dp(12))
            isClickable = true
            isFocusable = true
        }

        row.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(10)
            leftMargin = dp(2)
            rightMargin = dp(2)
        }

        val numberView = TextView(this).apply {
            text = if (unlocked) lesson.number.toString() else ""
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            background = ContextCompat.getDrawable(
                this@MainActivity,
                if (unlocked) R.drawable.bg_lesson_number else R.drawable.bg_lesson_locked_number
            )
            if (!unlocked) {
                setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_lock, 0, 0)
            }
        }
        row.addView(numberView, LinearLayout.LayoutParams(dp(42), dp(42)))

        val textGroup = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(13), 0, dp(8), 0)
        }
        row.addView(textGroup, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        textGroup.addView(TextView(this).apply {
            text = lesson.title
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.algoplay_text))
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
        })

        textGroup.addView(TextView(this).apply {
            text = when {
                completed -> "Selesai, ketuk untuk baca lagi"
                unlocked -> "Ketuk untuk belajar"
                else -> "Selesaikan materi sebelumnya dulu"
            }
            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.algoplay_subtext))
            textSize = 12f
            setPadding(0, dp(4), 0, 0)
        })

        val status = TextView(this).apply {
            text = state
            gravity = Gravity.CENTER
            setTextColor(
                ContextCompat.getColor(
                    this@MainActivity,
                    if (unlocked) R.color.algoplay_blue_dark else R.color.algoplay_subtext
                )
            )
            textSize = 11f
            setTypeface(null, Typeface.BOLD)
        }
        row.addView(status, LinearLayout.LayoutParams(dp(66), dp(34)))

        row.setOnClickListener {
            if (unlocked) {
                openLessonPage(lesson.number)
            } else {
                Toast.makeText(this, "Materi ini masih terkunci", Toast.LENGTH_SHORT).show()
            }
        }

        return row
    }

    private fun openLessonPage(lessonNumber: Int) {
        val lessonActivity = when (lessonNumber) {
            1 -> LessonOneActivity::class.java
            2 -> LessonTwoActivity::class.java
            3 -> LessonThreeActivity::class.java
            4 -> LessonFourActivity::class.java
            5 -> LessonFiveActivity::class.java
            6 -> LessonSixActivity::class.java
            7 -> LessonSevenActivity::class.java
            8 -> LessonEightActivity::class.java
            9 -> LessonNineActivity::class.java
            10 -> LessonTenActivity::class.java
            11 -> LessonElevenActivity::class.java
            12 -> LessonTwelveActivity::class.java
            else -> null
        }

        if (lessonActivity != null) {
            val intent = Intent(this, lessonActivity).apply {
                putExtra(LessonOneActivity.EXTRA_GUEST_MODE, isGuestMode)
                putExtra(LessonOneActivity.EXTRA_USER_NAME, if (isGuestMode) "Guest" else userName)
                putExtra(LessonOneActivity.EXTRA_SCORE, totalScore)
                putExtra(LessonOneActivity.EXTRA_PHOTO_URL, userPhotoUrl)
                putExtra(LessonOneActivity.EXTRA_PHOTO_URI, userLocalPhotoUri?.toString())
                putExtra(LessonOneActivity.EXTRA_ALREADY_COMPLETED, completedLessons.contains(lessonNumber))
            }
            lessonLauncher.launch(intent)
        } else {
            Toast.makeText(this, "Semua halaman materi sudah tersedia.", Toast.LENGTH_LONG).show()
        }
    }

    private fun markLessonCompletedFromPage(lessonNumber: Int) {
        val wasNew = completedLessons.add(lessonNumber)
        saveContinueTarget(CONTINUE_MATERI, (lessonNumber + 1).coerceAtMost(lessons.size).toString())
        if (!isGuestMode && wasNew) {
            saveLocalProgress()
        }
        updateContent(MainTab.MATERI)
        val message = when {
            isGuestMode && wasNew -> "Simulasi selesai. Materi berikutnya terbuka selama sesi ini."
            isGuestMode -> "Materi $lessonNumber sudah selesai di sesi ini."
            wasNew -> "Materi selesai! +$LESSON_SCORE_REWARD score."
            else -> "Materi $lessonNumber sudah pernah selesai."
        }
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun markFinalExamCompletedFromPage(score: Int) {
        val normalizedScore = score.coerceIn(0, 100)
        val wasNew = completedLessons.add(FINAL_EXAM_LESSON_NUMBER)
        val oldExamScore = finalExamScore
        saveContinueTarget(CONTINUE_MATERI, FINAL_EXAM_LESSON_NUMBER.toString())

        if (!isGuestMode) {
            finalExamScore = maxOf(finalExamScore, normalizedScore)
            if (wasNew || finalExamScore != oldExamScore) {
                saveLocalProgress()
            }
        }

        updateContent(MainTab.MATERI)
        val message = when {
            isGuestMode -> "Simulasi ujian selesai. Login untuk menyimpan nilai."
            finalExamScore > oldExamScore -> "Ujian selesai! Score ujian bertambah ${finalExamScore - oldExamScore}."
            wasNew -> "Ujian selesai! Nilai terbaikmu $finalExamScore."
            else -> "Nilai ujian terbaik tetap $finalExamScore."
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun handleDetailAction() {
        val card = activeCard
        val targetTab = card?.targetTab

        when {
            card?.rewardKey != null -> completeActivity(card)
            targetTab != null -> selectTab(targetTab)
            txtDetailAction.text.toString().contains("Reward", ignoreCase = true) -> selectTab(MainTab.PROFIL)
            else -> Toast.makeText(this, txtDetailAction.text.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun completeActivity(card: CardContent) {
        val rewardKey = card.rewardKey ?: return

        if (completedActivities.contains(rewardKey)) {
            Toast.makeText(this, "Mode ini sudah selesai", Toast.LENGTH_SHORT).show()
            return
        }

        if (isGuestMode) {
            Toast.makeText(this, "Nilai simulasi: ${card.scoreReward}. Login untuk menyimpan score.", Toast.LENGTH_LONG).show()
            return
        }

        completedActivities.add(rewardKey)
        saveContinueTarget(CONTINUE_LATIHAN, rewardKey)
        saveLocalProgress()

        Toast.makeText(
            this,
            "${card.detailTitle} selesai! +${card.scoreReward} score",
            Toast.LENGTH_SHORT
        ).show()
        selectTab(MainTab.LATIHAN, shouldScroll = false)
        currentCards.indexOfFirst { it.rewardKey == rewardKey }
            .takeIf { it >= 0 }
            ?.let { showCardDetail(it) }
    }

    private fun updateProfileDashboard() {
        val materialProgress = lessonProgress
        val modeStats = trainingModeStats()
        val completedStats = modeStats.filter { it.bestScore > 0 }
        val accuracy = completedStats.averageOf { it.accuracy }
        val averageScore = completedStats.averageOf { it.averageScore }
        val highScore = completedStats.maxOfOrNull { it.bestScore } ?: 0
        val weeklyStreak = weeklyStreak()
        val rank = scoreRank(totalScore)

        txtProfileGreeting.text = userName
        txtProfileRankBadge.text = "Tingkat ${rank.level} - ${rank.status}"
        updateProfileBadges(rank)
        txtProfileProgressCaption.text = "Selesai ${completedLessons.size} dari ${lessons.size} materi"
        txtProfileProgressPercent.text = "$materialProgress%"
        profileMaterialProgress.progress = materialProgress
        txtAccuracy.text = "$accuracy%"
        txtRewardScore.text = totalScore.toString()
        txtProfileAverageScore.text = averageScore.toString()
        txtStreakBadge.text = "$weeklyStreak/7 aktif"
        txtProfileModes.text = "${completedActivities.size}/4"
        txtProfileLessons.text = "${completedLessons.size}/${lessons.size}"
        txtProfileActivities.text = highScore.toString()
        txtProfileBadge.text = currentBadge()
        txtProfileRankStatus.text = rank.status
        renderStreakWeek(weeklyStreak)
        renderProfileTrainingStats(modeStats)
        updateProfilePhoto()
    }

    private fun updateProfileBadges(rank: ScoreRank) {
        val leaderboardRank = leaderboardEntries().indexOfFirst { it.isCurrentUser } + 1
        imgProfileLeaderboardBadge.alpha = if (leaderboardRank in 1..10) 1f else 0.38f
        imgProfileLeaderboardBadge.contentDescription = when (leaderboardRank) {
            1 -> "Lencana juara leaderboard 1"
            2 -> "Lencana leaderboard peringkat 2"
            3 -> "Lencana leaderboard peringkat 3"
            in 4..10 -> "Lencana leaderboard top 10"
            else -> "Lencana leaderboard belum terbuka"
        }
        imgProfileMaterialBadge.alpha = if (completedLessons.isNotEmpty()) 1f else 0.48f
        imgProfileMaterialBadge.contentDescription = if (completedLessons.size >= lessons.size) {
            "Lencana materi tuntas"
        } else {
            "Lencana materi ${completedLessons.size} dari ${lessons.size}"
        }
        imgProfileLevelBadge.alpha = 1f
        imgProfileLevelBadge.contentDescription = "Lencana tingkat ${rank.level} ${rank.status}"
    }

    private fun renderStreakWeek(activeDays: Int) {
        val days = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
        profileStreakWeek.removeAllViews()

        days.forEachIndexed { index, day ->
            val isActive = index < activeDays
            val dayView = createCompactText(
                day,
                11,
                if (isActive) R.color.white else R.color.algoplay_subtext,
                isActive,
                Gravity.CENTER
            ).apply {
                background = ContextCompat.getDrawable(
                    this@MainActivity,
                    if (isActive) R.drawable.bg_streak_done else R.drawable.bg_streak_empty
                )
            }
            profileStreakWeek.addView(
                dayView,
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
                    marginStart = dp(3)
                    marginEnd = dp(3)
                }
            )
        }
    }

    private fun renderProfileTrainingStats(stats: List<TrainingModeStat>) {
        val maxScore = stats.maxOfOrNull { it.maxScore } ?: 1
        profileScoreChart.removeAllViews()
        profileTrainingStatsList.removeAllViews()

        stats.forEach { stat ->
            profileScoreChart.addView(
                createScoreBar(stat, maxScore),
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
                    marginStart = dp(4)
                    marginEnd = dp(4)
                }
            )
        }

        stats.forEach { stat ->
            profileTrainingStatsList.addView(
                createTrainingStatRow(stat),
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(66)).apply {
                    topMargin = dp(8)
                }
            )
        }
    }

    private fun createScoreBar(stat: TrainingModeStat, maxScore: Int): View {
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        }
        column.addView(createCompactText(stat.bestScore.toString(), 11, R.color.algoplay_text, true, Gravity.CENTER))

        val trackHeight = dp(82)
        val track = FrameLayout(this).apply {
            background = roundedDrawable(ContextCompat.getColor(this@MainActivity, R.color.algoplay_blue_soft), dp(12))
        }
        val barHeight = if (stat.bestScore <= 0) {
            dp(8)
        } else {
            ((stat.bestScore.toFloat() / maxScore.toFloat()) * trackHeight).toInt().coerceAtLeast(dp(12))
        }
        track.addView(
            View(this).apply {
                background = roundedDrawable(ContextCompat.getColor(this@MainActivity, stat.colorRes), dp(12))
            },
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, barHeight, Gravity.BOTTOM)
        )
        column.addView(track, LinearLayout.LayoutParams(dp(38), trackHeight).apply {
            topMargin = dp(6)
        })
        column.addView(createCompactText(stat.shortLabel, 10, R.color.algoplay_subtext, true, Gravity.CENTER).apply {
            maxLines = 1
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(6)
        })
        return column
    }

    private fun createTrainingStatRow(stat: TrainingModeStat): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = roundedDrawable(ContextCompat.getColor(this@MainActivity, R.color.algoplay_bg), dp(16))
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }

        row.addView(
            View(this).apply {
                background = roundedDrawable(ContextCompat.getColor(this@MainActivity, stat.colorRes), dp(6))
            },
            LinearLayout.LayoutParams(dp(8), LinearLayout.LayoutParams.MATCH_PARENT)
        )

        val textGroup = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }
        textGroup.addView(createCompactText(stat.title, 14, R.color.algoplay_text, true, Gravity.START))
        val detailText = if (stat.bestScore > 0) {
            "Best ${stat.bestScore} - Rata ${stat.averageScore} - Akurasi ${stat.accuracy}%"
        } else {
            "Belum dimainkan - score masih 0"
        }
        textGroup.addView(createCompactText(detailText, 11, R.color.algoplay_subtext, false, Gravity.START).apply {
            maxLines = 1
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(4)
        })
        row.addView(textGroup, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
            marginStart = dp(12)
        })

        row.addView(createCompactText("${stat.bestScore}", 15, R.color.algoplay_blue_dark, true, Gravity.END), LinearLayout.LayoutParams(dp(54), LinearLayout.LayoutParams.MATCH_PARENT))
        return row
    }

    private fun trainingModeStats(): List<TrainingModeStat> {
        val baseStats = listOf(
            TrainingModeStat(PUZZLE_SYMBOL_KEY, "Puzzle Simbol", "Puzzle", 300, 0, 0, 0, R.color.algoplay_blue_dark),
            TrainingModeStat(SEQUENCE_ORDER_KEY, "Urutan Langkah", "Urutan", 500, 0, 0, 0, R.color.algoplay_green_dark),
            TrainingModeStat(QUICK_QUIZ_KEY, "Quiz Cepat", "Quiz", 700, 0, 0, 0, R.color.algoplay_red_dark),
            TrainingModeStat(CHALLENGE_KEY, "Tantangan 6 Jam", "Tantang", 900, 0, 0, 0, R.color.algoplay_gold_dark)
        )

        return baseStats.mapIndexed { index, stat ->
            if (!completedActivities.contains(stat.key)) {
                stat
            } else {
                val accuracy = (76 + (completedLessons.size / 2) + (index * 4)).coerceAtMost(98)
                val average = ((stat.maxScore * (72 + accuracy)) / 170).coerceAtMost(stat.maxScore)
                stat.copy(
                    bestScore = stat.maxScore,
                    averageScore = average,
                    accuracy = accuracy
                )
            }
        }
    }

    private fun List<TrainingModeStat>.averageOf(selector: (TrainingModeStat) -> Int): Int {
        if (isEmpty()) return 0
        return sumOf(selector) / size
    }

    private fun roundedDrawable(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius.toFloat()
            setColor(color)
        }
    }

    private fun roundedStrokeDrawable(fillColor: Int, strokeColor: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius.toFloat()
            setColor(fillColor)
            setStroke(dp(1), strokeColor)
        }
    }

    private fun gradientDrawable(startColor: Int, endColor: Int, radius: Int): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(startColor, endColor)
        ).apply {
            cornerRadius = radius.toFloat()
        }
    }

    private fun updateProfilePhoto() {
        imgProfilePhoto.clearColorFilter()
        imgProfilePhoto.imageTintList = null
        userLocalPhotoUri?.let { uri ->
            runCatching {
                imgProfilePhoto.setImageURI(uri)
                imgProfilePhoto.scaleType = ImageView.ScaleType.CENTER_CROP
            }.onSuccess {
                return
            }
        }

        imgProfilePhoto.setImageResource(R.drawable.ic_user)
        imgProfilePhoto.imageTintList = null
        imgProfilePhoto.setColorFilter(ContextCompat.getColor(this, R.color.algoplay_blue_dark))
        imgProfilePhoto.scaleType = ImageView.ScaleType.FIT_CENTER
        userPhotoUrl?.takeIf { it.isNotBlank() }?.let {
            loadRemotePhoto(it, imgProfilePhoto)
        }
    }

    private fun bindCards(cards: List<CardContent>) {
        cards.forEachIndexed { index, card ->
            if (card.iconBgRes == 0) {
                iconWraps[index].background = null
                cardIcons[index].clearColorFilter()
                cardIcons[index].imageTintList = null
            } else {
                iconWraps[index].setBackgroundResource(card.iconBgRes)
                cardIcons[index].imageTintList = null
                cardIcons[index].setColorFilter(ContextCompat.getColor(this, R.color.white))
            }
            cardIcons[index].setImageResource(card.iconRes)
            cardIcons[index].scaleType = ImageView.ScaleType.FIT_CENTER
            cardTitles[index].text = card.title
            cardDescs[index].text = card.desc
        }
    }

    private fun showCardDetail(index: Int) {
        val card = currentCards.getOrNull(index) ?: return
        activeCard = card
        if (currentTab == MainTab.LATIHAN) {
            card.rewardKey?.let { saveContinueTarget(CONTINUE_LATIHAN, it) }
        }
        updateDetail(card)
        detailCard.visibility = View.VISIBLE
        progressCard.visibility = if (
            currentTab == MainTab.LATIHAN ||
            currentTab == MainTab.PROFIL ||
            currentTab == MainTab.MATERI
        ) View.GONE else View.VISIBLE
        mainScroll.post { mainScroll.smoothScrollTo(0, detailCard.top - dp(12)) }
    }

    private fun updateDetail(card: CardContent) {
        txtDetailEyebrow.text = when (currentTab) {
            MainTab.HOME -> "Pilihan Albi"
            MainTab.MATERI -> "Materi"
            MainTab.LATIHAN -> "Latihan"
            MainTab.LEADERBOARD -> "Leaderboard"
            MainTab.PROFIL -> "Profil Anak"
        }
        txtDetailTitle.text = card.detailTitle
        txtDetailBody.text = card.detailBody
        txtDetailSteps.text = card.detailSteps.ifEmpty { "1. Baca arahan\n2. Coba aktivitas\n3. Kumpulkan score" }
        txtDetailAction.text = card.actionText
    }

    private fun buildLeaderboardCards(): List<CardContent> {
        val entries = leaderboardEntries()

        return entries.take(4).mapIndexed { index, entry ->
            val displayName = if (entry.isCurrentUser) "Kamu" else entry.name
            CardContent(
                "#${index + 1} $displayName",
                "${entry.score} score",
                entry.iconRes,
                entry.iconBgRes,
                if (entry.isCurrentUser) "Peringkat Kamu" else "Peringkat ${index + 1}",
                "${entry.note}. Kumpulkan total score dari Puzzle, Urutan, Quiz Cepat, dan Tantangan.",
                "Score: ${entry.score}\nTingkat: ${scoreRank(entry.score).status}\nBadge: ${if (entry.isCurrentUser) currentBadge() else "Challenger"}",
                if (entry.isCurrentUser) "Mulai Game" else "Coba Kejar",
                targetTab = MainTab.LATIHAN
            )
        }
    }

    private fun leaderboardEntries(): List<LeaderboardEntry> {
        return listOf(
            LeaderboardEntry("Raden", 6120, R.drawable.ic_user, R.drawable.bg_score_orange, "Kuat di Tantangan Harian"),
            LeaderboardEntry("Albi", 5380, R.drawable.ic_user, R.drawable.bg_score_blue, "Akurat di Urutan Langkah"),
            LeaderboardEntry("Naya", 4860, R.drawable.ic_user, R.drawable.bg_score_purple, "Cepat memahami materi"),
            LeaderboardEntry("Bima", 4320, R.drawable.ic_user, R.drawable.bg_score_green, "Rajin menyelesaikan puzzle"),
            LeaderboardEntry("Cika", 3760, R.drawable.ic_user, R.drawable.bg_score_purple, "Jago quiz cepat"),
            LeaderboardEntry("Dimas", 3180, R.drawable.ic_user, R.drawable.bg_score_blue, "Teliti di simbol flowchart"),
            LeaderboardEntry("Salsa", 2520, R.drawable.ic_user, R.drawable.bg_score_orange, "Kuat di challenge"),
            LeaderboardEntry("Mika", 1780, R.drawable.ic_user, R.drawable.bg_score_green, "Rapi menyusun langkah"),
            LeaderboardEntry("Gilang", 820, R.drawable.ic_user, R.drawable.bg_score_blue, "Mulai naik peringkat"),
            LeaderboardEntry("Tara", 320, R.drawable.ic_user, R.drawable.bg_score_green, "Baru masuk leaderboard"),
            LeaderboardEntry(userName, totalScore, R.drawable.ic_user, R.drawable.bg_score_blue, "Progress lokal kamu", true, userPhotoUrl)
        ).sortedWith(
            compareByDescending<LeaderboardEntry> { it.score }
                .thenBy { if (it.isCurrentUser) 0 else 1 }
        ).take(10)
    }

    private fun buildLeaderboardDashboard() {
        val entries = leaderboardEntries()
        leaderboardPodiumRow.removeAllViews()
        leaderboardList.removeAllViews()

        listOf(1, 0, 2).forEach { index ->
            val entry = entries.getOrNull(index) ?: return@forEach
            val rank = index + 1
            val params = LinearLayout.LayoutParams(0, podiumHeight(rank), 1f).apply {
                marginStart = dp(4)
                marginEnd = dp(4)
            }
            leaderboardPodiumRow.addView(createPodiumCard(entry, rank), params)
        }

        entries.drop(3).forEachIndexed { index, entry ->
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(72)
            ).apply {
                bottomMargin = dp(10)
            }
            leaderboardList.addView(createLeaderboardRow(entry, index + 4), params)
        }
    }

    private fun podiumHeight(rank: Int): Int {
        return when (rank) {
            1 -> dp(210)
            2 -> dp(184)
            else -> dp(170)
        }
    }

    private fun createPodiumCard(entry: LeaderboardEntry, rank: Int): View {
        val rankInfo = scoreRank(entry.score)
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = ContextCompat.getDrawable(
                this@MainActivity,
                if (entry.isCurrentUser) {
                    R.drawable.bg_leaderboard_current
                } else {
                    when (rank) {
                        1 -> R.drawable.bg_score_orange
                        2 -> R.drawable.bg_score_blue
                        else -> R.drawable.bg_score_purple
                    }
                }
            )
            elevation = if (rank == 1) 4f else 2f
            setPadding(dp(8), dp(10), dp(8), dp(10))
        }

        card.addView(createCompactText("#$rank", 13, R.color.algoplay_text, true, Gravity.CENTER))
        card.addView(createAvatar(entry, 54), LinearLayout.LayoutParams(dp(54), dp(54)).apply {
            topMargin = dp(8)
        })
        card.addView(createCompactText(if (entry.isCurrentUser) "Kamu" else entry.name, 14, R.color.algoplay_text, true, Gravity.CENTER).apply {
            maxLines = 1
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(8)
        })
        card.addView(createCompactText("${entry.score}", 18, R.color.algoplay_gold_dark, true, Gravity.CENTER).apply {
            maxLines = 1
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(4)
        })
        card.addView(createCompactText(rankInfo.status, 11, R.color.algoplay_subtext, true, Gravity.CENTER).apply {
            maxLines = 1
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(3)
        })

        return card
    }

    private fun createLeaderboardRow(entry: LeaderboardEntry, rank: Int): View {
        val rankInfo = scoreRank(entry.score)
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = ContextCompat.getDrawable(
                this@MainActivity,
                if (entry.isCurrentUser) R.drawable.bg_leaderboard_current else R.drawable.bg_home_card
            )
            elevation = if (entry.isCurrentUser) 4f else 2f
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }

        row.addView(createCompactText("#$rank", 14, R.color.algoplay_text, true, Gravity.CENTER), LinearLayout.LayoutParams(dp(38), LinearLayout.LayoutParams.MATCH_PARENT))
        row.addView(createAvatar(entry, 44), LinearLayout.LayoutParams(dp(44), dp(44)).apply {
            marginStart = dp(8)
        })

        val textGroup = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }
        textGroup.addView(createCompactText(if (entry.isCurrentUser) "Kamu" else entry.name, 15, R.color.algoplay_text, true, Gravity.START).apply {
            maxLines = 1
        })
        textGroup.addView(createCompactText(rankInfo.status, 12, R.color.algoplay_subtext, false, Gravity.START).apply {
            maxLines = 1
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(4)
        })
        row.addView(textGroup, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply {
            marginStart = dp(10)
        })

        val scoreGroup = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL or Gravity.END
        }
        scoreGroup.addView(createCompactText("${entry.score}", 15, R.color.algoplay_gold_dark, true, Gravity.END).apply {
            maxLines = 1
        })
        scoreGroup.addView(createCompactText("Tingkat ${rankInfo.level}", 11, R.color.algoplay_subtext, false, Gravity.END).apply {
            maxLines = 1
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(4)
        })
        row.addView(scoreGroup, LinearLayout.LayoutParams(dp(86), LinearLayout.LayoutParams.MATCH_PARENT))

        return row
    }

    private fun createAvatar(entry: LeaderboardEntry, size: Int): FrameLayout {
        val avatar = FrameLayout(this).apply {
            background = ContextCompat.getDrawable(this@MainActivity, R.drawable.bg_profile_avatar)
            setPadding(dp(4), dp(4), dp(4), dp(4))
        }
        val imageView = ImageView(this).apply {
            setImageResource(entry.iconRes)
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
            if (entry.iconRes == R.drawable.ic_user) {
                setColorFilter(ContextCompat.getColor(this@MainActivity, R.color.algoplay_blue_dark))
            }
        }
        avatar.addView(imageView, FrameLayout.LayoutParams(dp(size - 8), dp(size - 8), Gravity.CENTER))
        entry.photoUrl?.takeIf { it.isNotBlank() }?.let { loadRemotePhoto(it, imageView) }
        return avatar
    }

    private fun loadRemotePhoto(photoUrl: String, target: ImageView) {
        thread {
            runCatching {
                URL(photoUrl).openStream().use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }.onSuccess { bitmap ->
                runOnUiThread {
                    target.clearColorFilter()
                    target.setImageBitmap(bitmap)
                    target.scaleType = ImageView.ScaleType.CENTER_CROP
                }
            }
        }
    }

    private fun createCompactText(
        value: String,
        sizeSp: Int,
        colorRes: Int,
        bold: Boolean,
        gravityValue: Int
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = sizeSp.toFloat()
            setTextColor(ContextCompat.getColor(this@MainActivity, colorRes))
            gravity = gravityValue
            includeFontPadding = false
            if (bold) setTypeface(null, Typeface.BOLD)
        }
    }

    private fun currentBadge(): String {
        return when {
            completedLessons.size >= lessons.size -> "Master Flowchart"
            completedActivities.size >= 4 -> "Puzzle Solver"
            completedLessons.size >= 4 -> "Junior Flowchart"
            else -> "Pemula Algoritma"
        }
    }

    private fun scoreRank(score: Int): ScoreRank {
        return when {
            score >= 5500 -> ScoreRank(10, "Legend", "#22D3EE")
            score >= 4800 -> ScoreRank(9, "Grandmaster", "#FACC15")
            score >= 4100 -> ScoreRank(8, "Master", "#F97316")
            score >= 3400 -> ScoreRank(7, "Expert", "#EF4444")
            score >= 2700 -> ScoreRank(6, "Strategist", "#EC4899")
            score >= 2000 -> ScoreRank(5, "Problem Solver", "#7C3AED")
            score >= 1400 -> ScoreRank(4, "Challenger", "#8B5CF6")
            score >= 800 -> ScoreRank(3, "Explorer", "#3B82F6")
            score >= 300 -> ScoreRank(2, "Learner", "#38BDF8")
            else -> ScoreRank(1, "Beginner", "#4ADE80")
        }
    }

    private fun MainTab.activeColor(): Int {
        val colorRes = when (this) {
            MainTab.HOME -> R.color.algoplay_blue
            MainTab.MATERI -> R.color.algoplay_green_dark
            MainTab.LATIHAN -> R.color.algoplay_red_dark
            MainTab.LEADERBOARD -> R.color.algoplay_gold_dark
            MainTab.PROFIL -> R.color.algoplay_purple
        }

        return ContextCompat.getColor(this@MainActivity, colorRes)
    }

    private fun MainTab.pageBackgroundRes(): Int {
        return when (this) {
            MainTab.HOME -> R.drawable.bg_tab_home
            MainTab.MATERI -> R.drawable.bg_tab_materi
            MainTab.LATIHAN -> R.drawable.bg_tab_latihan
            MainTab.LEADERBOARD -> R.drawable.bg_tab_leaderboard
            MainTab.PROFIL -> R.drawable.bg_tab_profile
        }
    }

    private fun MainTab.statusColorRes(): Int {
        return when (this) {
            MainTab.HOME -> R.color.algoplay_tab_home_top
            MainTab.MATERI -> R.color.algoplay_tab_materi_top
            MainTab.LATIHAN -> R.color.algoplay_tab_latihan_top
            MainTab.LEADERBOARD -> R.color.algoplay_tab_leaderboard_top
            MainTab.PROFIL -> R.color.algoplay_splash_dark
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    companion object {
        private const val KEY_COMPLETED_LESSONS = "completed_lessons"
        private const val KEY_COMPLETED_ACTIVITIES = "completed_activities"
        private const val KEY_FINAL_EXAM_SCORE = "final_exam_score"
        private const val KEY_PROFILE_PHOTO_URI = "profile_photo_uri"
        private const val KEY_LAST_CONTINUE_TYPE = "last_continue_type"
        private const val KEY_LAST_CONTINUE_KEY = "last_continue_key"
        private const val CONTINUE_MATERI = "materi"
        private const val CONTINUE_LATIHAN = "latihan"
        private const val PUZZLE_SYMBOL_KEY = "puzzle_symbol"
        private const val SEQUENCE_ORDER_KEY = "urut_langkah"
        private const val QUICK_QUIZ_KEY = "quiz_cepat"
        private const val CHALLENGE_KEY = "tantangan_harian"
        private const val FINAL_EXAM_LESSON_NUMBER = 12
        private const val LESSON_SCORE_REWARD = 100
        private const val CLOCK_REFRESH_MS = 30_000L
    }
}
