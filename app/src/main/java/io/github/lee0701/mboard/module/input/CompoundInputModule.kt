package io.github.lee0701.mboard.module.input

class CompoundInputModule<In, Md, Out>(
    val left: InputModule<In, Md>,
    val right: InputModule<Md, Out>,
): InputModule<In, Out> {
    override fun process(input: In): Out {
        return right.process(left.process(input))
    }

    operator fun InputModule<In, Md>.plus(another: InputModule<Md, Out>): CompoundInputModule<In, Md, Out> {
        return CompoundInputModule(this, another)
    }
}