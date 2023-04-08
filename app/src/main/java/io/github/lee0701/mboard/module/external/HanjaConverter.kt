package io.github.lee0701.mboard.module.external

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.widget.Toast
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.dictionary.DictionaryManager
import io.github.lee0701.mboard.dictionary.HanjaDictionary
import io.github.lee0701.mboard.dictionary.ListDictionary

object HanjaConverter {
    fun loadDictionary(context: Context): ListDictionary<HanjaDictionary.Entry>? {
        val packageNames = listOf(
            "io.github.lee0701.converter.donation",
            "io.github.lee0701.converter",
        )
        var resources: Resources? = null
        var packageName: String? = null
        for(name in packageNames) {
            try {
                resources = context.packageManager.getResourcesForApplication(name)
                packageName = name
            } catch(ex: PackageManager.NameNotFoundException) {
                Toast.makeText(context, R.string.msg_hanja_converter_not_found, Toast.LENGTH_LONG).show()
            }
            if(resources != null) break
        }
        return if(resources != null && packageName != null)
            DictionaryManager.loadCompoundDictionary(resources, listOf("base"), packageName)
        else null
    }
}