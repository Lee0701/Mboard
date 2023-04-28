package io.github.lee0701.mboard.preset.softkeyboard

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