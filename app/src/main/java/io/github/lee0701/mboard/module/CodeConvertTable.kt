package io.github.lee0701.mboard.module

import android.view.KeyEvent
import io.github.lee0701.mboard.layout.CustomKeycode
import io.github.lee0701.mboard.module.serialization.HexIntSerializer
import io.github.lee0701.mboard.service.KeyboardState
import kotlinx.serialization.Serializable

@Serializable
data class CodeConvertTable(
    val map: Map<String, Entry> = mapOf(),
) {
    val codeMap: Map<Int, Entry> = map.mapKeys { (k, _) ->
        try {
            CustomKeycode.valueOf(k).code
        } catch(ex: IllegalArgumentException) {
            val keyCode = KeyEvent.keyCodeFromString(k)
            if(keyCode > 0) keyCode else k.toIntOrNull() ?: 0
        }
    }

    operator fun plus(another: CodeConvertTable): CodeConvertTable {
        return CodeConvertTable(map + another.map)
    }

    @Serializable
    data class Entry(
        @Serializable(with = HexIntSerializer::class) val base: Int? = null,
        @Serializable(with = HexIntSerializer::class) val shift: Int? = base,
        @Serializable(with = HexIntSerializer::class) val capsLock: Int? = shift,
        @Serializable(with = HexIntSerializer::class) val alt: Int? = base,
        @Serializable(with = HexIntSerializer::class) val altShift: Int? = shift,
    ) {
        fun withKeyboardState(keyboardState: KeyboardState): Int? {
            val shiftPressed = keyboardState.shiftState.pressed || keyboardState.shiftState.pressing
            val altPressed = keyboardState.altState.pressed || keyboardState.altState.pressing
            return if(keyboardState.shiftState.locked) capsLock
            else if(shiftPressed && altPressed) altShift
            else if(shiftPressed) shift
            else if(altPressed) alt
            else base
        }
    }
}