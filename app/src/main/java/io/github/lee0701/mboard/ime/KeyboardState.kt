package io.github.lee0701.mboard.ime

import android.view.KeyEvent

data class KeyboardState(
    val shiftState: ModifierState = ModifierState(),
    val altState: ModifierState = ModifierState(),
    val controlState: ModifierState = ModifierState(),
    val metaState: ModifierState = ModifierState(),
) {
    val time: Long = System.currentTimeMillis()
    fun asMetaState(): Int {
        var result = 0
        result = result or if(shiftState.pressed) KeyEvent.META_SHIFT_ON else 0
        result = result or if(altState.pressed) KeyEvent.META_ALT_ON else 0
        result = result or if(controlState.pressed) KeyEvent.META_CTRL_ON else 0
        result = result or if(metaState.pressed) KeyEvent.META_META_ON else 0
        return result
    }
}
