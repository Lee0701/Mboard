package io.github.lee0701.mboard.settings

import android.content.Context
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceManager
import com.charleskorn.kaml.decodeFromStream
import com.charleskorn.kaml.encodeToStream
import io.github.lee0701.mboard.module.InputEnginePreset
import java.io.File

class KeyboardLayoutPreferenceDataStore(
    val context: Context,
    val file: File,
    val onChangeListener: OnChangeListener,
): PreferenceDataStore() {
    val rootPreference = PreferenceManager.getDefaultSharedPreferences(context)

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
        super.putString(key, value)
    }

    override fun putStringSet(key: String?, values: MutableSet<String>?) {
        super.putStringSet(key, values)
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
        }
        update()
    }

    override fun getString(key: String?, defValue: String?): String? {
        return super.getString(key, defValue)
    }

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
        return super.getStringSet(key, defValues)
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
        const val KEY_DEFAULT_HEIGHT = "default_height"
        const val KEY_ROW_HEIGHT = "row_height"
    }
}