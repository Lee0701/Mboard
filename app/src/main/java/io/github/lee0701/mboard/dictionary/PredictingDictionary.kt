package io.github.lee0701.mboard.dictionary

interface PredictingDictionary<T>: ListDictionary<T> {

    fun predict(key: List<Byte>): List<Pair<List<Byte>, T>>
}