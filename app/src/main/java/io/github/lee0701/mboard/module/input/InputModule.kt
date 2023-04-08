package io.github.lee0701.mboard.module.input

interface InputModule<In, Out> {
    fun process(input: In): Out
}