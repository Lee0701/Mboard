package io.github.lee0701.mboard.preset.softkeyboard

import kotlinx.serialization.Serializable

@Serializable
enum class KeyType {
    Alphanumeric,
    AlphanumericAlt,
    Modifier,
    ModifierAlt,
    Space,
    Action,
}