package io.github.lee0701.mboard.input

import android.graphics.drawable.Drawable
import android.view.KeyCharacterMap
import io.github.lee0701.mboard.module.table.CodeConvertTable
import io.github.lee0701.mboard.service.KeyboardState

class CodeConverterInputEngine(
    private val table: CodeConvertTable,
    override val listener: InputEngine.Listener,
): InputEngine {
    private val moreKeysMap = CodeConvertTable(mapOf(
        "1000" to CodeConvertTable.Entry('A'.code),
        "1001" to CodeConvertTable.Entry('R'.code),
        "1002" to CodeConvertTable.Entry('S'.code),
        "1003" to CodeConvertTable.Entry('T'.code),
        "1010" to CodeConvertTable.Entry('O'.code),
        "1011" to CodeConvertTable.Entry('E'.code),
        "1012" to CodeConvertTable.Entry('U'.code),
    ))

    private val keyCharacterMap: KeyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD)

    override fun onKey(code: Int, state: KeyboardState) {
        val converted = (moreKeysMap.codeMap[code] ?: table.codeMap[code])?.withKeyboardState(state)
        if(converted == null) {
            val char = keyCharacterMap.get(code, state.asMetaState())
            onReset()
            if(char > 0) listener.onCommitText(char.toChar().toString())
        } else {
            listener.onCommitText(converted.toChar().toString())
        }
    }

    override fun onDelete() {
        listener.onDeleteText(1, 0)
    }

    override fun onTextAroundCursor(before: String, after: String) {
    }

    override fun onReset() {
        listener.onFinishComposing()
    }

    override fun getLabels(state: KeyboardState): Map<Int, CharSequence> {
        val codeMap = table.codeMap.mapValues { (_, entry) -> entry.withKeyboardState(state)?.toChar().toString() }
        return DirectInputEngine.getLabels(keyCharacterMap, state) + codeMap
    }

    override fun getIcons(state: KeyboardState): Map<Int, Drawable> {
        return emptyMap()
    }
}