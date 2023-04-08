package io.github.lee0701.mboard.settings

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import androidx.preference.Preference
import io.github.lee0701.mboard.service.ImportExportActivity

class StartActivityPreference(
    context: Context,
    attrs: AttributeSet?,
): Preference(context, attrs) {
    override fun onClick() {
        val intent = Intent(context, ImportExportActivity::class.java)
        context.startActivity(intent)
    }
}