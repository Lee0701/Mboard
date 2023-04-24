package io.github.lee0701.mboard.settings

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import androidx.preference.Preference
import io.github.lee0701.mboard.R

class EnterKeyboardSettingsPreference(
    context: Context,
    attrs: AttributeSet?,
): Preference(context, attrs) {

    private val fileName: String

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.SliderPreference)
        fileName = a.getString(R.styleable.EnterKeyboardSettingsPreference_fileName) ?: "default.yaml"
        a.recycle()

        layoutResource = R.layout.preference_inline
    }

    override fun onClick() {
        super.onClick()
        val intent = Intent(context, KeyboardLayoutSettingsActivity::class.java)
        intent.putExtra("fileName", fileName)
        context.startActivity(intent)
    }
}