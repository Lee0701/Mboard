package io.github.lee0701.mboard.dictionary

class EmptyDictionary<T>: Dictionary<T> {
    override fun search(key: String): T? {
        return null
    }
}