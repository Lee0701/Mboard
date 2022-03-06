package io.github.lee0701.mboard.dictionary

interface MutableListDictionary<T>: MutableDictionary<List<T>>, ListDictionary<T> {

    fun insert(key: List<Byte>, item: T) {
        put(key, (search(key) ?: listOf()) + item)
    }

    fun remove(key: List<Byte>, item: T) {
        val newList = (search(key) ?: listOf()) - item
        if(newList.isEmpty()) remove(key)
        else put(key, newList)
    }
}