package io.github.lee0701.mboard.module

import kotlinx.serialization.Serializable

@Serializable
enum class KeyType {
    Alphanumeric,
    AlphanumericAlt,
    Modifier,
    ModifierAlt,
    Space,
    Return,
}