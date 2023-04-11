package io.github.lee0701.mboard.dictionary_legacy

interface Dictionary<T> {
    fun search(key: String): T?
}
