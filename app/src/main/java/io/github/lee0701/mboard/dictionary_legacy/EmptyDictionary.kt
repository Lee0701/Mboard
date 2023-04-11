package io.github.lee0701.mboard.dictionary_legacy

class EmptyDictionary<T>: Dictionary<T> {
    override fun search(key: String): T? {
        return null
    }
}