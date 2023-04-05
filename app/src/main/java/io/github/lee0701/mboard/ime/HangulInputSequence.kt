package io.github.lee0701.mboard.ime

import android.view.KeyCharacterMap
import io.github.lee0701.mboard.input.CodeConverter
import io.github.lee0701.mboard.input.HangulCombiner

class HangulInputSequence(
    val codeTable: Map<Int, CodeConverter.Entry>,
    val jamoCombinationTable: Map<Pair<Int, Int>, Int>,
    val listener: Listener,
) {
    private val keyCharacterMap: KeyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD)
    private val codeConverter = CodeConverter(codeTable)
    private val hangulCombiner = HangulCombiner(jamoCombinationTable)

    private val stateStack: MutableList<HangulCombiner.State> = mutableListOf()
    private val hangulState: HangulCombiner.State get() = stateStack.lastOrNull() ?: HangulCombiner.State()

    fun onKey(code: Int, state: KeyboardState) {
        val converted = codeConverter.convert(code, state)
        if(converted == null) {
            val char = keyCharacterMap.get(code, state.asMetaState())
            reset()
            listener.onCommitText(char.toString())
        } else {
            val (text, hangulState) = hangulCombiner.combine(hangulState, converted)
            if(text.isNotEmpty()) this.stateStack.clear()
            this.stateStack += hangulState
            listener.onCommitText(text)
            listener.onComposingText(hangulState.composed)
        }
    }

    fun onDelete() {
        if(stateStack.size >= 1) {
            stateStack.removeLast()
            listener.onComposingText(stateStack.lastOrNull()?.composed ?: "")
        }
        else listener.onDeleteText(1, 0)
    }

    fun reset() {
        listener.onCommitText(hangulState.composed)
        stateStack.clear()
        listener.onComposingText("")
        listener.onFinishComposing()
    }

    interface Listener {
        fun onComposingText(text: CharSequence)
        fun onFinishComposing()
        fun onCommitText(text: CharSequence)
        fun onDeleteText(beforeLength: Int, afterLength: Int)
    }
}