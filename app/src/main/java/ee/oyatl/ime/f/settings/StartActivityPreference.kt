package ee.oyatl.ime.f.settings

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import androidx.preference.Preference
import ee.oyatl.ime.f.BuildConfig
import ee.oyatl.ime.f.R
import ee.oyatl.ime.f.service.ImportExportActivity

class StartActivityPreference(
    context: Context,
    attrs: AttributeSet?,
): Preference(context, attrs) {
    init {
        layoutResource = R.layout.preference_inline
    }
    override fun onClick() {
        if(BuildConfig.DEBUG) {
            val intent = Intent(context, ImportExportActivity::class.java)
            context.startActivity(intent)
        }
    }
}