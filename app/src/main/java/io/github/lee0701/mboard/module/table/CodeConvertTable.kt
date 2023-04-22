package io.github.lee0701.mboard.module.table

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
    val reversedCodeMap: Map<Pair<Int, EntryKey>, Int> = codeMap.flatMap { (key, value) ->
        value.explode().map { (entryKey, charCode) -> (charCode to entryKey) to key }
    }.toMap()

    operator fun plus(another: CodeConvertTable): CodeConvertTable {
        return CodeConvertTable(this.map + another.map)
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
        fun forKey(key: EntryKey): Int? {
            return when(key) {
                EntryKey.Base -> base
                EntryKey.Shift -> shift ?: base
                EntryKey.CapsLock -> capsLock ?: shift ?: base
                EntryKey.Alt -> alt ?: base
                EntryKey.AltShift -> altShift ?: alt
            }
        }
        fun explode(): Map<EntryKey, Int> {
            return listOfNotNull(
                base?.let { EntryKey.Base to it },
                shift?.let { EntryKey.Shift to it },
                capsLock?.let { EntryKey.CapsLock to it },
                alt?.let { EntryKey.Alt to it },
                altShift?.let { EntryKey.AltShift to it },
            ).toMap()
        }
    }

    enum class EntryKey {
        Base, Shift, CapsLock, Alt, AltShift;
        companion object {
            fun fromKeyboardState(keyboardState: KeyboardState): EntryKey {
                return if(keyboardState.altState.pressed && keyboardState.shiftState.pressed) AltShift
                else if(keyboardState.altState.pressed) Alt
                else if(keyboardState.shiftState.locked) CapsLock
                else if(keyboardState.shiftState.pressed) Shift
                else Base
            }
        }
    }
}