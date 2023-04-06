package io.github.lee0701.mboard.keyboard

import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.keyboard.Key.Type.*

object Themes {
    val Static = Theme(
        R.style.Theme_MBoard_Keyboard,
        mapOf(
            Alphanumeric to R.style.Theme_MBoard_Keyboard_Key,
            AlphanumericAlt to R.style.Theme_MBoard_Keyboard_Key_Mod,
            Modifier to R.style.Theme_MBoard_Keyboard_Key_Mod,
            ModifierAlt to R.style.Theme_MBoard_Keyboard_Key,
            Space to R.style.Theme_MBoard_Keyboard_Key,
            Return to R.style.Theme_MBoard_Keyboard_Key_Return,
        ),
        R.style.Theme_MBoard_Keyboard_KeyPopup
    )

    val Dynamic = Theme(
        R.style.Theme_MBoard_Keyboard_Overlay,
        mapOf(
            Alphanumeric to R.style.Theme_MBoard_Keyboard_Key_Overlay,
            AlphanumericAlt to R.style.Theme_MBoard_Keyboard_Key_Mod_Overlay,
            Modifier to R.style.Theme_MBoard_Keyboard_Key_Mod_Overlay,
            ModifierAlt to R.style.Theme_MBoard_Keyboard_Key_Overlay,
            Space to R.style.Theme_MBoard_Keyboard_Key_Overlay,
            Return to R.style.Theme_MBoard_Keyboard_Key_Return_Overlay,
            ),
        R.style.Theme_MBoard_Keyboard_KeyPopup_Overlay
    )

    val map: Map<String, Theme> = mapOf(
        "static" to Static,
        "dynamic" to Dynamic,
    )
}