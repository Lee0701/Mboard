package io.github.lee0701.mboard.module.input

class CodeConvert(
    val table: Map<Int, Int>,
): InputModule<Int, Int?> {
    override fun process(input: Int): Int? {
        return table[input]
    }
}