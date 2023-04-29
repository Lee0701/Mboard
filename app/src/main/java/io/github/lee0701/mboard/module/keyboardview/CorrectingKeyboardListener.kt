package io.github.lee0701.mboard.module.keyboardview

import io.github.lee0701.mboard.service.KeyboardState

interface CorrectingKeyboardListener: KeyboardListener {
    fun onKeyClick(primaryCode: Int, distances: Map<Int, Double>, state: KeyboardState)
    fun onKeyDown(primaryCode: Int, distances: Map<Int, Double>, state: KeyboardState)
    fun onKeyUp(primaryCode: Int, distances: Map<Int, Double>, state: KeyboardState)
}