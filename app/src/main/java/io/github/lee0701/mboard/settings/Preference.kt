package io.github.lee0701.mboard.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import io.github.lee0701.mboard.R

class Preference(
    context: Context,
    atts: AttributeSet?,
): Preference(context, atts) {
    init {
        layoutResource = R.layout.preference_inline
    }
}