package io.github.lee0701.mboard.input

import android.graphics.drawable.Drawable
import io.github.lee0701.mboard.module.softkeyboard.Keyboard
import io.github.lee0701.mboard.service.KeyboardState

interface InputEngine {
    val listener: Listener

    fun onKey(code: Int, state: KeyboardState)
    fun onDelete()
    fun onTextAroundCursor(before: String, after: String)

    fun onReset()

    fun getLabels(state: KeyboardState): Map<Int, CharSequence>
    fun getIcons(state: KeyboardState): Map<Int, Drawable>
    fun getMoreKeys(state: KeyboardState): Map<Int, Keyboard>

    interface Listener {
        fun onComposingText(text: CharSequence)
        fun onFinishComposing()
        fun onCommitText(text: CharSequence)
        fun onDeleteText(beforeLength: Int, afterLength: Int)
        fun onCandidates(list: List<Candidate>)
        fun onSystemKey(code: Int): Boolean
        fun onEditorAction(code: Int)
    }
}