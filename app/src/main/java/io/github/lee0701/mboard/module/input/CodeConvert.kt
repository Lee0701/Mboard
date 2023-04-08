package io.github.lee0701.mboard.module.input

import io.github.lee0701.mboard.module.input.essentials.InputModule

class CodeConvert(
    private val table: Map<Int, Int> = mapOf(),
): InputModule<Int, Int?> {
    override fun process(input: Int): Int? {
        return table[input]
    }
}