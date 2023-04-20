package io.github.lee0701.mboard.module.table

import io.github.lee0701.mboard.module.serialization.HexIntSerializer
import io.github.lee0701.mboard.module.serialization.KeyCodeSerializer
import io.github.lee0701.mboard.service.KeyboardState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("simple")
class SimpleCodeConvertTable(
    @Serializable private val map: Map<
            @Serializable(with = KeyCodeSerializer::class) Int,
            Entry> = mapOf(),
): CodeConvertTable {

    override fun get(keyCode: Int, state: KeyboardState): Int? {
        return map[keyCode]?.withKeyboardState(state)
    }

    override fun getAllForState(state: KeyboardState): Map<Int, Int> {
        return map.map { (k, v) -> v.withKeyboardState(state)?.let { k to it } }
            .filterNotNull()
            .toMap()
    }

    operator fun plus(simpleCodeConvertTable: SimpleCodeConvertTable): SimpleCodeConvertTable {
        return SimpleCodeConvertTable(map = this.map + simpleCodeConvertTable.map)
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