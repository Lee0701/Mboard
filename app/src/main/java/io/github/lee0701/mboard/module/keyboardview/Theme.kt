package io.github.lee0701.mboard.module.keyboardview

import io.github.lee0701.mboard.preset.softkeyboard.KeyIconType
import io.github.lee0701.mboard.preset.softkeyboard.KeyType

data class Theme(
    val keyboardBackground: Int,
    val keyBackground: Map<KeyType, Int> = mapOf(),
    val keyIcon: Map<KeyIconType, Int> = mapOf(),
    val popupBackground: Int,
)