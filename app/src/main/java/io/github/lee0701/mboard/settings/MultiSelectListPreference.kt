package io.github.lee0701.mboard.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.MultiSelectListPreference
import io.github.lee0701.mboard.R

class MultiSelectListPreference(
    context: Context,
    atts: AttributeSet?,
): MultiSelectListPreference(context, atts) {
    init {
        layoutResource = R.layout.preference_inline
    }
}