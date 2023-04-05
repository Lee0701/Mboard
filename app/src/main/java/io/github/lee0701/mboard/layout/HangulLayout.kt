package io.github.lee0701.mboard.layout

import android.view.KeyEvent
import io.github.lee0701.mboard.input.CodeConverter.Entry

object HangulLayout {
    val SEBEOL_390 = mapOf(
        KeyEvent.KEYCODE_A to Entry(0x11bc),
        KeyEvent.KEYCODE_S to Entry(0x11ab),
        KeyEvent.KEYCODE_D to Entry(0x1175),
        KeyEvent.KEYCODE_F to Entry(0x1161),
        KeyEvent.KEYCODE_G to Entry(0x1173),
        KeyEvent.KEYCODE_H to Entry(0x1102),
        KeyEvent.KEYCODE_J to Entry(0x110b),
        KeyEvent.KEYCODE_K to Entry(0x1100),
        KeyEvent.KEYCODE_L to Entry(0x110c),
    )

    val COMB_SEBEOL_390 = mapOf(
        0x1100 to 0x1100 to 0x1101,
    )
}