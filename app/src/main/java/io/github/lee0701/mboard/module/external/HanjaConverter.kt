package io.github.lee0701.mboard.module.external

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.dictionary.DictionaryManager
import io.github.lee0701.mboard.dictionary.HanjaDictionary
import io.github.lee0701.mboard.dictionary.HanjaDictionaryEntry
import io.github.lee0701.mboard.dictionary.ListDictionary

object HanjaConverter {
    fun loadDictionary(context: Context): ListDictionary<HanjaDictionaryEntry>? {
        val packageNames = listOf(
            "io.github.lee0701.converter.donation",
            "io.github.lee0701.converter",
        )
        for(name in packageNames) {
            try {
                val packageContext = context.createPackageContext(name, 0) ?: continue
                return DictionaryManager.loadCompoundDictionary(packageContext.assets, listOf("base"))
            } catch(ex: PackageManager.NameNotFoundException) {
            }
        }
        Toast.makeText(context, R.string.msg_hanja_converter_not_found, Toast.LENGTH_LONG).show()
        return null
    }
}