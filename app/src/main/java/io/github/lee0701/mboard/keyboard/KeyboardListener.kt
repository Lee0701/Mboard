package io.github.lee0701.mboard.keyboard

interface KeyboardListener {
    fun onKeyPressed(code: Int, output: String?)
}