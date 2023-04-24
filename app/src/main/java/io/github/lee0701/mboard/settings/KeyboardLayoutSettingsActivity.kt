package io.github.lee0701.mboard.settings

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.input.Candidate
import io.github.lee0701.mboard.input.InputEngine
import io.github.lee0701.mboard.input.SoftInputEngine
import io.github.lee0701.mboard.module.InputEnginePreset
import io.github.lee0701.mboard.view.keyboard.FlickDirection
import io.github.lee0701.mboard.view.keyboard.KeyboardListener
import java.io.File
import kotlin.math.roundToInt

class KeyboardLayoutSettingsActivity: AppCompatActivity(),
    KeyboardLayoutPreferenceDataStore.OnChangeListener {
    private val handler: Handler = Handler(Looper.getMainLooper())

    private var keyboardViewType: String = "canvas"
    private var themeName: String = "theme_dynamic"

    var previewView: View? = null
    lateinit var preferenceDataStore: KeyboardLayoutPreferenceDataStore

    private val emptyKeyboardListener = object: KeyboardListener {
        override fun onKeyClick(code: Int, output: String?) = Unit
        override fun onKeyLongClick(code: Int, output: String?) = Unit
        override fun onKeyDown(code: Int, output: String?) = Unit
        override fun onKeyUp(code: Int, output: String?) = Unit
        override fun onKeyFlick(direction: FlickDirection, code: Int, output: String?) = Unit
    }

    private val emptyInputEngineListener = object: InputEngine.Listener {
        override fun onComposingText(text: CharSequence) = Unit
        override fun onFinishComposing() = Unit
        override fun onCommitText(text: CharSequence) = Unit
        override fun onDeleteText(beforeLength: Int, afterLength: Int) = Unit
        override fun onCandidates(list: List<Candidate>) = Unit
        override fun onSystemKey(code: Int): Boolean = false
        override fun onEditorAction(code: Int) = Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)

        val fileName = intent.getStringExtra("fileName") ?: "default.yaml"
        val file = File(filesDir, fileName)
        if(!file.exists()) {
            val input = assets.open("preset/preset_mobile_3set_391_strict.yaml")
            file.outputStream().write(input.readBytes())
        }

        keyboardViewType = preferences.getString("appearance_keyboard_view_type", "canvas") ?: keyboardViewType
        themeName = preferences.getString("appearance_theme", "theme_dynamic") ?: themeName

        preferenceDataStore = KeyboardLayoutPreferenceDataStore(this, file, this)
        setContentView(R.layout.activity_keyboard_layout_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, KeyboardSettingsFragment(file, preferenceDataStore))
            .commit()
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onDestroy() {
        preferenceDataStore.write()
        super.onDestroy()
    }

    private fun updateKeyboardView(preset: InputEnginePreset) {
        val engine = mod(preset).inflate(this, emptyInputEngineListener)
        val view = if(engine is SoftInputEngine) engine.initView(this) else null
        this.previewView = view
        handler.post {
            this.findViewById<FrameLayout>(R.id.preview_wrapper)?.apply {
                removeAllViews()
                addView(view)
            }
        }
    }

    override fun onChange(preset: InputEnginePreset) {
        updateKeyboardView(preset)
    }

    private fun mod(preset: InputEnginePreset): InputEnginePreset {
        return when(preset) {
            is InputEnginePreset.Latin -> preset.copy(rowHeight = modHeight(preset.rowHeight))
            is InputEnginePreset.Hangul -> preset.copy(rowHeight = modHeight(preset.rowHeight))
            else -> preset
        }
    }

    private fun modHeight(height: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height.toFloat(), resources.displayMetrics).roundToInt()
    }

    class KeyboardSettingsFragment(
        private val file: File,
        private val preferenceDataStore: KeyboardLayoutPreferenceDataStore,
    ): PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.preferenceDataStore = preferenceDataStore
            setPreferencesFromResource(R.xml.keyboard_layout_preferences, rootKey)
            val rootPreference = PreferenceManager.getDefaultSharedPreferences(requireContext())

            val defaultHeight = findPreference<SwitchPreference>("default_height")
            val rowHeight = findPreference<SliderPreference>("row_height")
            defaultHeight?.setOnPreferenceChangeListener { preference, newValue ->
                if(newValue == true) {
                    rowHeight?.isEnabled = false
                    rowHeight?.setValue(rootPreference.getFloat("appearance_keyboard_height", 55f))
                } else {
                    rowHeight?.isEnabled = true
                }
                true
            }
        }
    }
}