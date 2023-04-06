package io.github.lee0701.mboard.layout

import android.view.KeyEvent
import io.github.lee0701.mboard.input.CodeConverter.Entry

object HangulLayout {
    val SEBEOL_390 = mapOf(
        KeyEvent.KEYCODE_1 to Entry(0x11c2, 0x11bd),
        KeyEvent.KEYCODE_2 to Entry(0x11bb, 0x0040),
        KeyEvent.KEYCODE_3 to Entry(0x11b8, 0x0023),
        KeyEvent.KEYCODE_4 to Entry(0x116d, 0x0024),
        KeyEvent.KEYCODE_5 to Entry(0x1172, 0x0025),
        KeyEvent.KEYCODE_6 to Entry(0x1163, 0x005e),
        KeyEvent.KEYCODE_7 to Entry(0x1168, 0x0026),
        KeyEvent.KEYCODE_8 to Entry(0x1174, 0x002a),
        KeyEvent.KEYCODE_9 to Entry(0x116e, 0x0028),
        KeyEvent.KEYCODE_0 to Entry(0x110f, 0x0029),

        KeyEvent.KEYCODE_Q to Entry(0x11ba, 0x11c1),
        KeyEvent.KEYCODE_W to Entry(0x11af, 0x11c0),
        KeyEvent.KEYCODE_E to Entry(0x1167, 0x11bf),
        KeyEvent.KEYCODE_R to Entry(0x1162, 0x1164),
        KeyEvent.KEYCODE_T to Entry(0x1165, 0x003b),
        KeyEvent.KEYCODE_Y to Entry(0x1105, 0x003c),
        KeyEvent.KEYCODE_U to Entry(0x1103, 0x0037),
        KeyEvent.KEYCODE_I to Entry(0x1106, 0x0038),
        KeyEvent.KEYCODE_O to Entry(0x110e, 0x0039),
        KeyEvent.KEYCODE_P to Entry(0x1111, 0x003e),

        KeyEvent.KEYCODE_A to Entry(0x11bc, 0x11ae),
        KeyEvent.KEYCODE_S to Entry(0x11ab, 0x11ad),
        KeyEvent.KEYCODE_D to Entry(0x1175, 0x11b0),
        KeyEvent.KEYCODE_F to Entry(0x1161, 0x11a9),
        KeyEvent.KEYCODE_G to Entry(0x1173, 0x002f),
        KeyEvent.KEYCODE_H to Entry(0x1102, 0x0027),
        KeyEvent.KEYCODE_J to Entry(0x110b, 0x0034),
        KeyEvent.KEYCODE_K to Entry(0x1100, 0x0035),
        KeyEvent.KEYCODE_L to Entry(0x110c, 0x0036),
        KeyEvent.KEYCODE_SEMICOLON to Entry(0x1107, 0x003a),
        KeyEvent.KEYCODE_APOSTROPHE to Entry(0x1110, 0x0022),

        KeyEvent.KEYCODE_Z to Entry(0x11b7, 0x11be),
        KeyEvent.KEYCODE_X to Entry(0x11a8, 0x11b9),
        KeyEvent.KEYCODE_C to Entry(0x1166, 0x11b1),
        KeyEvent.KEYCODE_V to Entry(0x1169, 0x11b6),
        KeyEvent.KEYCODE_B to Entry(0x116e, 0x0021),
        KeyEvent.KEYCODE_N to Entry(0x1109, 0x0030),
        KeyEvent.KEYCODE_M to Entry(0x1112, 0x0031),
        KeyEvent.KEYCODE_COMMA to Entry(0x002c, 0x0032),
        KeyEvent.KEYCODE_PERIOD to Entry(0x002e, 0x0033),
        KeyEvent.KEYCODE_SLASH to Entry(0x1169, 0x003f),

        CustomKeycode.KEYCODE_COMMA_PERIOD to Entry(0x002c, 0x002e),
        CustomKeycode.KEYCODE_SEBEOL_390_0 to Entry(0x116e, 0x0030),
        CustomKeycode.KEYCODE_SEBEOL_390_1 to Entry(0x1109, 0x0031),
        CustomKeycode.KEYCODE_SEBEOL_390_2 to Entry(0x1112, 0x0032),
        CustomKeycode.KEYCODE_SEBEOL_390_3 to Entry(0x1110, 0x0033),
        )

    val COMB_SEBEOL_390 = mapOf(
        0x1100 to 0x1100 to 0x1101,	// ㄲ
        0x1103 to 0x1103 to 0x1104,	// ㄸ
        0x1107 to 0x1107 to 0x1108,	// ㅃ
        0x1109 to 0x1109 to 0x110a,	// ㅆ
        0x110c to 0x110c to 0x110d,	// ㅉ

        0x1169 to 0x1161 to 0x116a,	// ㅘ
        0x1169 to 0x1162 to 0x116b,	// ㅙ
        0x1169 to 0x1175 to 0x116c,	// ㅚ
        0x116e to 0x1165 to 0x116f,	// ㅝ
        0x116e to 0x1166 to 0x1170,	// ㅞ
        0x116e to 0x1175 to 0x1171,	// ㅟ
        0x1173 to 0x1175 to 0x1174,	// ㅢ

        0x11a8 to 0x11a8 to 0x11a9,	// ㄲ
        0x11a8 to 0x11ba to 0x11aa,	// ㄳ
        0x11ab to 0x11bd to 0x11ac,	// ㄵ
        0x11ab to 0x11c2 to 0x11ad,	// ㄶ
        0x11af to 0x11a8 to 0x11b0,	// ㄺ
        0x11af to 0x11b7 to 0x11b1,	// ㄻ
        0x11af to 0x11b8 to 0x11b2,	// ㄼ
        0x11af to 0x11ba to 0x11b3,	// ㄽ
        0x11af to 0x11c0 to 0x11b4,	// ㄾ
        0x11af to 0x11c1 to 0x11b5,	// ㄿ
        0x11af to 0x11c2 to 0x11b6,	// ㅀ
        0x11b8 to 0x11ba to 0x11b9,	// ㅄ
        0x11ba to 0x11ba to 0x11bb,	// ㅆ
    )
}