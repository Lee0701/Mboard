package io.github.lee0701.mboard.module.inputengine

import android.graphics.drawable.Drawable
import android.view.KeyCharacterMap
import io.github.lee0701.mboard.module.component.Component
import io.github.lee0701.mboard.preset.softkeyboard.Keyboard
import io.github.lee0701.mboard.service.KeyboardState

class DirectInputEngine(
    override val listener: InputEngine.Listener,
    override val components: List<Component>,
): InputEngine {

    private val keyCharacterMap: KeyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD)

    override var alternativeInputEngine: InputEngine? = null
    override var symbolsInputEngine: InputEngine? = null

    override fun onKey(code: Int, state: KeyboardState) {
        val char = keyCharacterMap.get(code, state.asMetaState())
        if(char > 0) listener.onCommitText(char.toChar().toString())
    }

    override fun onDelete() {
        listener.onDeleteText(1, 0)
    }

    override fun onTextAroundCursor(before: String, after: String) {
    }

    override fun onReset() {
        listener.onFinishComposing()
    }

    override fun getLabels(state: KeyboardState): Map<Int, CharSequence> {
        return getLabels(keyCharacterMap, state)
    }

    override fun getIcons(state: KeyboardState): Map<Int, Drawable> {
        return emptyMap()
    }

    override fun getMoreKeys(state: KeyboardState): Map<Int, Keyboard> {
        return mapOf()
    }

    companion object {
        fun getLabels(keyCharacterMap: KeyCharacterMap, state: KeyboardState): Map<Int, CharSequence> {
            val range = 0 .. 304
            return range.map { keyCode -> keyCode to keyCharacterMap.get(keyCode, state.asMetaState()) }
                .mapNotNull { (keyCode, label) -> (if(label == 0) null else label)?.let { keyCode to it } }.toMap()
                .mapValues { (_, label) -> label.toChar().toString() }
        }
    }
}