package io.github.lee0701.mboard.view.keyboard

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.WindowManager
import io.github.lee0701.mboard.module.softkeyboard.Keyboard

class StackedViewMoreKeysView(
    context: Context,
    attrs: AttributeSet?,
    keyboard: Keyboard,
    theme: Theme,
    listener: KeyboardListener,
): StackedViewKeyboardView(context, attrs, keyboard, theme, listener), MoreKeysKeyboardView {

    override fun highlight(key: KeyWrapper) {
        reset()
        val wrappedKey = wrappedKeys.filterIsInstance<KeyViewWrapper>().find { it == key } ?: return
        wrappedKey.binding.root.isPressed = true
    }

    override fun reset() {
        wrappedKeys.filterIsInstance<KeyViewWrapper>().forEach { it.binding.root.isPressed = false }
    }
}