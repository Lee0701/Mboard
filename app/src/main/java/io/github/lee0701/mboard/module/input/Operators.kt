package io.github.lee0701.mboard.module.input

import io.github.lee0701.mboard.module.input.essentials.InputModule

operator fun <In, Md, Out> InputModule<In, Md>.plus(another: InputModule<Md, Out>): CompoundInputModule<In, Md, Out> {
    return CompoundInputModule(this, another)
}
