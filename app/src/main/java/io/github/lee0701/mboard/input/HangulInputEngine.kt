package io.github.lee0701.mboard.input

import android.graphics.drawable.Drawable
import android.view.KeyCharacterMap
import io.github.lee0701.mboard.charset.Hangul
import io.github.lee0701.mboard.module.softkeyboard.Keyboard
import io.github.lee0701.mboard.module.table.CodeConvertTable
import io.github.lee0701.mboard.module.table.JamoCombinationTable
import io.github.lee0701.mboard.service.KeyboardState

class HangulInputEngine(
    private val table: CodeConvertTable,
    private val jamoCombinationTable: JamoCombinationTable,
    override val listener: InputEngine.Listener,
): InputEngine {

    private val keyCharacterMap: KeyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD)
    private val hangulCombiner = HangulCombiner(jamoCombinationTable)

    private val stateStack: MutableList<HangulCombiner.State> = mutableListOf()
    private val hangulState: HangulCombiner.State get() = stateStack.lastOrNull() ?: HangulCombiner.State()

    override fun onKey(code: Int, state: KeyboardState) {
        val converted = table.codeMap[code]?.withKeyboardState(state)
        if(converted == null) {
            val char = keyCharacterMap.get(code, state.asMetaState())
            onReset()
            if(char > 0) listener.onCommitText(char.toChar().toString())
        } else {
            val (text, hangulStates) = hangulCombiner.combine(hangulState, converted)
            if(text.isNotEmpty()) clearStack()
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

    override fun onTextAroundCursor(before: String, after: String) {
    }

    override fun onReset() {
        listener.onFinishComposing()
        clearStack()
    }

    fun clearStack() {
        stateStack.clear()
    }

    override fun getLabels(state: KeyboardState): Map<Int, CharSequence> {
        val codeMap = table.codeMap.mapValues { (_, entry) ->
            val ch = entry.withKeyboardState(state)?.let { it and 0xffffff }
            if(ch != null && Hangul.isModernJamo(ch)) {
                if(Hangul.isCho(ch)) Hangul.choToCompatConsonant(ch.toChar()).toString()
                else if(Hangul.isJung(ch)) Hangul.jungToCompatVowel(ch.toChar()).toString()
                else if(Hangul.isJong(ch)) Hangul.jongToCompatConsonant(ch.toChar()).toString()
                else ch.toChar().toString()
            } else ch?.toChar()?.toString().orEmpty()
        }
        return DirectInputEngine.getLabels(keyCharacterMap, state) + codeMap
    }

    override fun getIcons(state: KeyboardState): Map<Int, Drawable> {
        return emptyMap()
    }

    override fun getMoreKeys(state: KeyboardState): Map<Int, Keyboard> {
        return mapOf()
    }
}