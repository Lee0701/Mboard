package io.github.lee0701.mboard.input

import io.github.lee0701.mboard.ime.KeyboardState

class CodeConverter(
    private val map: Map<Int, Entry> = mapOf(),
) {
    fun convert(input: Int, state: KeyboardState): Int? {
        val entry = map[input] ?: return null
        return entry.withKeyboardState(state)
    }

    data class Entry(
        val base: Int,
        val shift: Int = base,
        val capsLocked: Int = shift,
        val alt: Int = base,
        val altShift: Int = shift,
    ) {
        constructor(
            base: Char,
            shift: Char = base,
            capsLocked: Char = shift,
            alt: Char = base,
            altShift: Char = shift,
        ): this(base.code, shift.code, capsLocked.code, alt.code, altShift.code)

        fun withKeyboardState(keyboardState: KeyboardState): Int {
            val shiftPressed = keyboardState.shiftState.pressed || keyboardState.shiftState.pressing
            val altPressed = keyboardState.altState.pressed || keyboardState.altState.pressing
            return if(keyboardState.shiftState.locked) capsLocked
            else if(shiftPressed && altPressed) altShift
            else if(shiftPressed) shift
            else if(altPressed) alt
            else base
        }
    }
}