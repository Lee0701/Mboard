package io.github.lee0701.mboard.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import io.github.lee0701.mboard.R
import java.io.File

class KeyboardLayoutSettingsActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fileName = intent.getStringExtra("fileName") ?: "default.yaml"
        setContentView(R.layout.activity_keyboard_layout_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsActivity.SettingsFragment())
            .commit()
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    class KeyboardSettingsFragment: PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.keyboard_layout_preferences, rootKey)
            preferenceManager.preferenceDataStore = KeyboardLayoutPreferenceDataStore(File(requireContext().filesDir, "arst.json"))
        }
    }
}