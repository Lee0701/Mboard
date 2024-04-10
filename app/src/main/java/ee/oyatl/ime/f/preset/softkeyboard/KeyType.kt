package ee.oyatl.ime.f.preset.softkeyboard

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