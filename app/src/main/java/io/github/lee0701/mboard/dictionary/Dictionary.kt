package io.github.lee0701.mboard.dictionary

interface Dictionary<T> {
    fun search(key: List<Byte>): T?
    fun entries(): List<Pair<List<Byte>, T>>
}
