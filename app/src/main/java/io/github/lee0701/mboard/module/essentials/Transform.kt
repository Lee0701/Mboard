package io.github.lee0701.mboard.module.essentials

class Transform<In, Out>(
    private val predicate: (In) -> Out,
): InputModule<In, Out> {
    override fun process(input: In): Out {
        return predicate(input)
    }
}