package io.github.lee0701.mboard.input

import io.github.lee0701.mboard.module.table.CodeConvertTable
import io.github.lee0701.mboard.service.KeyboardState

class CodeConverter(
    private val table: CodeConvertTable,
) {
    fun convert(input: Int, state: KeyboardState): Int? {
        return table.get(input, state)
    }
}