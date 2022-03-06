package io.github.lee0701.mboard.dictionary

interface MutableDictionary<T>: Dictionary<T> {
    fun put(key: List<Byte>, value: T)
    fun remove(key: List<Byte>)
}
