package io.github.lee0701.mboard.dictionary

interface Dictionary {
    fun search(key: List<Int>): Map<Int, Int>
    fun searchPrefix(key: List<Int>): Map<Int, Int>
    fun entries(): Map<List<Int>, Map<Int, Int>>
}