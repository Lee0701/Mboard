package io.github.lee0701.mboard.dictionary_legacy

interface WritableDictionary<T>: Dictionary<T> {
    fun insert(key: String, value: T)
    fun remove(key: T)
}
