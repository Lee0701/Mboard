package io.github.lee0701.mboard.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.service.MBoardIME
import io.github.lee0701.mboard.settings.fragment.RootSettingsFragment

class SettingsActivity: AppCompatActivity() {

    private val preferenceList = listOf<Int>(
        R.xml.preference_root,
        R.xml.preference_input_method,
        R.xml.preference_appearance,
        R.xml.preference_keyboard_layout_list,
        R.xml.preference_keyboard_layout,
        R.xml.preference_behaviour,
        R.xml.preference_about
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, RootSettingsFragment())
            .commit()
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val hasSetDefaultValues = PreferenceManager.getDefaultSharedPreferences(this)
            .getBoolean(PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, false)
        if(!hasSetDefaultValues) {
            preferenceList.forEach { xml ->
                PreferenceManager.setDefaultValues(this, xml, true)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        MBoardIME.sendReloadIntent(this)
    }
}