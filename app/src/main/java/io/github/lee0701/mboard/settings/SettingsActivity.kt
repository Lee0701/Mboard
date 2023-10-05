package io.github.lee0701.mboard.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.service.MBoardIME
import io.github.lee0701.mboard.settings.fragment.RootSettingsFragment

class SettingsActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, RootSettingsFragment())
            .commit()
        supportActionBar?.setDisplayShowHomeEnabled(true)

        PreferenceManager.setDefaultValues(this, R.xml.preference_root, false)
    }

    override fun onStop() {
        super.onStop()
        MBoardIME.sendReloadIntent(this)
    }
}