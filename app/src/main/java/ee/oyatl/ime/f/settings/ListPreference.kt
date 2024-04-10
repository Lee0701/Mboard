package ee.oyatl.ime.f.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference
import ee.oyatl.ime.f.R

class ListPreference(
    context: Context,
    attrs: AttributeSet?,
): ListPreference(context, attrs) {
    init {
        layoutResource = R.layout.preference_inline
    }
}