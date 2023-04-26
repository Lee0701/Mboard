package io.github.lee0701.mboard.settings

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.charleskorn.kaml.decodeFromStream
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.input.InputEngine
import io.github.lee0701.mboard.input.SoftInputEngine
import io.github.lee0701.mboard.module.InputEnginePreset
import java.io.File
import kotlin.math.roundToInt

class KeyboardLayoutSettingsFragment(
    private val fileName: String,
    private val template: String,
): PreferenceFragmentCompat(),
    KeyboardLayoutPreferenceDataStore.OnChangeListener {
    private val handler: Handler = Handler(Looper.getMainLooper())

    private var screenMode: String = "mobile"
    private var keyboardViewType: String = "canvas"
    private var themeName: String = "theme_dynamic"

    var previewView: View? = null
    var inputEngine: InputEngine? = null
    var preferenceDataStore: KeyboardLayoutPreferenceDataStore? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.keyboard_layout_preferences, rootKey)
        val context = context ?: return
        val rootPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val screenMode = rootPreferences.getString("layout_screen_mode", "mobile") ?: "mobile"
        this.screenMode = screenMode

        val file = File(context.filesDir, fileName)
        if(!file.exists()) {
            file.outputStream().write(context.assets.open(template).readBytes())
        }

        keyboardViewType = rootPreferences.getString("appearance_keyboard_view_type", "canvas") ?: keyboardViewType
        themeName = rootPreferences.getString("appearance_theme", "theme_dynamic") ?: themeName
        val preferenceDataStore = KeyboardLayoutPreferenceDataStore(context, file, this)
        this.preferenceDataStore = preferenceDataStore
        preferenceManager.preferenceDataStore = preferenceDataStore

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
        updateByDefaultHeight(preferenceDataStore.getBoolean(KeyboardLayoutPreferenceDataStore.KEY_DEFAULT_HEIGHT, true))

        val engineType = findPreference<ListPreference>(KeyboardLayoutPreferenceDataStore.KEY_ENGINE_TYPE)
        val layoutPreset = findPreference<ListPreference>(KeyboardLayoutPreferenceDataStore.KEY_LAYOUT_PRESET)
        val inputHeader = findPreference<PreferenceCategory>(KeyboardLayoutPreferenceDataStore.KEY_INPUT_HEADER)

        fun updateByEngineType(newValue: Any?) {
            inputHeader?.isVisible = newValue == InputEnginePreset.Type.Hangul.name
            val (entries, values) = when(newValue) {
                InputEnginePreset.Type.Hangul.name -> {
                    R.array.preset_hangul_entries to R.array.preset_hangul_values
                }
                InputEnginePreset.Type.Latin.name -> {
                    R.array.preset_latin_entries to R.array.preset_latin_values
                }
                InputEnginePreset.Type.Symbol.name -> {
                    R.array.preset_symbol_entries to R.array.preset_symbol_values
                }
                else -> return
            }
            layoutPreset?.setEntries(entries)
            layoutPreset?.setEntryValues(values)
        }
        engineType?.setOnPreferenceChangeListener { _, newValue ->
            updateByEngineType(newValue)
            layoutPreset?.setValueIndex(0)
            true
        }
        updateByEngineType(preferenceDataStore.getString(KeyboardLayoutPreferenceDataStore.KEY_ENGINE_TYPE, "Latin"))
        engineType?.isVisible = false

        layoutPreset?.setOnPreferenceChangeListener { _, newValue ->
            if(newValue !is String) return@setOnPreferenceChangeListener true
            val newLayout = InputEnginePreset.yaml
                .decodeFromStream<InputEnginePreset>(requireContext().assets.open(newValue)).layout
            true
        }
    }

    private fun updateKeyboardView(preset: InputEnginePreset) {
        val context = context ?: return
        val engine = mod(preset).inflate(context,
            KeyboardLayoutSettingsActivity.emptyInputEngineListener
        )
        val view = if(engine is SoftInputEngine) engine.initView(context) else null
        this.inputEngine = engine
        this.previewView = view
        handler.post {
            val adapter = KeyboardLayoutPreviewAdapter(context)
            activity?.findViewById<RecyclerView>(R.id.preview_recycler_view)?.apply {
                this.adapter = adapter
                this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            }
            adapter.submitList(listOf(preset))
        }
    }

    override fun onChange(preset: InputEnginePreset) {
        val rootPreference = PreferenceManager.getDefaultSharedPreferences(context ?: return)
        preferenceDataStore?.write()
        rootPreference.edit().putBoolean("requested_restart", true).apply()
        rootPreference.edit().putBoolean("requested_restart", false).apply()
        inputEngine?.onReset()
        updateKeyboardView(preset)
    }

    private fun mod(preset: InputEnginePreset): InputEnginePreset {
        return preset.copy(
            layout = modLayout(preset.layout),
            size = InputEnginePreset.Size(rowHeight = modHeight(preset.size.rowHeight)),
        )
    }

    private fun modLayout(layout: InputEnginePreset.Layout): InputEnginePreset.Layout {
        return layout.copy(
            softKeyboard = modFilenames(layout.softKeyboard),
            moreKeysTable = modFilenames(layout.moreKeysTable),
            codeConvertTable = modFilenames(layout.codeConvertTable),
            combinationTable = modFilenames(layout.combinationTable),
        )
    }

    private fun modFilenames(fileNames: List<String>): List<String> {
        return fileNames.map { it.format(screenMode) }
    }

    private fun modHeight(height: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height.toFloat(), resources.displayMetrics).roundToInt()
    }

}