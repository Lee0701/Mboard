package io.github.lee0701.mboard.settings.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.MultiSelectListPreference
import io.github.lee0701.mboard.R

class MultiSelectListPreference(
    context: Context,
    attrs: AttributeSet?,
): MultiSelectListPreference(context, attrs) {
    init {
        layoutResource = R.layout.preference_inline
    }
}