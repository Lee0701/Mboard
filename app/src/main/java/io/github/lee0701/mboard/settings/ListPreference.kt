package io.github.lee0701.mboard.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference
import io.github.lee0701.mboard.R

class ListPreference(
    context: Context,
    attrs: AttributeSet?,
): ListPreference(context, attrs) {
    init {
        layoutResource = R.layout.preference_inline
    }
}