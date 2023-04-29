package io.github.lee0701.mboard.module.inputengine

import android.graphics.drawable.Drawable
import io.github.lee0701.mboard.dictionary.AbstractTrieDictionary
import io.github.lee0701.mboard.module.component.InputViewComponent
import io.github.lee0701.mboard.preset.softkeyboard.Keyboard
import io.github.lee0701.mboard.service.KeyboardState
import java.text.Normalizer

class CorrectingInputEngine(
    val inputEngine: WordComposingInputEngine,
    val dictionary: AbstractTrieDictionary,
): InputEngine {

    override var components: List<InputViewComponent> = inputEngine.components
    override var listener: InputEngine.Listener? = null
    override var symbolsInputEngine: InputEngine? = null
    override var alternativeInputEngine: InputEngine? = null

    private val currentComposing: String get() = inputEngine.currentComposing
    private val currentKey: List<Int> get() =
        Normalizer.normalize(currentComposing, Normalizer.Form.NFD).map { it.code }

    fun onKey(code: Int, keyDistances: Map<Int, Double>, state: KeyboardState) {
        val result = dictionary.searchPrefix(currentKey)
        keyDistances.filterKeys { it in result }.forEach { (key, dist) ->
            println("$key: $dist")
        }
    }

    override fun onReset() {
        inputEngine.onReset()
    }

    override fun onKey(code: Int, state: KeyboardState) {
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
}