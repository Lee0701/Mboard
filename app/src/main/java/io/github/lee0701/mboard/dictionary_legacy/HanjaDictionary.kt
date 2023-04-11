package io.github.lee0701.mboard.dictionary_legacy

typealias HanjaDictionary = ListDictionary<HanjaDictionaryEntry>

data class HanjaDictionaryEntry(
    val result: String,
    val extra: String?,
    val frequency: Int,
)