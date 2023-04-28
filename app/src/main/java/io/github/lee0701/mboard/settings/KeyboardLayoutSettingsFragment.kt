package io.github.lee0701.mboard.settings

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.INVISIBLE
import androidx.recyclerview.widget.RecyclerView.VISIBLE
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.charleskorn.kaml.decodeFromStream
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.input.InputEngine
import io.github.lee0701.mboard.input.SoftInputEngine
import io.github.lee0701.mboard.module.InputEnginePreset
import io.github.lee0701.mboard.settings.KeyboardLayoutPreferenceDataStore.Companion.KEY_DEFAULT_HEIGHT
import io.github.lee0701.mboard.settings.KeyboardLayoutPreferenceDataStore.Companion.KEY_ENGINE_TYPE
import io.github.lee0701.mboard.settings.KeyboardLayoutPreferenceDataStore.Companion.KEY_HANJA_ADDITIONAL_DICTIONARIES
import io.github.lee0701.mboard.settings.KeyboardLayoutPreferenceDataStore.Companion.KEY_HANJA_CONVERSION
import io.github.lee0701.mboard.settings.KeyboardLayoutPreferenceDataStore.Companion.KEY_HANJA_PREDICTION
import io.github.lee0701.mboard.settings.KeyboardLayoutPreferenceDataStore.Companion.KEY_HANJA_SORT_BY_CONTEXT
import io.github.lee0701.mboard.settings.KeyboardLayoutPreferenceDataStore.Companion.KEY_INPUT_HEADER
import io.github.lee0701.mboard.settings.KeyboardLayoutPreferenceDataStore.Companion.KEY_LAYOUT_PRESET
import io.github.lee0701.mboard.settings.KeyboardLayoutPreferenceDataStore.Companion.KEY_ROW_HEIGHT
import io.github.lee0701.mboard.settings.KeyboardLayoutPreferenceDataStore.Companion.KEY_SHOW_CANDIDATES
import io.github.lee0701.mboard.settings.KeyboardLayoutSettingsActivity.Companion.emptyInputEngineListener
import java.io.File
import java.util.Collections
import kotlin.math.roundToInt

