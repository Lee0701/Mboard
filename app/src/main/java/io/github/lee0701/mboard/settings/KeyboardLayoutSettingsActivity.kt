package io.github.lee0701.mboard.settings

import android.annotation.SuppressLint
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
import io.github.lee0701.mboard.settings.KeyboardLayoutPreferenceDataStore.Companion.KEY_DEFAULT_HEIGHT
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
    var inputEngine: InputEngine? = null
    var preferenceDataStore: KeyboardLayoutPreferenceDataStore? = null

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
        val rootPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val fileName = intent.getStringExtra("fileName") ?: "default.yaml"
        val file = File(filesDir, fileName)
        if(!file.exists()) {
            val input = assets.open("preset/arst.yaml")
            file.outputStream().write(input.readBytes())
        }

        keyboardViewType = rootPreferences.getString("appearance_keyboard_view_type", "canvas") ?: keyboardViewType
        themeName = rootPreferences.getString("appearance_theme", "theme_dynamic") ?: themeName

        val preferenceDataStore = KeyboardLayoutPreferenceDataStore(this, file, this)
        setContentView(R.layout.activity_keyboard_layout_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, KeyboardSettingsFragment(preferenceDataStore))
            .commit()
        supportActionBar?.setDisplayShowHomeEnabled(true)
        this.preferenceDataStore = preferenceDataStore
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun updateKeyboardView(preset: InputEnginePreset) {
        val engine = mod(preset).inflate(this, emptyInputEngineListener)
        val view = if(engine is SoftInputEngine) engine.initView(this) else null
        this.inputEngine = engine
        this.previewView = view
        handler.post {
            this.findViewById<FrameLayout>(R.id.preview_wrapper)?.apply {
                removeAllViews()
                addView(view)
            }
        }
    }

    @SuppressLint("ApplySharedPref")
    override fun onChange(preset: InputEnginePreset) {
        val rootPreference = PreferenceManager.getDefaultSharedPreferences(this)
        preferenceDataStore?.write()
        rootPreference.edit().putBoolean("requested_restart", true).apply()
        rootPreference.edit().putBoolean("requested_restart", false).apply()
        updateKeyboardView(preset)
        inputEngine?.onReset()
    }

    private fun mod(preset: InputEnginePreset): InputEnginePreset {
        return preset.copy(
            size = InputEnginePreset.Size(rowHeight = modHeight(preset.size.rowHeight)),
        )
    }

    private fun modHeight(height: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height.toFloat(), resources.displayMetrics).roundToInt()
    }

    class KeyboardSettingsFragment(
        private val preferenceDataStore: KeyboardLayoutPreferenceDataStore,
    ): PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.preferenceDataStore = preferenceDataStore
            setPreferencesFromResource(R.xml.keyboard_layout_preferences, rootKey)

            val rootPreference = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val defaultHeightValue = rootPreference.getFloat("appearance_keyboard_height", 55f)

            val defaultHeight = findPreference<SwitchPreference>(KeyboardLayoutPreferenceDataStore.KEY_DEFAULT_HEIGHT)
            val rowHeight = findPreference<SliderPreference>(KeyboardLayoutPreferenceDataStore.KEY_ROW_HEIGHT)

            fun updateByDefaultHeight(newValue: Any?) {
                rowHeight?.isEnabled = newValue == false
                if(newValue == true) preferenceDataStore.putFloat(KeyboardLayoutPreferenceDataStore.KEY_ROW_HEIGHT, defaultHeightValue)
            }
            defaultHeight?.setOnPreferenceChangeListener { _, newValue ->
                updateByDefaultHeight(newValue)
                true
            }
            updateByDefaultHeight(preferenceDataStore.getBoolean(KEY_DEFAULT_HEIGHT, true))

            val engineType = findPreference<ListPreference>(KeyboardLayoutPreferenceDataStore.KEY_ENGINE_TYPE)
            val hangulHeader = findPreference<PreferenceCategory>(KeyboardLayoutPreferenceDataStore.KEY_ENGINE_TYPE_HANGUL_HEADER)
            val mainLayout = findPreference<ListPreference>(KeyboardLayoutPreferenceDataStore.KEY_MAIN_LAYOUT)

            fun updateByEngineType(newValue: Any?) {
                hangulHeader?.isEnabled = newValue == InputEnginePreset.Type.Hangul.name
                val (entries, values) =
                    if(newValue == InputEnginePreset.Type.Hangul.name)
                        R.array.main_layout_hangul_entries to R.array.main_layout_hangul_values
                    else R.array.main_layout_latin_entries to R.array.main_layout_latin_values
                mainLayout?.setEntries(entries)
                mainLayout?.setEntryValues(values)
                mainLayout?.setValueIndex(0)
            }
            engineType?.setOnPreferenceChangeListener { _, newValue ->
                updateByEngineType(newValue)
                true
            }
            updateByEngineType(engineType?.value)
        }
    }
}