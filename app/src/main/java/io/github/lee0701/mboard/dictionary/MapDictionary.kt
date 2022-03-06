package io.github.lee0701.mboard.dictionary

class MapDictionary<T>(private val entries: Map<List<Byte>, T>): Dictionary<T> {
    override fun search(key: List<Byte>): T? {
        return entries[key]
    }

    override fun entries(): List<Pair<List<Byte>, T>> {
        return entries.map { (k, v) -> k to v }
    }
}
