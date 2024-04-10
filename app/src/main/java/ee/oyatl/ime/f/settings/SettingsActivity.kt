package ee.oyatl.ime.f.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import ee.oyatl.ime.f.R
import ee.oyatl.ime.f.service.MBoardIME

class SettingsActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayShowHomeEnabled(true)

        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
    }

    override fun onStop() {
        super.onStop()
        MBoardIME.sendReloadIntent(this)
    }

    class SettingsFragment: PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}