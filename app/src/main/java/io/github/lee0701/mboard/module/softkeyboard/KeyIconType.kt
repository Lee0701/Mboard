package io.github.lee0701.mboard.module.softkeyboard

import kotlinx.serialization.Serializable

@Serializable
enum class KeyIconType {
    Shift,
    ShiftLock,
    Caps,
    Tab,
    Backspace,
    Language,
    Return,
}