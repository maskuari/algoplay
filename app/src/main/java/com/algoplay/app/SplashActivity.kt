package com.algoplay.app

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var txtSplashTitle: TextView
    private lateinit var txtSplashVersion: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()
        txtSplashTitle = findViewById(R.id.txtSplashTitle)
        txtSplashVersion = findViewById(R.id.txtSplashVersion)

        animateSplashText()

        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = auth.currentUser
            val nextScreen = if (currentUser != null) {
                if (hasSeenOnboarding(currentUser.uid)) MainActivity::class.java else OnboardingActivity::class.java
            } else {
                LoginActivity::class.java
            }
            val options = ActivityOptions.makeCustomAnimation(
                this,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            startActivity(Intent(this, nextScreen), options.toBundle())
            finish()
        }, 2800)
    }

    private fun hasSeenOnboarding(uid: String): Boolean {
        return getSharedPreferences(PREF_ONBOARDING, MODE_PRIVATE)
            .getBoolean("${KEY_ONBOARDING_PREFIX}$uid", false)
    }

    private fun animateSplashText() {
        txtSplashTitle.alpha = 0f
        txtSplashTitle.translationY = 18f
        txtSplashVersion.alpha = 0f

        txtSplashTitle.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(780)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        txtSplashVersion.animate()
            .alpha(1f)
            .setStartDelay(320)
            .setDuration(650)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    companion object {
        private const val PREF_ONBOARDING = "algoplay_onboarding"
        private const val KEY_ONBOARDING_PREFIX = "seen_"
    }
}
