package io.github.lee0701.mboard.input

import android.graphics.drawable.Drawable
import android.view.KeyCharacterMap
import io.github.lee0701.mboard.module.CodeConvertTable
import io.github.lee0701.mboard.service.KeyboardState

class CodeConverterInputEngine(
    private val table: CodeConvertTable,
    override val listener: InputEngine.Listener,
): InputEngine {

    private val keyCharacterMap: KeyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD)

    override fun onKey(code: Int, state: KeyboardState) {
        val converted = table.map[code]?.withKeyboardState(state)
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

    override fun onReset() {
        listener.onFinishComposing()
    }

    override fun getLabels(state: KeyboardState): Map<Int, CharSequence> {
        val codeMap = table.map.mapValues { (_, entry) -> entry.withKeyboardState(state).toChar().toString() }
        return DirectInputEngine.getLabels(keyCharacterMap, state) + codeMap
    }

    override fun getIcons(state: KeyboardState): Map<Int, Drawable> {
        return emptyMap()
    }
}