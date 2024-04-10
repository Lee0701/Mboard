package ee.oyatl.ime.f.module.keyboardview

import ee.oyatl.ime.f.preset.softkeyboard.KeyIconType
import ee.oyatl.ime.f.preset.softkeyboard.KeyType

data class Theme(
    val keyboardBackground: Int,
    val keyBackground: Map<KeyType, Int> = mapOf(),
    val keyIcon: Map<KeyIconType, Int> = mapOf(),
    val popupBackground: Int,
)