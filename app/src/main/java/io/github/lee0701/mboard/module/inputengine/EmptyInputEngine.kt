package io.github.lee0701.mboard.module.inputengine

import android.graphics.drawable.Drawable
import io.github.lee0701.mboard.module.component.InputViewComponent
import io.github.lee0701.mboard.preset.softkeyboard.Keyboard
import io.github.lee0701.mboard.service.KeyboardState

object EmptyInputEngine: InputEngine {

    override var components: List<InputViewComponent> = listOf()
    override var listener: InputEngineListener? = null
    override var symbolsInputEngine: InputEngine? = null
    override var alternativeInputEngine: InputEngine? = null

    override fun onReset() = Unit
    override fun onKey(code: Int, state: KeyboardState) = Unit
    override fun onDelete() = Unit
    override fun onTextAroundCursor(before: String, after: String) = Unit
    override fun getLabels(state: KeyboardState): Map<Int, CharSequence> = mapOf()
    override fun getIcons(state: KeyboardState): Map<Int, Drawable> = mapOf()
    override fun getMoreKeys(state: KeyboardState): Map<Int, Keyboard> = mapOf()

}