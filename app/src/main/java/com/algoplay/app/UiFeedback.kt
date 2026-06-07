package com.algoplay.app

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

enum class AlgoSound {
    BENAR, SALAH, SELESAI, PILIH
}

private object AlgoSoundPool {
    private var soundPool: SoundPool? = null
    private val soundIds = mutableMapOf<AlgoSound, Int>()
    private val loadedSounds = mutableSetOf<Int>()

    fun preload(context: Context) {
        if (soundPool != null) return
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val pool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attributes)
            .build()
        soundPool = pool
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) loadedSounds.add(sampleId)
        }
        AlgoSound.values().forEach { effect ->
            soundIds[effect] = pool.load(context.applicationContext, effect.rawRes(), 1)
        }
    }

    fun play(context: Context, effect: AlgoSound) {
        preload(context)
        val pool = soundPool ?: return
        val soundId = soundIds[effect] ?: return
        if (loadedSounds.contains(soundId)) {
            pool.play(soundId, 0.86f, 0.86f, 1, 0, 1f)
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                if (loadedSounds.contains(soundId)) {
                    pool.play(soundId, 0.86f, 0.86f, 1, 0, 1f)
                }
            }, 80L)
        }
    }

    private fun AlgoSound.rawRes(): Int {
        return when (this) {
            AlgoSound.BENAR -> R.raw.benar
            AlgoSound.SALAH -> R.raw.salah
            AlgoSound.SELESAI -> R.raw.selesai
            AlgoSound.PILIH -> R.raw.pilih
        }
    }
}

fun Context.preloadAlgoSounds() {
    AlgoSoundPool.preload(this)
}

fun Context.playAlgoSound(effect: AlgoSound) {
    AlgoSoundPool.play(this, effect)
}

fun View.enableTapFeedback(pressedScale: Float = 0.97f): View {
    isClickable = true
    isFocusable = true

    val ripple = TypedValue()
    if (context.theme.resolveAttribute(android.R.attr.selectableItemBackground, ripple, true)) {
        foreground = ContextCompat.getDrawable(context, ripple.resourceId)
    }

    setOnTouchListener { view, event ->
        if (!view.isEnabled) return@setOnTouchListener false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> view.animate()
                .scaleX(pressedScale)
                .scaleY(pressedScale)
                .setDuration(70L)
                .start()

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(90L)
                .start()
        }
        false
    }
    return this
}

fun Context.showBriefResultPopup(
    title: String,
    message: String,
    @DrawableRes imageRes: Int,
    success: Boolean,
    soundEffect: AlgoSound? = if (success) AlgoSound.BENAR else AlgoSound.SALAH,
    durationMs: Long = 820L
) {
    val activity = this as? Activity
    if (activity?.isFinishing == true || activity?.isDestroyed == true) return
    soundEffect?.let { playAlgoSound(it) }

    val dialog = Dialog(this)
    dialog.setCancelable(false)

    val root = FrameLayout(this).apply {
        setPadding(dp(28), 0, dp(28), 0)
    }
    val card = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        background = roundedDialogDrawable(ContextCompat.getColor(this@showBriefResultPopup, R.color.white), dp(24))
        elevation = dp(10).toFloat()
        setPadding(dp(18), dp(16), dp(18), dp(16))
        alpha = 0f
        scaleX = 0.9f
        scaleY = 0.9f
    }
    root.addView(
        card,
        FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        )
    )

    card.addView(ImageView(this).apply {
        setImageResource(imageRes)
        adjustViewBounds = true
        scaleType = ImageView.ScaleType.FIT_CENTER
    }, LinearLayout.LayoutParams(dp(104), dp(104)))

    card.addView(TextView(this).apply {
        text = title
        textSize = 20f
        setTypeface(null, Typeface.BOLD)
        setTextColor(ContextCompat.getColor(this@showBriefResultPopup, R.color.algoplay_text))
        gravity = Gravity.CENTER
        includeFontPadding = false
    }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
        topMargin = dp(8)
    })

    card.addView(TextView(this).apply {
        text = message
        textSize = 13f
        setTextColor(ContextCompat.getColor(this@showBriefResultPopup, R.color.algoplay_subtext))
        gravity = Gravity.CENTER
        includeFontPadding = false
        setLineSpacing(dp(2).toFloat(), 1f)
    }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
        topMargin = dp(7)
    })

    val accent = ContextCompat.getColor(this, if (success) R.color.algoplay_green_dark else R.color.algoplay_red_dark)
    card.addView(View(this).apply {
        background = roundedDialogDrawable(accent, dp(999))
    }, LinearLayout.LayoutParams(dp(74), dp(5)).apply {
        topMargin = dp(14)
    })

    dialog.setContentView(root)
    dialog.setOnShowListener {
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setDimAmount(0.22f)
        dialog.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        card.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(210L)
            .setInterpolator(OvershootInterpolator(1.04f))
            .start()
        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) dialog.dismiss()
        }, durationMs)
    }
    dialog.show()
}

private fun Context.dp(value: Int): Int {
    return (value * resources.displayMetrics.density).toInt()
}

private fun roundedDialogDrawable(color: Int, radius: Int): GradientDrawable {
    return GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = radius.toFloat()
        setColor(color)
    }
}
