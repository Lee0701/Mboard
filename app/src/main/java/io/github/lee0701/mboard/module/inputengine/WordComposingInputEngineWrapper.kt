package io.github.lee0701.mboard.module.inputengine

import android.graphics.drawable.Drawable
import io.github.lee0701.mboard.module.component.InputViewComponent
import io.github.lee0701.mboard.preset.softkeyboard.Keyboard
import io.github.lee0701.mboard.service.KeyboardState

class WordComposingInputEngineWrapper(
    val inputEngine: InputEngine,
): WordComposingInputEngine {

    override var listener: InputEngine.Listener? = null
    override var components: List<InputViewComponent> = listOf()
    override var symbolsInputEngine: InputEngine? = null
    override var alternativeInputEngine: InputEngine? = null

    override fun onReset() {
        TODO("Not yet implemented")
    }

    override fun onKey(code: Int, state: KeyboardState) {
        TODO("Not yet implemented")
    }

    override fun onDelete() {
        TODO("Not yet implemented")
    }

    override fun onTextAroundCursor(before: String, after: String) {
        TODO("Not yet implemented")
    }

    override fun getLabels(state: KeyboardState): Map<Int, CharSequence> {
        TODO("Not yet implemented")
    }

    override fun getIcons(state: KeyboardState): Map<Int, Drawable> {
        TODO("Not yet implemented")
    }

    override fun getMoreKeys(state: KeyboardState): Map<Int, Keyboard> {
        TODO("Not yet implemented")
    }

    override val currentComposing: String
        get() = TODO("Not yet implemented")
}