class KeyboardLayoutSettingsFragment(
    private val fileName: String,
    private val template: String,
): PreferenceFragmentCompat(),
    KeyboardLayoutPreferenceDataStore.OnChangeListener {
    private val handler: Handler = Handler(Looper.getMainLooper())

    private var preferenceDataStore: KeyboardLayoutPreferenceDataStore? = null

    private var screenMode: String = "mobile"
    private var keyboardViewType: String = "canvas"
    private var themeName: String = "theme_dynamic"

    private var previewMode: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.keyboard_layout_preferences, rootKey)
        val context = context ?: return
        val rootPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val screenMode = rootPreferences.getString("layout_screen_mode", "mobile") ?: "mobile"
        this.screenMode = screenMode

        val file = File(context.filesDir, fileName)
        if(!file.exists()) {
            file.outputStream().write(context.assets.open(template).readBytes())
        }

        keyboardViewType = rootPreferences.getString("appearance_keyboard_view_type", "canvas") ?: keyboardViewType
        themeName = rootPreferences.getString("appearance_theme", "theme_dynamic") ?: themeName
        val pref = KeyboardLayoutPreferenceDataStore(context, file, this)
        this.preferenceDataStore = pref
        preferenceManager.preferenceDataStore = pref

        val rootPreference = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val defaultHeightValue = rootPreference.getFloat("appearance_keyboard_height", 55f)

        val defaultHeight = findPreference<SwitchPreference>(KEY_DEFAULT_HEIGHT)
        val rowHeight = findPreference<SliderPreference>(KEY_ROW_HEIGHT)

        val engineType = findPreference<ListPreference>(KEY_ENGINE_TYPE)
        val layoutPreset = findPreference<ListPreference>(KEY_LAYOUT_PRESET)
        val inputHeader = findPreference<PreferenceCategory>(KEY_INPUT_HEADER)

        val showCandidates = findPreference<SwitchPreference>(KEY_SHOW_CANDIDATES)
        val hanjaConversion = findPreference<SwitchPreference>(KEY_HANJA_CONVERSION)
        val hanjaPrediction = findPreference<SwitchPreference>(KEY_HANJA_PREDICTION)
        val sortByContext = findPreference<SwitchPreference>(KEY_HANJA_SORT_BY_CONTEXT)
        val additionalDictionaries = findPreference<MultiSelectListPreference>(KEY_HANJA_ADDITIONAL_DICTIONARIES)

        fun updateByDefaultHeight(newValue: Any?) {
            val enabled = newValue != true
            defaultHeight?.isChecked = !enabled
            rowHeight?.isEnabled = enabled
            if(!enabled) rowHeight?.value = defaultHeightValue
        }
        defaultHeight?.setOnPreferenceChangeListener { _, newValue ->
            updateByDefaultHeight(newValue)
            true
        }
        updateByDefaultHeight(pref.getBoolean(KEY_DEFAULT_HEIGHT, true))

        fun updateByShowCandidates(newValue: Any?) {
            val enabled = newValue == true
            showCandidates?.isChecked = enabled
            hanjaConversion?.isEnabled = enabled
            hanjaPrediction?.isEnabled = enabled
            sortByContext?.isEnabled = enabled
            additionalDictionaries?.isEnabled = enabled

            showCandidates?.isChecked = preferenceDataStore?.getBoolean(KEY_SHOW_CANDIDATES, false) == true
            hanjaConversion?.isChecked = preferenceDataStore?.getBoolean(KEY_HANJA_CONVERSION, false) == true
            hanjaPrediction?.isChecked = preferenceDataStore?.getBoolean(KEY_HANJA_PREDICTION, false) == true
            sortByContext?.isChecked = preferenceDataStore?.getBoolean(KEY_HANJA_SORT_BY_CONTEXT, false) == true
            additionalDictionaries?.values = preferenceDataStore?.getStringSet(KEY_HANJA_ADDITIONAL_DICTIONARIES, mutableSetOf()) ?: mutableSetOf()
        }
        showCandidates?.setOnPreferenceChangeListener { _, newValue ->
            updateByShowCandidates(newValue)
            true
        }
        updateByShowCandidates(pref.getBoolean(KEY_SHOW_CANDIDATES, false))

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
        updateByEngineType(pref.getString(KEY_ENGINE_TYPE, "Latin"))
        engineType?.isVisible = false

        layoutPreset?.setOnPreferenceChangeListener { _, newValue ->
            if(newValue !is String) return@setOnPreferenceChangeListener true
            val newLayout = InputEnginePreset.yaml
                .decodeFromStream<InputEnginePreset>(requireContext().assets.open(newValue)).layout
            true
        }
        updateKeyboardView()
    }

    private fun updateKeyboardView() {
        val preset = preferenceDataStore?.preset?.commit() ?: return
        activity?.findViewById<FrameLayout>(R.id.preview_mode_frame)?.visibility = INVISIBLE
        activity?.findViewById<RecyclerView>(R.id.reorder_mode_recycler_view)?.visibility = INVISIBLE
        if(previewMode) updatePreviewMode(preset)
        else updateReorderMode(preset)
    }

    private fun updatePreviewMode(preset: InputEnginePreset) {
        val context = context ?: return
        val frame = activity?.findViewById<FrameLayout>(R.id.preview_mode_frame) ?: return
        val engine = mod(preset).inflate(context, emptyInputEngineListener)
        frame.removeAllViews()
        if(engine is SoftInputEngine) frame.addView(engine.initView(context))
        engine.onReset()
        frame.visibility = VISIBLE
    }

    private fun updateReorderMode(preset: InputEnginePreset) {
        val context = context ?: return
        val presets = preset.layout.softKeyboard.map { keyboard ->
            preset.copy(layout = preset.layout.copy(softKeyboard = listOf(keyboard))) }
            .toMutableList()
        val recyclerView = activity?.findViewById<RecyclerView>(R.id.reorder_mode_recycler_view)
        handler.post {
            val adapter = KeyboardLayoutPreviewAdapter(context)
            val touchHelper = ItemTouchHelper(TouchCallback { from, to ->
                Collections.swap(presets, from.adapterPosition, to.adapterPosition)
                adapter.notifyItemMoved(from.adapterPosition, to.adapterPosition)
                preferenceDataStore?.putKeyboards(presets.flatMap { it.layout.softKeyboard })
                true
            })
            adapter.onItemLongPress = { viewHolder ->
                touchHelper.startDrag(viewHolder)
            }
            adapter.onItemMenuPress = { type, viewHolder ->
                when(type) {
                    KeyboardLayoutPreviewAdapter.ItemMenuType.Remove -> {
                        val position = viewHolder.adapterPosition
                        presets.removeAt(position)
                        adapter.notifyItemRemoved(position)
                        preferenceDataStore?.putKeyboards(presets.flatMap { it.layout.softKeyboard })
                    }
                    KeyboardLayoutPreviewAdapter.ItemMenuType.MoveUp -> {
                        val position = viewHolder.adapterPosition
                        if(position - 1 in presets.indices) {
                            Collections.swap(presets, position, position - 1)
                            adapter.notifyItemMoved(position, position - 1)
                        }
                    }
                    KeyboardLayoutPreviewAdapter.ItemMenuType.MoveDown -> {
                        val position = viewHolder.adapterPosition
                        if(position + 1 in presets.indices) {
                            Collections.swap(presets, position, position + 1)
                            adapter.notifyItemMoved(position, position + 1)
                        }
                    }
                    else -> Unit
                }
            }
            recyclerView?.apply {
                this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                this.adapter = adapter
                touchHelper.attachToRecyclerView(this)
                adapter.submitList(presets)
                this.visibility = VISIBLE
            }
        }
    }

    override fun onChange(preset: InputEnginePreset) {
        val rootPreference = PreferenceManager.getDefaultSharedPreferences(context ?: return)
        preferenceDataStore?.write()
        rootPreference.edit().putBoolean("requested_restart", true).apply()
        rootPreference.edit().putBoolean("requested_restart", false).apply()
        updateKeyboardView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_keyboard_layout_setting, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val previewMode = menu.findItem(R.id.preview_mode)
        val changeOrdersMode = menu.findItem(R.id.reorder_mode)
        previewMode.isVisible = false
        changeOrdersMode.isVisible = false
        if(this.previewMode) changeOrdersMode.isVisible = true
        else previewMode.isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.preview_mode -> {
                previewMode = true
                updateKeyboardView()
                true
            }
            R.id.reorder_mode -> {
                previewMode = false
                updateKeyboardView()
                true
            }
            R.id.add_component -> {
                val dataStore = preferenceDataStore ?: return true
                val bottomSheet = ChooseNewComponentBottomSheetFragment(
                    types = listOf(ComponentType.NumberRow)
                ) { componentType ->
                    when(componentType) {
                        ComponentType.NumberRow -> {
                            dataStore.putKeyboards(modFilenames(listOf(NUMBER_ROW_ID)) +
                                    dataStore.preset.layout.softKeyboard)
                        }
                        ComponentType.TextSelection -> {
                        }
                        ComponentType.LanguageTab -> {
                        }
                    }
                    updateKeyboardView()
                }
                bottomSheet.show(childFragmentManager, ChooseNewComponentBottomSheetFragment.TAG)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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

    class TouchCallback(
        val onMove: (ViewHolder, ViewHolder) -> Boolean,
    ): ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: ViewHolder,
            target: ViewHolder
        ): Boolean {
            return onMove(viewHolder, target)
        }

        override fun onSwiped(viewHolder: ViewHolder, direction: Int) = Unit

        override fun isLongPressDragEnabled(): Boolean = false
    }

    enum class ComponentType(
        @DrawableRes val iconRes: Int,
        @StringRes val titleRes: Int,
    ) {
        NumberRow(R.drawable.baseline_123_24,
            R.string.pref_layout_component_number_row_title),
        TextSelection(R.drawable.baseline_text_select_move_forward_character,
            R.string.pref_layout_component_text_selection_title),
        LanguageTab(R.drawable.baseline_language_24,
            R.string.pref_layout_component_number_language_tab_title);
    }

    companion object {
        const val NUMBER_ROW_ID = "common/soft_%s_number.yaml"
    }
}