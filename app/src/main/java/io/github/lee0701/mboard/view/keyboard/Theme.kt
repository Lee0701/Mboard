package io.github.lee0701.mboard.view.keyboard

import io.github.lee0701.mboard.module.KeyIconType
import io.github.lee0701.mboard.module.KeyType

data class Theme(
    val keyboardBackground: Int,
    val keyBackground: Map<KeyType, Int> = mapOf(),
    val keyIcon: Map<KeyIconType, Int> = mapOf(),
    val popupBackground: Int,
)