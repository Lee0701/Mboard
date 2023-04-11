package io.github.lee0701.mboard.dictionary

typealias HanjaDictionary = ListDictionary<HanjaDictionaryEntry>

data class HanjaDictionaryEntry(
    val result: String,
    val extra: String?,
    val frequency: Int,
)