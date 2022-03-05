package io.github.lee0701.mboard

interface KeyboardListener {
    fun onKey(code: Int, output: String?)
}