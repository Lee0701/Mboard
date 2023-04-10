package io.github.lee0701.mboard.module

import io.github.lee0701.mboard.service.KeyboardState
import kotlinx.serialization.Serializable

@Serializable
data class CodeConvertTable(
    val map: Map<Int, Entry> = mapOf(),
) {
    @Serializable
    data class Entry(
        val base: Int,
        val shift: Int = base,
        val capsLocked: Int = shift,
        val alt: Int = base,
        val altShift: Int = shift,
    ) {
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