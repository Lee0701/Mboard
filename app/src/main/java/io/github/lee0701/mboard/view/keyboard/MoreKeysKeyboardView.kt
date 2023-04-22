package io.github.lee0701.mboard.view.keyboard

interface MoreKeysKeyboardView {
    fun highlight(key: KeyboardView.KeyWrapper)
    fun reset()
    fun findKey(x: Int, y: Int): KeyboardView.KeyWrapper?
}