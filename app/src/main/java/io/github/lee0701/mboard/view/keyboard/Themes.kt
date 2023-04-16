package io.github.lee0701.mboard.view.keyboard

import com.google.android.material.color.DynamicColors
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.module.softkeyboard.KeyType.*
import io.github.lee0701.mboard.module.softkeyboard.KeyIconType.*

object Themes {
    val Static = Theme(
        R.style.Theme_MBoard_Keyboard,
        mapOf(
            Alphanumeric to R.style.Theme_MBoard_Keyboard_Key,
            AlphanumericAlt to R.style.Theme_MBoard_Keyboard_Key_Mod,
            Modifier to R.style.Theme_MBoard_Keyboard_Key_Mod,
            ModifierAlt to R.style.Theme_MBoard_Keyboard_Key,
            Space to R.style.Theme_MBoard_Keyboard_Key,
            Action to R.style.Theme_MBoard_Keyboard_Key_Return,
        ),
        mapOf(
            Shift to R.drawable.keyic_shift,
            ShiftLock to R.drawable.keyic_shift_lock,
            Caps to R.drawable.keyic_caps,
            Tab to R.drawable.keyic_tab,
            Backspace to R.drawable.keyic_backspace,
            Language to R.drawable.keyic_language,
            Return to R.drawable.keyic_return,
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
            Action to R.style.Theme_MBoard_Keyboard_Key_Return_Overlay,
        ),
        mapOf(
            Shift to R.drawable.keyic_shift,
            ShiftLock to R.drawable.keyic_shift_lock,
            Caps to R.drawable.keyic_caps,
            Tab to R.drawable.keyic_tab,
            Backspace to R.drawable.keyic_backspace,
            Language to R.drawable.keyic_language,
            Return to R.drawable.keyic_return,
        ),
        R.style.Theme_MBoard_Keyboard_KeyPopup_Overlay,
    )

    val map: Map<String, Theme> = mapOf(
        "theme_static" to Static,
        "theme_dynamic" to Dynamic,
    )

    fun ofName(name: String?): Theme {
        return (map[name] ?: Static).let {
            if(!DynamicColors.isDynamicColorAvailable() && it == Dynamic) Static
            else it
        }
    }
}