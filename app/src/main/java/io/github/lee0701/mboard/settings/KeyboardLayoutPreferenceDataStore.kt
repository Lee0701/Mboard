package io.github.lee0701.mboard.settings

import android.content.Context
import androidx.preference.PreferenceDataStore
import com.charleskorn.kaml.decodeFromStream
import com.charleskorn.kaml.encodeToStream
import io.github.lee0701.mboard.module.InputEnginePreset
import java.io.File

class KeyboardLayoutPreferenceDataStore(
    val context: Context,
    val file: File,
    val onChangeListener: OnChangeListener,
): PreferenceDataStore() {

    constructor(
        context: Context,
        file: File,
        onChange: () -> Unit,
    ): this(context, file, object: OnChangeListener {
        override fun onChange(preset: InputEnginePreset) {
            onChange()
        }
    })

    private val preset: InputEnginePreset.Mutable =
        InputEnginePreset.yaml.decodeFromStream<InputEnginePreset>(file.inputStream()).mutable()

    init {
        update()
    }

    override fun putString(key: String?, value: String?) {
        when(key) {
            KEY_ENGINE_TYPE -> preset.type = InputEnginePreset.Type.valueOf(value ?: "Latin")
        }
        update()
    }

    override fun putStringSet(key: String?, values: MutableSet<String>?) {
        when(key) {
            KEY_HANJA_ADDITIONAL_DICTIONARIES -> preset.hanjaAdditionalDictionaries = values ?: mutableSetOf()
        }
        update()
    }

    override fun putInt(key: String?, value: Int) {
        super.putInt(key, value)
    }

    override fun putLong(key: String?, value: Long) {
        super.putLong(key, value)
    }

    override fun putFloat(key: String?, value: Float) {
        when(key) {
            KEY_ROW_HEIGHT -> preset.rowHeight = value.toInt()
        }
        update()
    }

    override fun putBoolean(key: String?, value: Boolean) {
        when(key) {
            KEY_DEFAULT_HEIGHT -> preset.defaultHeight = value
            KEY_HANJA_CONVERSION -> preset.enableHanjaConversion = value
            KEY_HANJA_PREDICTION -> preset.enableHanjaPrediction = value
            KEY_HANJA_SORT_BY_CONTEXT -> preset.hanjaSortByContext = value
        }
        update()
    }

    override fun getString(key: String?, defValue: String?): String? {
        return when(key) {
            KEY_ENGINE_TYPE -> preset.type.name
            else -> defValue
        }
    }

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
        return when(key) {
            KEY_HANJA_ADDITIONAL_DICTIONARIES -> preset.hanjaAdditionalDictionaries
            else -> defValues
        }
    }

    override fun getInt(key: String?, defValue: Int): Int {
        return super.getInt(key, defValue)
    }

    override fun getLong(key: String?, defValue: Long): Long {
        return super.getLong(key, defValue)
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        return when(key) {
            KEY_ROW_HEIGHT -> preset.rowHeight.toFloat()
            else -> defValue
        }
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return when(key) {
            KEY_DEFAULT_HEIGHT -> preset.defaultHeight
            KEY_HANJA_CONVERSION -> preset.enableHanjaConversion
            KEY_HANJA_PREDICTION -> preset.enableHanjaPrediction
            KEY_HANJA_SORT_BY_CONTEXT -> preset.hanjaSortByContext
            else -> defValue
        }
    }

    fun write() {
        InputEnginePreset.yaml.encodeToStream(preset.commit(), file.outputStream())
    }

    fun update() {
        onChangeListener.onChange(preset.commit())
    }

    interface OnChangeListener {
        fun onChange(preset: InputEnginePreset)
    }

    companion object {
        const val KEY_DEFAULT_HEIGHT = "appearance_default_height"
        const val KEY_ROW_HEIGHT = "appearance_row_height"

        const val KEY_ENGINE_TYPE = "input_engine_type"
        const val KEY_HANJA_CONVERSION = "input_hanja_conversion"
        const val KEY_HANJA_PREDICTION = "input_hanja_prediction"
        const val KEY_HANJA_SORT_BY_CONTEXT = "input_hanja_sort_by_context"
        const val KEY_HANJA_ADDITIONAL_DICTIONARIES = "input_hanja_additional_dictionaries"
    }
}