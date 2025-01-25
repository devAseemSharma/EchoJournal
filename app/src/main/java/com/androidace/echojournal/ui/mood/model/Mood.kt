package com.androidace.echojournal.ui.mood.model

import androidx.annotation.DrawableRes
import com.androidace.echojournal.R

enum class Mood(
    val displayName: String,
    @DrawableRes val activeResId: Int,
    @DrawableRes val inactiveResId: Int
) {
    STRESSED("Stressed", R.drawable.stress_active, R.drawable.stress_inactive, ),
    SAD("Sad", R.drawable.sad_active, R.drawable.sad_inactive),
    NEUTRAL("Neutral", R.drawable.neutral_active, R.drawable.neutral_inactive),
    PEACEFUL("Peaceful", R.drawable.peaceful_active, R.drawable.peaceful_inactive),
    EXCITED("Excited", R.drawable.excited_active, R.drawable.excited_inactive)
}