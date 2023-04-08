package io.github.lee0701.mboard.module.input.essentials

object Split {
    class ToPair<T>: InputModule<T, Pair<T, T>> {
        override fun process(input: T): Pair<T, T> {
            return input to input
        }
    }

    class ToList<T>(
        private val n: Int,
    ): InputModule<T, List<T>> {
        override fun process(input: T): List<T> {
            return (0 until n).map { input }
        }
    }
}