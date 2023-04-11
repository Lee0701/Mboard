package io.github.lee0701.mboard.module.external

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.preference.PreferenceManager
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.dictionary_legacy.DictionaryManager
import io.github.lee0701.mboard.dictionary_legacy.HanjaDictionaryEntry
import io.github.lee0701.mboard.dictionary_legacy.ListDictionary

object HanjaConverter {
    fun loadDictionary(context: Context): ListDictionary<HanjaDictionaryEntry>? {
        val packageNames = listOf(
            "io.github.lee0701.converter.donation",
            "io.github.lee0701.converter",
        )
        for(name in packageNames) {
            try {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                val additional = sharedPreferences.getStringSet("input_hanja_additional_dictionaries", setOf()).orEmpty()
                val packageContext = context.createPackageContext(name, 0) ?: continue
                return DictionaryManager.loadCompoundDictionary(packageContext.assets, listOf("base") + additional)
            } catch(ex: PackageManager.NameNotFoundException) { }
        }
        Toast.makeText(context, R.string.msg_hanja_converter_not_found, Toast.LENGTH_LONG).show()
        return null
    }
}