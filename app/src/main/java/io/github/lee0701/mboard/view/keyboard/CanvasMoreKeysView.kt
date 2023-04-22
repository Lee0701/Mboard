package io.github.lee0701.mboard.view.keyboard

import android.content.Context
import android.util.AttributeSet
import io.github.lee0701.mboard.module.softkeyboard.Keyboard

class CanvasMoreKeysView(
    context: Context,
    attrs: AttributeSet?,
    keyboard: Keyboard,
    theme: Theme,
    listener: KeyboardListener,
    override val keyboardWidth: Int = 0,
    override val keyboardHeight: Int = 0,
): CanvasKeyboardView(context, attrs, keyboard, theme, listener), MoreKeysKeyboardView {

    init {
        clearCachedKeys()
        cacheKeys()
    }

    override fun highlight(key: KeyWrapper) {
        reset()
        this.keyStates[key.key.code] = true
        invalidate()
    }

    override fun reset() {
        this.keyStates.clear()
    }
}