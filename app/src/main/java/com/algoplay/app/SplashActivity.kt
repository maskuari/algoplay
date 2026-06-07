package com.algoplay.app

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.ActivityOptions
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var txtSplashTitle: TextView
    private lateinit var txtSplashSubtitle: TextView
    private lateinit var txtSplashVersion: TextView
    private lateinit var splashDotOne: View
    private lateinit var splashDotTwo: View
    private lateinit var splashDotThree: View
    private var openSoundPlayer: MediaPlayer? = null
    private val loadingAnimators = mutableListOf<ObjectAnimator>()
    private val splashHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()
        txtSplashTitle = findViewById(R.id.txtSplashTitle)
        txtSplashSubtitle = findViewById(R.id.txtSplashSubtitle)
        txtSplashVersion = findViewById(R.id.txtSplashVersion)
        splashDotOne = findViewById(R.id.splashDotOne)
        splashDotTwo = findViewById(R.id.splashDotTwo)
        splashDotThree = findViewById(R.id.splashDotThree)

        playOpenSound()
        animateSplashText()

        splashHandler.postDelayed({ openNextScreen() }, SPLASH_DURATION_MS)
    }

    override fun onDestroy() {
        splashHandler.removeCallbacksAndMessages(null)
        loadingAnimators.forEach { it.cancel() }
        openSoundPlayer?.release()
        openSoundPlayer = null
        super.onDestroy()
    }

    private fun hasSeenOnboarding(uid: String): Boolean {
        return getSharedPreferences(PREF_ONBOARDING, MODE_PRIVATE)
            .getBoolean("${KEY_ONBOARDING_PREFIX}$uid", false)
    }

    private fun animateSplashText() {
        txtSplashTitle.alpha = 0f
        txtSplashTitle.translationX = -110f
        txtSplashSubtitle.alpha = 0f
        txtSplashSubtitle.translationX = -88f
        txtSplashVersion.alpha = 0f
        splashDotOne.alpha = 0.25f
        splashDotTwo.alpha = 0.25f
        splashDotThree.alpha = 0.25f

        txtSplashTitle.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(860)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        txtSplashSubtitle.animate()
            .alpha(1f)
            .translationX(0f)
            .setStartDelay(180)
            .setDuration(820)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        txtSplashVersion.animate()
            .alpha(1f)
            .setStartDelay(320)
            .setDuration(650)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        animateLoadingDot(splashDotOne, 0L)
        animateLoadingDot(splashDotTwo, 150L)
        animateLoadingDot(splashDotThree, 300L)
    }

    private fun animateLoadingDot(dot: View, delay: Long) {
        val animator = ObjectAnimator.ofFloat(dot, View.TRANSLATION_Y, 0f, -9f, 0f).apply {
            duration = 760L
            startDelay = delay
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        loadingAnimators.add(animator)
        dot.animate()
            .alpha(1f)
            .setStartDelay(delay)
            .setDuration(360L)
            .start()
    }

    private fun playOpenSound() {
        openSoundPlayer = MediaPlayer.create(this, R.raw.openalgoplay)?.apply {
            setVolume(0.9f, 0.9f)
            setOnCompletionListener {
                it.release()
                if (openSoundPlayer === it) openSoundPlayer = null
            }
            start()
        }
    }

    private fun openNextScreen() {
        val currentUser = auth.currentUser
        val nextScreen = if (currentUser != null) {
            if (hasSeenOnboarding(currentUser.uid)) MainActivity::class.java else OnboardingActivity::class.java
        } else {
            LoginActivity::class.java
        }
        openSoundPlayer?.release()
        openSoundPlayer = null
        val options = ActivityOptions.makeCustomAnimation(
            this,
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )
        startActivity(Intent(this, nextScreen), options.toBundle())
        finish()
    }

    companion object {
        private const val PREF_ONBOARDING = "algoplay_onboarding"
        private const val KEY_ONBOARDING_PREFIX = "seen_"
        private const val SPLASH_DURATION_MS = 5_000L
    }
}
