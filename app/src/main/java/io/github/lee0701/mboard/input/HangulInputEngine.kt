package io.github.lee0701.mboard.input

import android.graphics.drawable.Drawable
import android.view.KeyCharacterMap
import io.github.lee0701.mboard.service.KeyboardState

class HangulInputEngine(
    private val codeConverter: CodeConverter,
    private val jamoCombinationTable: JamoCombinationTable,
    override val listener: InputEngine.Listener,
): InputEngine {

    private val keyCharacterMap: KeyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD)
    private val hangulCombiner = HangulCombiner(jamoCombinationTable)

    private val stateStack: MutableList<HangulCombiner.State> = mutableListOf()
    private val hangulState: HangulCombiner.State get() = stateStack.lastOrNull() ?: HangulCombiner.State()

    override fun onKey(code: Int, state: KeyboardState) {
        val converted = codeConverter.convert(code, state)
        if(converted == null) {
            val char = keyCharacterMap.get(code, state.asMetaState())
            onReset()
            if(char > 0) listener.onCommitText(char.toChar().toString())
        } else {
            val (text, hangulStates) = hangulCombiner.combine(hangulState, converted)
            if(text.isNotEmpty()) this.stateStack.clear()
            this.stateStack += hangulStates
            listener.onCommitText(text)
            listener.onComposingText(hangulStates.lastOrNull()?.composed ?: "")
        }
    }

    override fun onDelete() {
        if(stateStack.size >= 1) {
            stateStack.removeLast()
            listener.onComposingText(stateStack.lastOrNull()?.composed ?: "")
        }
        else listener.onDeleteText(1, 0)
    }

    override fun onReset() {
        listener.onFinishComposing()
        stateStack.clear()
    }

    override fun getLabels(state: KeyboardState): Map<Int, CharSequence> {
        val codeMap = codeConverter.map.mapValues { (_, entry) -> entry.withKeyboardState(state).toChar().toString() }
        return DirectInputEngine.getLabels(keyCharacterMap, state) + codeMap
    }

    override fun getIcons(state: KeyboardState): Map<Int, Drawable> {
        return emptyMap()
    }
}