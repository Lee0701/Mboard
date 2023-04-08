package io.github.lee0701.mboard.view.keyboard

import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.module.KeyType.*
import io.github.lee0701.mboard.module.KeyIconType.*

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
        mapOf(
            Shift to R.drawable.keyic_shift,
            Caps to R.drawable.keyic_shift_lock,
            Backspace to R.drawable.keyic_backspace,
            Language to R.drawable.keyic_language,
            Enter to R.drawable.keyic_enter,
        ),
        R.style.Theme_MBoard_Keyboard_KeyPopup,
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
        mapOf(
            Shift to R.drawable.keyic_shift,
            Caps to R.drawable.keyic_shift_lock,
            Backspace to R.drawable.keyic_backspace,
            Language to R.drawable.keyic_language,
            Enter to R.drawable.keyic_enter,
        ),
        R.style.Theme_MBoard_Keyboard_KeyPopup_Overlay,
    )

    val map: Map<String, Theme> = mapOf(
        "theme_static" to Static,
        "theme_dynamic" to Dynamic,
    )
}