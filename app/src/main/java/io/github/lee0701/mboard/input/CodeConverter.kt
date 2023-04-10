package io.github.lee0701.mboard.input

import io.github.lee0701.mboard.module.CodeConvertTable
import io.github.lee0701.mboard.service.KeyboardState

class CodeConverter(
    val table: CodeConvertTable,
) {
    fun convert(input: Int, state: KeyboardState): Int? {
        val entry = table.map[input] ?: return null
        return entry.withKeyboardState(state)
    }
}