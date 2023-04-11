package io.github.lee0701.mboard.dictionary_legacy

interface PredictingDictionary<T>: ListDictionary<T> {

    fun predict(key: String): List<Pair<String, T>>
}