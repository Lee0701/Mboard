package io.github.lee0701.mboard.keyboard

interface KeyboardListener {
    fun onKey(code: Int, output: String?)
}