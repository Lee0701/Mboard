package io.github.lee0701.mboard.settings.preference

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import androidx.preference.Preference
import io.github.lee0701.mboard.BuildConfig
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.service.ImportExportActivity

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