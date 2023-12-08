package io.github.lee0701.mboard.settings.preference

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import androidx.preference.Preference
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.settings.KeyboardLayoutSettingsActivity

class EnterKeyboardSettingsPreference(
    context: Context,
    attrs: AttributeSet?,
): Preference(context, attrs) {

    private val fileName: String
    private val template: String

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.EnterKeyboardSettingsPreference)
        fileName = a.getString(R.styleable.EnterKeyboardSettingsPreference_fileName) ?: "default.yaml"
        template = a.getString(R.styleable.EnterKeyboardSettingsPreference_template) ?: "default.yaml"
        a.recycle()
        layoutResource = R.layout.preference_inline
    }

    override fun onClick() {
        super.onClick()
        val intent = Intent(context, KeyboardLayoutSettingsActivity::class.java)
        intent.putExtra("fileName", fileName)
        intent.putExtra("template", template)
        context.startActivity(intent)
    }
}