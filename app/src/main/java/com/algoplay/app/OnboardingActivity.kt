package com.algoplay.app

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class OnboardingActivity : AppCompatActivity() {

    private lateinit var imgAlbi: ImageView
    private lateinit var btnStartAdventure: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        imgAlbi = findViewById(R.id.imgAlbi)
        btnStartAdventure = findViewById(R.id.btnStartAdventure)

        animateAlbi()

        btnStartAdventure.setOnClickListener {
            FirebaseAuth.getInstance().currentUser?.uid?.let { uid ->
                getSharedPreferences(PREF_ONBOARDING, MODE_PRIVATE)
                    .edit()
                    .putBoolean("${KEY_ONBOARDING_PREFIX}$uid", true)
                    .apply()
            }
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun animateAlbi() {
        ObjectAnimator.ofFloat(imgAlbi, "translationY", 0f, -14f, 0f).apply {
            duration = 1600
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    companion object {
        private const val PREF_ONBOARDING = "algoplay_onboarding"
        private const val KEY_ONBOARDING_PREFIX = "seen_"
    }
}
