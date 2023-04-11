package io.github.lee0701.mboard.view.keyboard

import io.github.lee0701.mboard.module.softkeyboard.KeyIconType
import io.github.lee0701.mboard.module.softkeyboard.KeyType

data class Theme(
    val keyboardBackground: Int,
    val keyBackground: Map<KeyType, Int> = mapOf(),
    val keyIcon: Map<KeyIconType, Int> = mapOf(),
    val popupBackground: Int,
)