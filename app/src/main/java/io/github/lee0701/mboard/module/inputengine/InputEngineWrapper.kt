package io.github.lee0701.mboard.module.inputengine

import android.graphics.drawable.Drawable
import io.github.lee0701.mboard.module.candidates.Candidate
import io.github.lee0701.mboard.module.component.InputViewComponent
import io.github.lee0701.mboard.preset.softkeyboard.Keyboard
import io.github.lee0701.mboard.service.KeyboardState

abstract class InputEngineWrapper(
    open val inputEngine: InputEngine,
): InputEngine {

    override var listener: InputEngineListener? = null
    override var components: List<InputViewComponent> = listOf()
    override var symbolsInputEngine: InputEngine? = null
    override var alternativeInputEngine: InputEngine? = null

    override fun onReset() {
        inputEngine.onReset()
    }

    override fun onKey(code: Int, state: KeyboardState) {
        inputEngine.onKey(code, state)
    }

    override fun onDelete() {
        inputEngine.onDelete()
    }

    override fun onTextAroundCursor(before: String, after: String) {
        inputEngine.onTextAroundCursor(before, after)
    }

    override fun getLabels(state: KeyboardState): Map<Int, CharSequence> {
        return inputEngine.getLabels(state)
    }

    override fun getIcons(state: KeyboardState): Map<Int, Drawable> {
        return inputEngine.getIcons(state)
    }

    override fun getMoreKeys(state: KeyboardState): Map<Int, Keyboard> {
        return inputEngine.getMoreKeys(state)
    }

    abstract class Listener:
        InputEngineListener {

        private val output: Listener? = null

        override fun onComposingText(text: CharSequence) {
            output?.onComposingText(text)
        }

        override fun onFinishComposing() {
            onFinishComposing()
        }

        override fun onCommitText(text: CharSequence) {
            output?.onCommitText(text)
        }

        override fun onDeleteText(beforeLength: Int, afterLength: Int) {
            output?.onDeleteText(beforeLength, afterLength)
        }

        override fun onCandidates(list: List<Candidate>) {
            output?.onCandidates(list)
        }

        override fun onSystemKey(code: Int): Boolean {
            return output?.onSystemKey(code) == true
        }

        override fun onEditorAction(code: Int) {
            output?.onEditorAction(code)
        }
    }
}