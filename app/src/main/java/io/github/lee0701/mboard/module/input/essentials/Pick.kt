package io.github.lee0701.mboard.module.input.essentials

class Pick<T>(
    private val index: Int,
): InputModule<List<T>, T> {
    override fun process(input: List<T>): T {
        return input[index]
    }

    class A<A, B>: InputModule<Pair<A, B>, A> {
        override fun process(input: Pair<A, B>): A {
            return input.first
        }
    }
    class B<A, B>: InputModule<Pair<A, B>, B> {
        override fun process(input: Pair<A, B>): B {
            return input.second
        }
    }
}