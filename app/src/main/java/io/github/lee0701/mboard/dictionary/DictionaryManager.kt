package io.github.lee0701.mboard.dictionary

import android.annotation.SuppressLint
import android.content.res.Resources
import android.content.res.Resources.NotFoundException
import java.io.IOException

object DictionaryManager {
    fun loadCompoundDictionary(resources: Resources, names: List<String>, packageName: String): CompoundDictionary<HanjaDictionary.Entry> {
        val dictionaries = names.mapNotNull { name ->
            loadDictionary(resources, name, packageName)
        }
        return CompoundDictionary(dictionaries)
    }
    @SuppressLint("DiscouragedApi")
    fun loadDictionary(resources: Resources, name: String, packageName: String): ListDictionary<HanjaDictionary.Entry>? {
        return try {
            val id = resources.getIdentifier(name, "raw", packageName)
            if(id > 0) DiskDictionary(resources.openRawResource(id))
            else null
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        } catch(ex: NotFoundException) {
            ex.printStackTrace()
            null
        }
    }
}