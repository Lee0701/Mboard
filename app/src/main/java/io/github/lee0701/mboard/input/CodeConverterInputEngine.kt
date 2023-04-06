package io.github.lee0701.mboard.input

import android.view.KeyCharacterMap
import io.github.lee0701.mboard.ime.KeyboardState

class CodeConverterInputEngine(
    private val codeTable: Map<Int, CodeConverter.Entry>,
    override val listener: InputEngine.Listener,
): InputEngine {
    private val keyCharacterMap: KeyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD)
    private val codeConverter = CodeConverter(codeTable)

    override fun onKey(code: Int, state: KeyboardState) {
        val converted = codeConverter.convert(code, state)
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
        return codeTable.mapValues { (_, entry) -> entry.withKeyboardState(state).toChar().toString() }
    }
}