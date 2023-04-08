package io.github.lee0701.mboard.module.input

class CompoundInputModule<In, Md, Out>(
    val left: InputModule<In, Md>,
    val right: InputModule<Md, Out>,
) {
    operator fun InputModule<In, Md>.plus(another: InputModule<Md, Out>): CompoundInputModule<In, Md, Out> {
        return CompoundInputModule<In, Md, Out>(this, another)
    }
}