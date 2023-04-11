package io.github.lee0701.mboard.input

import io.github.lee0701.mboard.dictionary_legacy.HanjaDictionary

open class DictionaryHanjaConverter(
    private val dictionary: HanjaDictionary,
) {

    fun convert(word: String): List<Candidate> {
        val result = dictionary.search(word) ?: emptyList()
        return result.map { DefaultHanjaCandidate(word, it.result, it.extra ?: "") }
    }

    fun convertPrefix(word: String): List<List<Candidate>> {
        return word.indices.reversed().map { i ->
            val slicedWord = word.slice(0 .. i)
            dictionary.search(slicedWord)
                ?.sortedByDescending { it.frequency }
                ?.map { DefaultHanjaCandidate(it.result, slicedWord, it.extra ?: "") } ?: emptyList()
        }
    }

}