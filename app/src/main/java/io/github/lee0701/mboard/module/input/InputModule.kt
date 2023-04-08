package io.github.lee0701.mboard.module.input

interface InputModule<In, Out> {
    fun process(input: In): Out
    operator fun <In, Md, Out>InputModule<In, Md>.plus(another: InputModule<Md, Out>): CompoundInputModule<In, Md, Out> {
        return CompoundInputModule(this, another)
    }
}