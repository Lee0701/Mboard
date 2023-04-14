package io.github.lee0701.mboard.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import io.github.lee0701.mboard.R

class SwitchPreference(
    context: Context,
    atts: AttributeSet?,
): SwitchPreferenceCompat(context, atts) {
    init {
        layoutResource = R.layout.preference_inline
    }
}