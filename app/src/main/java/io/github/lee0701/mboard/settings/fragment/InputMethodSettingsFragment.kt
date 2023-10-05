package io.github.lee0701.mboard.settings.fragment

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import io.github.lee0701.mboard.R

class InputMethodSettingsFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_input_method, rootKey)
    }
}