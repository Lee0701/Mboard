package io.github.lee0701.mboard.dictionary

interface Dictionary<T> {
    fun search(key: String): T?
}
