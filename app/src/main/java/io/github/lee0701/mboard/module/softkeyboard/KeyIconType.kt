package io.github.lee0701.mboard.module.softkeyboard

import kotlinx.serialization.Serializable

@Serializable
enum class KeyIconType {
    Shift,
    Caps,
    Backspace,
    Language,
    Return,
}