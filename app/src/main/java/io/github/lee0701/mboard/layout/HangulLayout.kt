package io.github.lee0701.mboard.layout

import android.view.KeyEvent
import io.github.lee0701.mboard.input.CodeConverter.Entry

object HangulLayout {
    val SEBEOL_390 = mapOf(
        KeyEvent.KEYCODE_1 to Entry(0x11c2),
        KeyEvent.KEYCODE_2 to Entry(0x11bb),
        KeyEvent.KEYCODE_3 to Entry(0x11b8),
        KeyEvent.KEYCODE_4 to Entry(0x116d),
        KeyEvent.KEYCODE_5 to Entry(0x1172),
        KeyEvent.KEYCODE_6 to Entry(0x1163),
        KeyEvent.KEYCODE_7 to Entry(0x1168),
        KeyEvent.KEYCODE_8 to Entry(0x1174),
        KeyEvent.KEYCODE_9 to Entry(0x116e),
        KeyEvent.KEYCODE_0 to Entry(0x110f),

        KeyEvent.KEYCODE_Q to Entry(0x11ba),
        KeyEvent.KEYCODE_W to Entry(0x11af),
        KeyEvent.KEYCODE_E to Entry(0x1167),
        KeyEvent.KEYCODE_R to Entry(0x1162),
        KeyEvent.KEYCODE_T to Entry(0x1165),
        KeyEvent.KEYCODE_Y to Entry(0x1105),
        KeyEvent.KEYCODE_U to Entry(0x1103),
        KeyEvent.KEYCODE_I to Entry(0x1106),
        KeyEvent.KEYCODE_O to Entry(0x110e),
        KeyEvent.KEYCODE_P to Entry(0x1111),

        KeyEvent.KEYCODE_A to Entry(0x11bc),
        KeyEvent.KEYCODE_S to Entry(0x11ab),
        KeyEvent.KEYCODE_D to Entry(0x1175),
        KeyEvent.KEYCODE_F to Entry(0x1161),
        KeyEvent.KEYCODE_G to Entry(0x1173),
        KeyEvent.KEYCODE_H to Entry(0x1102),
        KeyEvent.KEYCODE_J to Entry(0x110b),
        KeyEvent.KEYCODE_K to Entry(0x1100),
        KeyEvent.KEYCODE_L to Entry(0x110c),
        KeyEvent.KEYCODE_SEMICOLON to Entry(0x1107),
        KeyEvent.KEYCODE_APOSTROPHE to Entry(0x1110),

        KeyEvent.KEYCODE_Z to Entry(0x11b7),
        KeyEvent.KEYCODE_X to Entry(0x11a8),
        KeyEvent.KEYCODE_C to Entry(0x1166),
        KeyEvent.KEYCODE_V to Entry(0x1169),
        KeyEvent.KEYCODE_B to Entry(0x116e),
        KeyEvent.KEYCODE_N to Entry(0x1109),
        KeyEvent.KEYCODE_M to Entry(0x1112),
        KeyEvent.KEYCODE_COMMA to Entry(0x002c),
        KeyEvent.KEYCODE_PERIOD to Entry(0x002e),
        KeyEvent.KEYCODE_SLASH to Entry(0x1169),

        )

    val COMB_SEBEOL_390 = mapOf(
        0x1100 to 0x1100 to 0x1101,
    )
}