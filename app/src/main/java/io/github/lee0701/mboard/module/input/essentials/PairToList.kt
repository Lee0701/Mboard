package io.github.lee0701.mboard.module.input.essentials

class PairToList<T>: InputModule<Pair<T, T>, List<T>> {
    override fun process(input: Pair<T, T>): List<T> {
        return listOf(input.first, input.second)
    }
}