package io.github.lee0701.mboard.settings.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.SwitchPreferenceCompat
import io.github.lee0701.mboard.R

class SwitchPreference(
    context: Context,
    attrs: AttributeSet?,
): SwitchPreferenceCompat(context, attrs) {
    init {
        layoutResource = R.layout.preference_inline
    }

}