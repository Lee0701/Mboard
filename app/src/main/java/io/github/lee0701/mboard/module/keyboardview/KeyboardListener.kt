package io.github.lee0701.mboard.module.keyboardview

import io.github.lee0701.mboard.preset.softkeyboard.Key


interface KeyboardListener {
    fun onKeyClick(key: Key)
    fun onKeyLongClick(key: Key)
    fun onKeyDown(key: Key)
    fun onKeyUp(key: Key)
    fun onKeyFlick(direction: FlickDirection, key: Key)
}