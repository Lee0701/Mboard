package io.github.lee0701.mboard

import android.view.KeyEvent

object Layout {

    val LAYOUT = Keyboard(listOf(
        Keyboard.Row(listOf(
            Keyboard.Key(KeyEvent.KEYCODE_Q, "Q"),
            Keyboard.Key(KeyEvent.KEYCODE_W, "W"),
            Keyboard.Key(KeyEvent.KEYCODE_E, "E"),
            Keyboard.Key(KeyEvent.KEYCODE_R, "R"),
            Keyboard.Key(KeyEvent.KEYCODE_T, "T"),
            Keyboard.Key(KeyEvent.KEYCODE_Y, "Y"),
            Keyboard.Key(KeyEvent.KEYCODE_U, "U"),
            Keyboard.Key(KeyEvent.KEYCODE_I, "I"),
            Keyboard.Key(KeyEvent.KEYCODE_O, "O"),
            Keyboard.Key(KeyEvent.KEYCODE_P, "P"),
        )),
        Keyboard.Row(listOf(
            Keyboard.Key(KeyEvent.KEYCODE_A, "A"),
            Keyboard.Key(KeyEvent.KEYCODE_S, "S"),
            Keyboard.Key(KeyEvent.KEYCODE_D, "D"),
            Keyboard.Key(KeyEvent.KEYCODE_F, "F"),
            Keyboard.Key(KeyEvent.KEYCODE_G, "G"),
            Keyboard.Key(KeyEvent.KEYCODE_H, "H"),
            Keyboard.Key(KeyEvent.KEYCODE_J, "J"),
            Keyboard.Key(KeyEvent.KEYCODE_K, "K"),
            Keyboard.Key(KeyEvent.KEYCODE_L, "L"),
        ), padding = 0.5f),
        Keyboard.Row(listOf(
            Keyboard.Key(KeyEvent.KEYCODE_SHIFT_LEFT, null, icon = R.drawable.keyic_shift, width = 1.5f),
            Keyboard.Key(KeyEvent.KEYCODE_Z, "Z"),
            Keyboard.Key(KeyEvent.KEYCODE_X, "X"),
            Keyboard.Key(KeyEvent.KEYCODE_C, "C"),
            Keyboard.Key(KeyEvent.KEYCODE_V, "V"),
            Keyboard.Key(KeyEvent.KEYCODE_B, "B"),
            Keyboard.Key(KeyEvent.KEYCODE_N, "N"),
            Keyboard.Key(KeyEvent.KEYCODE_M, "M"),
            Keyboard.Key(KeyEvent.KEYCODE_DEL, null, icon = R.drawable.keyic_backspace, width = 1.5f),
        )),
        Keyboard.Row(listOf(
            Keyboard.Key(KeyEvent.KEYCODE_SYM, null, "?12", width = 1.5f),
            Keyboard.Key(KeyEvent.KEYCODE_COMMA, ","),
            Keyboard.Key(KeyEvent.KEYCODE_LANGUAGE_SWITCH, null, icon = R.drawable.keyic_language),
            Keyboard.Key(KeyEvent.KEYCODE_SPACE, null, "", width = 4f),
            Keyboard.Key(KeyEvent.KEYCODE_PERIOD, "."),
            Keyboard.Key(KeyEvent.KEYCODE_ENTER, null, icon = R.drawable.keyic_enter, width = 1.5f),
        ))
    ), 240f)

}