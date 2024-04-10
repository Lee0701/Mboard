package ee.oyatl.ime.f.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import ee.oyatl.ime.f.R

class Preference(
    context: Context,
    atts: AttributeSet?,
): Preference(context, atts) {
    init {
        layoutResource = R.layout.preference_inline
    }
}