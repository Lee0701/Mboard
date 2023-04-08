package io.github.lee0701.mboard.module.input

import io.github.lee0701.mboard.module.input.essentials.InputModule

class ComposeWord: InputModule<Int, List<Int>> {
    private val inputs: MutableList<Int> = mutableListOf()
    override fun process(input: Int): List<Int> {
        inputs += input
        return inputs
    }
}