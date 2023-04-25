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
import com.charleskorn.kaml.decodeFromStream
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.input.Candidate
import io.github.lee0701.mboard.input.InputEngine
import io.github.lee0701.mboard.input.SoftInputEngine
import io.github.lee0701.mboard.module.InputEnginePreset
import io.github.lee0701.mboard.settings.KeyboardLayoutPreferenceDataStore.Companion.KEY_DEFAULT_HEIGHT
import io.github.lee0701.mboard.settings.KeyboardLayoutPreferenceDataStore.Companion.KEY_ENGINE_TYPE
import io.github.lee0701.mboard.settings.KeyboardLayoutPreferenceDataStore.Companion.KEY_ENGINE_TYPE_HANGUL_HEADER
import io.github.lee0701.mboard.settings.KeyboardLayoutPreferenceDataStore.Companion.KEY_LAYOUT_PRESET
import io.github.lee0701.mboard.settings.KeyboardLayoutPreferenceDataStore.Companion.KEY_ROW_HEIGHT
import io.github.lee0701.mboard.view.keyboard.FlickDirection
import io.github.lee0701.mboard.view.keyboard.KeyboardListener
import java.io.File
import kotlin.math.roundToInt

class KeyboardLayoutSettingsActivity: AppCompatActivity() {

    private val fileName: String by lazy { intent.getStringExtra("fileName") ?: "default.yaml" }
    private val template: String by lazy { intent.getStringExtra("template") ?: "default.yaml" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_keyboard_layout_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, KeyboardSettingsFragment(fileName, template))
            .commit()
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
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
    }

    class KeyboardSettingsFragment(
        val fileName: String,
        val template: String,
    ): PreferenceFragmentCompat(),
        KeyboardLayoutPreferenceDataStore.OnChangeListener {
        private val handler: Handler = Handler(Looper.getMainLooper())

        private var keyboardViewType: String = "canvas"
        private var themeName: String = "theme_dynamic"

        var previewView: View? = null
        var inputEngine: InputEngine? = null
        var preferenceDataStore: KeyboardLayoutPreferenceDataStore? = null

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val context = context ?: return
            val rootPreferences = PreferenceManager.getDefaultSharedPreferences(context)

            val file = File(context.filesDir, fileName)
            if(!file.exists()) {
                val input = context.assets.open(template)
                file.outputStream().write(input.readBytes())
            }

            keyboardViewType = rootPreferences.getString("appearance_keyboard_view_type", "canvas") ?: keyboardViewType
            themeName = rootPreferences.getString("appearance_theme", "theme_dynamic") ?: themeName
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            val context = context ?: return
            val file = File(context.filesDir, fileName)
            val preferenceDataStore = KeyboardLayoutPreferenceDataStore(context, file, this)
            this.preferenceDataStore = preferenceDataStore
            preferenceManager.preferenceDataStore = preferenceDataStore
            setPreferencesFromResource(R.xml.keyboard_layout_preferences, rootKey)

            val rootPreference = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val defaultHeightValue = rootPreference.getFloat("appearance_keyboard_height", 55f)

            val defaultHeight = findPreference<SwitchPreference>(KEY_DEFAULT_HEIGHT)
            val rowHeight = findPreference<SliderPreference>(KEY_ROW_HEIGHT)

            fun updateByDefaultHeight(newValue: Any?) {
                rowHeight?.isEnabled = newValue == false
                if(newValue == true) preferenceDataStore.putFloat(KEY_ROW_HEIGHT, defaultHeightValue)
            }
            defaultHeight?.setOnPreferenceChangeListener { _, newValue ->
                updateByDefaultHeight(newValue)
                true
            }
            updateByDefaultHeight(preferenceDataStore.getBoolean(KEY_DEFAULT_HEIGHT, true))

            val engineType = findPreference<ListPreference>(KEY_ENGINE_TYPE)
            val layoutPreset = findPreference<ListPreference>(KEY_LAYOUT_PRESET)
            val hangulHeader = findPreference<PreferenceCategory>(KEY_ENGINE_TYPE_HANGUL_HEADER)
//            val mainLayout = findPreference<ListPreference>(KeyboardLayoutPreferenceDataStore.KEY_MAIN_LAYOUT)

            fun updateByEngineType(newValue: Any?) {
                hangulHeader?.isEnabled = newValue == InputEnginePreset.Type.Hangul.name
                val (entries, values) =
                    if(newValue == InputEnginePreset.Type.Hangul.name)
                        R.array.main_layout_hangul_entries to R.array.main_layout_hangul_values
                    else R.array.main_layout_latin_entries to R.array.main_layout_latin_values
//                mainLayout?.setEntries(entries)
//                mainLayout?.setEntryValues(values)
//                mainLayout?.setValueIndex(0)
            }
            engineType?.setOnPreferenceChangeListener { _, newValue ->
                updateByEngineType(newValue)
                true
            }
            updateByEngineType(engineType?.value)

            layoutPreset?.setOnPreferenceChangeListener { _, newValue ->
                if(newValue !is String) return@setOnPreferenceChangeListener true
                val preset = InputEnginePreset.yaml.decodeFromStream<InputEnginePreset>(requireContext().assets.open(newValue))

                true
            }
        }

        private fun updateKeyboardView(preset: InputEnginePreset) {
            val activity = activity ?: return
            val engine = mod(preset).inflate(activity, emptyInputEngineListener)
            val view = if(engine is SoftInputEngine) engine.initView(activity) else null
            this.inputEngine = engine
            this.previewView = view
            handler.post {
                activity.findViewById<FrameLayout>(R.id.preview_wrapper)?.apply {
                    removeAllViews()
                    addView(view)
                }
            }
        }

        override fun onChange(preset: InputEnginePreset) {
            val rootPreference = PreferenceManager.getDefaultSharedPreferences(context ?: return)
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

    }
}