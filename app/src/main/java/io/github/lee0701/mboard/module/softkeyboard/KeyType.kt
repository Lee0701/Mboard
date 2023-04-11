package io.github.lee0701.mboard.module.softkeyboard

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