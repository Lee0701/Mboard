package io.github.lee0701.mboard.keyboard

interface KeyboardListener {
    fun onKeyClick(code: Int, output: String?)
    fun onKeyDown(code: Int, output: String?)
    fun onKeyUp(code: Int, output: String?)
}