package io.github.lee0701.mboard.module.essentials

class Both<T, A, B>(
    private val predicateA: (T) -> A,
    private val predicateB: (T) -> B,
): InputModule<Pair<T, T>, Pair<A, B>> {
    override fun process(input: Pair<T, T>): Pair<A, B> {
        return predicateA(input.first) to predicateB(input.second)
    }
}