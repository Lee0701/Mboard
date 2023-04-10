package io.github.lee0701.mboard.input

import android.graphics.drawable.Drawable
import io.github.lee0701.mboard.service.KeyboardState

interface InputEngine {
    val listener: Listener

    fun onKey(code: Int, state: KeyboardState)
    fun onDelete()

    fun onReset()

    fun getLabels(state: KeyboardState): Map<Int, CharSequence>
    fun getIcons(state: KeyboardState): Map<Int, Drawable>

    interface Listener {
        fun onComposingText(text: CharSequence)
        fun onFinishComposing()
        fun onCommitText(text: CharSequence)
        fun onDeleteText(beforeLength: Int, afterLength: Int)
        fun onSystemKey(code: Int): Boolean
        fun onEditorAction(code: Int)
    }
}