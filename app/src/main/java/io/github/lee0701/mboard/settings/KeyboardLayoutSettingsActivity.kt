package io.github.lee0701.mboard.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import io.github.lee0701.mboard.R
import java.io.File

class KeyboardLayoutSettingsActivity: AppCompatActivity() {

    lateinit var preferenceDataStore: KeyboardLayoutPreferenceDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fileName = intent.getStringExtra("fileName") ?: "default.yaml"
        val file = File(filesDir, fileName)
        if(!file.exists()) {
            val input = assets.open("preset/preset_mobile_3set_391_strict.yaml")
            file.outputStream().write(input.readBytes())
        }
        preferenceDataStore = KeyboardLayoutPreferenceDataStore(file)
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

    class KeyboardSettingsFragment(
        private val file: File,
        private val preferenceDataStore: KeyboardLayoutPreferenceDataStore,
    ): PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.preferenceDataStore = preferenceDataStore
            setPreferencesFromResource(R.xml.keyboard_layout_preferences, rootKey)
        }
    }
}