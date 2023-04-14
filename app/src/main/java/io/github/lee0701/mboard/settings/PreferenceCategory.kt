package io.github.lee0701.mboard.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.PreferenceCategory
import io.github.lee0701.mboard.R

class PreferenceCategory(
    context: Context,
    attrs: AttributeSet?,
): PreferenceCategory(context, attrs) {
    init {
        layoutResource = R.layout.preference_category
    }
}