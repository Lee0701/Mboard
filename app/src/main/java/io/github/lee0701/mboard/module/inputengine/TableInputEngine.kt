package io.github.lee0701.mboard.module.inputengine

import android.view.KeyCharacterMap
import io.github.lee0701.mboard.preset.table.CodeConvertTable
import io.github.lee0701.mboard.preset.table.LayeredCodeConvertTable
import io.github.lee0701.mboard.service.KeyboardState

open class TableInputEngine(
    private val table: CodeConvertTable,
): InputEngineWrapper(EmptyInputEngine) {
    private val keyCharacterMap: KeyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD)

    open val layerId: String = "base"

    override fun onKey(code: Int, state: KeyboardState) {
        val converted =
            if(table is LayeredCodeConvertTable) table.get(layerId, code, state)
            else table.get(code, state)

    }

}