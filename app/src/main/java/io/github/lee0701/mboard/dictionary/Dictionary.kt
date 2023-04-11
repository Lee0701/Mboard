package io.github.lee0701.mboard.dictionary

interface Dictionary {
    fun search(key: List<Int>): List<Int>
    fun entries(): Map<CharSequence, List<Int>>
}