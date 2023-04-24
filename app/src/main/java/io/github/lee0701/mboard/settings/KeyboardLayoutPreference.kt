package io.github.lee0701.mboard.settings

import android.content.SharedPreferences
import com.charleskorn.kaml.decodeFromStream
import com.charleskorn.kaml.encodeToStream
import io.github.lee0701.mboard.module.InputEnginePreset
import java.io.File

class KeyboardLayoutPreference(
    val file: File,
    val preset: InputEnginePreset = InputEnginePreset.yaml.decodeFromStream(file.inputStream()),
): SharedPreferences {

    override fun getAll(): MutableMap<String, *> {
        return mutableMapOf<String, Any>()
    }

    override fun getString(key: String?, defValue: String?): String? {
        TODO("Not yet implemented")
    }

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
        TODO("Not yet implemented")
    }

    override fun getInt(key: String?, defValue: Int): Int {
        TODO("Not yet implemented")
    }

    override fun getLong(key: String?, defValue: Long): Long {
        TODO("Not yet implemented")
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        return when(key) {
            KEY_ROW_HEIGHT -> preset.rowHeight.toFloat()
            else -> defValue
        }
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun contains(key: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun edit(): SharedPreferences.Editor {
        return Editor(file, preset.mutable())
    }

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        TODO("Not yet implemented")
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        TODO("Not yet implemented")
    }

    class Editor(
        val file: File,
        val mutablePreset: InputEnginePreset.Mutable,
    ): SharedPreferences.Editor {
        override fun putString(key: String?, value: String?): SharedPreferences.Editor {
            TODO("Not yet implemented")
        }

        override fun putStringSet(
            key: String?,
            values: MutableSet<String>?
        ): SharedPreferences.Editor {
            TODO("Not yet implemented")
        }

        override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
            TODO("Not yet implemented")
        }

        override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
            TODO("Not yet implemented")
        }

        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
            when(key) {
                KEY_ROW_HEIGHT -> mutablePreset.rowHeight = value.toInt()
                else -> {}
            }
            return this
        }

        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
            TODO("Not yet implemented")
        }

        override fun remove(key: String?): SharedPreferences.Editor {
            TODO("Not yet implemented")
        }

        override fun clear(): SharedPreferences.Editor {
            TODO("Not yet implemented")
        }

        override fun commit(): Boolean {
            val preset = mutablePreset.commit()
            InputEnginePreset.yaml.encodeToStream(preset, file.outputStream())
            return true
        }

        override fun apply() {
            val preset = mutablePreset.commit()
            InputEnginePreset.yaml.encodeToStream(preset, file.outputStream())
        }
    }

    companion object {
        const val KEY_ROW_HEIGHT = "row_height"
    }
}