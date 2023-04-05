package io.github.lee0701.mboard

import android.view.KeyEvent
import io.github.lee0701.mboard.keyboard.*

object Layout {

    val LAYOUT = Keyboard(listOf(
        Row(listOf(
            Key(KeyEvent.KEYCODE_Q, "Q"),
            Key(KeyEvent.KEYCODE_W, "W"),
            Key(KeyEvent.KEYCODE_E, "E"),
            Key(KeyEvent.KEYCODE_R, "R"),
            Key(KeyEvent.KEYCODE_T, "T"),
            Key(KeyEvent.KEYCODE_Y, "Y"),
            Key(KeyEvent.KEYCODE_U, "U"),
            Key(KeyEvent.KEYCODE_I, "I"),
            Key(KeyEvent.KEYCODE_O, "O"),
            Key(KeyEvent.KEYCODE_P, "P"),
        )),
        Row(listOf(
            Key(KeyEvent.KEYCODE_A, "A"),
            Key(KeyEvent.KEYCODE_S, "S"),
            Key(KeyEvent.KEYCODE_D, "D"),
            Key(KeyEvent.KEYCODE_F, "F"),
            Key(KeyEvent.KEYCODE_G, "G"),
            Key(KeyEvent.KEYCODE_H, "H"),
            Key(KeyEvent.KEYCODE_J, "J"),
            Key(KeyEvent.KEYCODE_K, "K"),
            Key(KeyEvent.KEYCODE_L, "L"),
        ), padding = 0.5f),
        Row(listOf(
            Key(KeyEvent.KEYCODE_SHIFT_LEFT, null, icon = R.drawable.keyic_shift, width = 1.5f, type = Key.Type.Modifier),
            Key(KeyEvent.KEYCODE_Z, "Z"),
            Key(KeyEvent.KEYCODE_X, "X"),
            Key(KeyEvent.KEYCODE_C, "C"),
            Key(KeyEvent.KEYCODE_V, "V"),
            Key(KeyEvent.KEYCODE_B, "B"),
            Key(KeyEvent.KEYCODE_N, "N"),
            Key(KeyEvent.KEYCODE_M, "M"),
            Key(KeyEvent.KEYCODE_DEL, null, icon = R.drawable.keyic_backspace, width = 1.5f, type = Key.Type.Modifier),
        )),
        Row(listOf(
            Key(KeyEvent.KEYCODE_SYM, null, "?12", width = 1.5f, type = Key.Type.Modifier),
            Key(KeyEvent.KEYCODE_COMMA, ",", type = Key.Type.Modifier),
            Key(KeyEvent.KEYCODE_LANGUAGE_SWITCH, null, icon = R.drawable.keyic_language),
            Key(KeyEvent.KEYCODE_SPACE, null, "", width = 4f),
            Key(KeyEvent.KEYCODE_PERIOD, ".", type = Key.Type.Modifier),
            Key(KeyEvent.KEYCODE_ENTER, null, icon = R.drawable.keyic_enter, width = 1.5f, type = Key.Type.Return),
        ))
    ), 220f)

}