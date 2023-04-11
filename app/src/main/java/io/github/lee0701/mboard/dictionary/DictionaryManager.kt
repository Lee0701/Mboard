package io.github.lee0701.mboard.dictionary

import android.content.res.AssetManager
import java.io.IOException

object DictionaryManager {
    fun loadCompoundDictionary(assetManager: AssetManager, names: List<String>): CompoundDictionary<HanjaDictionaryEntry> {
        val dictionaries = names.mapNotNull { name ->
            loadDictionary(assetManager, name)
        }
        return CompoundDictionary(dictionaries)
    }
    fun loadDictionary(assetManager: AssetManager, name: String): ListDictionary<HanjaDictionaryEntry>? {
        return try {
            DiskDictionary(assetManager.open("dict/$name.bin"))
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }
}