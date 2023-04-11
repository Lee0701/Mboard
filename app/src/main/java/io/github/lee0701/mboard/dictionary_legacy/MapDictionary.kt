package io.github.lee0701.mboard.dictionary_legacy

class MapDictionary<T>(private val entries: Map<String, T>): Dictionary<T> {
    override fun search(key: String): T? {
        return entries[key]
    }
}
