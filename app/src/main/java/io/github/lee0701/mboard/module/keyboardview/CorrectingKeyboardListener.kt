package io.github.lee0701.mboard.module.keyboardview

import io.github.lee0701.mboard.preset.softkeyboard.Key


interface CorrectingKeyboardListener: KeyboardListener {
    fun onKeyClick(distances: Map<Key, Double>)
    fun onKeyDown(distances: Map<Key, Double>)
    fun onKeyUp(distances: Map<Key, Double>)
}