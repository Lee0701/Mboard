package io.github.lee0701.mboard.module.input

import io.github.lee0701.mboard.module.essentials.InputModule

class CompoundInputModule<In, Md, Out>(
    private val left: InputModule<In, Md>,
    private val right: InputModule<Md, Out>,
): InputModule<In, Out> {
    override fun process(input: In): Out {
        return right.process(left.process(input))
    }
}