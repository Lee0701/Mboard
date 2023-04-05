package io.github.lee0701.mboard.input

import io.github.lee0701.mboard.ime.KeyboardState

class CodeConverter(
    private val map: Map<Int, Entry> = mapOf(),
) {
    fun convert(input: Int, state: KeyboardState): Int? {
        val entry = map[input] ?: return null
        return if(state.altState.pressed && state.shiftState.pressed) entry.altShift
        else if(state.altState.pressed) entry.alt
        else if(state.shiftState.locked) entry.capsLocked
        else if(state.shiftState.pressed) entry.shift
        else entry.base
    }

    data class Entry(
        val base: Int,
        val shift: Int = base,
        val capsLocked: Int = shift,
        val alt: Int = base,
        val altShift: Int = shift,
    )
}