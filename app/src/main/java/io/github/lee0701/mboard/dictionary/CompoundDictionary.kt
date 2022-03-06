package io.github.lee0701.mboard.dictionary

class CompoundDictionary<T>(
    private val dictionaries: List<ListDictionary<T>>
): ListDictionary<T>, PredictingDictionary<T> {
    override fun search(key: List<Byte>): List<T> {
        val result = dictionaries.mapNotNull { it.search(key) }
        return result.flatten()
    }

    override fun predict(key: List<Byte>): List<Pair<List<Byte>, T>> {
        val result = dictionaries.filterIsInstance<PredictingDictionary<T>>().map { it.predict(key) }
        return result.flatten()
    }

    override fun entries(): List<Pair<List<Byte>, List<T>>> {
        return dictionaries.flatMap { it.entries() }
    }
}