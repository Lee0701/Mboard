package io.github.lee0701.mboard.dictionary

interface MutableDictionary: Dictionary {
    fun put(key: List<Int>, value: List<Int>)
}