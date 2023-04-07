package io.github.lee0701.mboard.layout

import android.view.KeyEvent
import io.github.lee0701.mboard.input.CodeConverter.Entry

object LatinLayout {

    val LAYOUT_LATIN_DVORAK = mapOf(
        KeyEvent.KEYCODE_1 to Entry(0x0031, 0x0021), // 1, !
        KeyEvent.KEYCODE_2 to Entry(0x0032, 0x0040), // 2, @
        KeyEvent.KEYCODE_3 to Entry(0x0033, 0x0023), // 3, #
        KeyEvent.KEYCODE_4 to Entry(0x0034, 0x0024), // 4, $
        KeyEvent.KEYCODE_5 to Entry(0x0035, 0x0025), // 5, %
        KeyEvent.KEYCODE_6 to Entry(0x0036, 0x005e), // 6, ^
        KeyEvent.KEYCODE_7 to Entry(0x0037, 0x0026), // 7, &
        KeyEvent.KEYCODE_8 to Entry(0x0038, 0x002a), // 8, *
        KeyEvent.KEYCODE_9 to Entry(0x0039, 0x0028), // 9, (
        KeyEvent.KEYCODE_0 to Entry(0x0030, 0x0029), // 0, )
        KeyEvent.KEYCODE_Q to Entry(0x0027, 0x0022), // ', "
        KeyEvent.KEYCODE_W to Entry(0x002c, 0x003c), // ,, <
        KeyEvent.KEYCODE_E to Entry(0x002e, 0x003e), // ., >
        KeyEvent.KEYCODE_R to Entry(0x0070, 0x0050), // p, P
        KeyEvent.KEYCODE_T to Entry(0x0079, 0x0059), // y, Y
        KeyEvent.KEYCODE_Y to Entry(0x0066, 0x0046), // f, F
        KeyEvent.KEYCODE_U to Entry(0x0067, 0x0047), // g, G
        KeyEvent.KEYCODE_I to Entry(0x0063, 0x0043), // c, C
        KeyEvent.KEYCODE_O to Entry(0x0072, 0x0052), // r, R
        KeyEvent.KEYCODE_P to Entry(0x006c, 0x004c), // l, L
        KeyEvent.KEYCODE_A to Entry(0x0061, 0x0041), // a, A
        KeyEvent.KEYCODE_S to Entry(0x006f, 0x004f), // o, O
        KeyEvent.KEYCODE_D to Entry(0x0065, 0x0045), // e, E
        KeyEvent.KEYCODE_F to Entry(0x0075, 0x0055), // u, U
        KeyEvent.KEYCODE_G to Entry(0x0069, 0x0049), // i, I
        KeyEvent.KEYCODE_H to Entry(0x0064, 0x0044), // d, D
        KeyEvent.KEYCODE_J to Entry(0x0068, 0x0048), // h, H
        KeyEvent.KEYCODE_K to Entry(0x0074, 0x0054), // t, T
        KeyEvent.KEYCODE_L to Entry(0x006e, 0x004e), // n, N
        KeyEvent.KEYCODE_SEMICOLON to Entry(0x0073, 0x0053), // s, S
        KeyEvent.KEYCODE_APOSTROPHE to Entry(0x002d, 0x005f), // -, _
        KeyEvent.KEYCODE_Z to Entry(0x003b, 0x003a), // ;, :
        KeyEvent.KEYCODE_X to Entry(0x0071, 0x0051), // q, Q
        KeyEvent.KEYCODE_C to Entry(0x006a, 0x004a), // j, J
        KeyEvent.KEYCODE_V to Entry(0x006b, 0x004b), // k, K
        KeyEvent.KEYCODE_B to Entry(0x0078, 0x0058), // x, X
        KeyEvent.KEYCODE_N to Entry(0x0062, 0x0042), // b, B
        KeyEvent.KEYCODE_M to Entry(0x006d, 0x004d), // m, M
        KeyEvent.KEYCODE_COMMA to Entry(0x0077, 0x0057), // w, W
        KeyEvent.KEYCODE_PERIOD to Entry(0x0076, 0x0056), // v, V
        KeyEvent.KEYCODE_SLASH to Entry(0x007a, 0x005a), // z, Z
        KeyEvent.KEYCODE_MINUS to Entry(0x005b, 0x007b), // [, {
        KeyEvent.KEYCODE_NUM to Entry(0x005d, 0x007d), // ], }
        KeyEvent.KEYCODE_LEFT_BRACKET to Entry(0x002f, 0x003f), // /, ?
        KeyEvent.KEYCODE_RIGHT_BRACKET to Entry(0x003d, 0x002b), // =, +
    )

    val LAYOUT_LATIN_COLEMAK = mapOf(
        KeyEvent.KEYCODE_1 to Entry(0x0031, 0x0021), // 1, !
        KeyEvent.KEYCODE_2 to Entry(0x0032, 0x0040), // 2, @
        KeyEvent.KEYCODE_3 to Entry(0x0033, 0x0023), // 3, #
        KeyEvent.KEYCODE_4 to Entry(0x0034, 0x0024), // 4, $
        KeyEvent.KEYCODE_5 to Entry(0x0035, 0x0025), // 5, %
        KeyEvent.KEYCODE_6 to Entry(0x0036, 0x005e), // 6, ^
        KeyEvent.KEYCODE_7 to Entry(0x0037, 0x0026), // 7, &
        KeyEvent.KEYCODE_8 to Entry(0x0038, 0x002a), // 8, *
        KeyEvent.KEYCODE_9 to Entry(0x0039, 0x0028), // 9, (
        KeyEvent.KEYCODE_0 to Entry(0x0030, 0x0029), // 0, )
        KeyEvent.KEYCODE_Q to Entry(0x0071, 0x0051), // q, Q
        KeyEvent.KEYCODE_W to Entry(0x0077, 0x0057), // w, W
        KeyEvent.KEYCODE_E to Entry(0x0066, 0x0046), // f, F
        KeyEvent.KEYCODE_R to Entry(0x0070, 0x0050), // p, P
        KeyEvent.KEYCODE_T to Entry(0x0067, 0x0047), // g, G
        KeyEvent.KEYCODE_Y to Entry(0x006a, 0x004a), // j, J
        KeyEvent.KEYCODE_U to Entry(0x006c, 0x004c), // l, L
        KeyEvent.KEYCODE_I to Entry(0x0075, 0x0055), // u, U
        KeyEvent.KEYCODE_O to Entry(0x0079, 0x0059), // y, Y
        KeyEvent.KEYCODE_P to Entry(0x003b, 0x003a), // ;, :
        KeyEvent.KEYCODE_A to Entry(0x0061, 0x0041), // a, A
        KeyEvent.KEYCODE_S to Entry(0x0072, 0x0052), // r, R
        KeyEvent.KEYCODE_D to Entry(0x0073, 0x0053), // s, S
        KeyEvent.KEYCODE_F to Entry(0x0074, 0x0054), // t, T
        KeyEvent.KEYCODE_G to Entry(0x0064, 0x0044), // d, D
        KeyEvent.KEYCODE_H to Entry(0x0068, 0x0048), // h, H
        KeyEvent.KEYCODE_J to Entry(0x006e, 0x004e), // n, N
        KeyEvent.KEYCODE_K to Entry(0x0065, 0x0045), // e, E
        KeyEvent.KEYCODE_L to Entry(0x0069, 0x0049), // i, I
        KeyEvent.KEYCODE_SEMICOLON to Entry(0x006f, 0x004f), // o, O
        KeyEvent.KEYCODE_Z to Entry(0x007a, 0x005a), // z, Z
        KeyEvent.KEYCODE_X to Entry(0x0078, 0x0058), // x, X
        KeyEvent.KEYCODE_C to Entry(0x0063, 0x0043), // c, C
        KeyEvent.KEYCODE_V to Entry(0x0076, 0x0056), // v, V
        KeyEvent.KEYCODE_B to Entry(0x0062, 0x0042), // b, B
        KeyEvent.KEYCODE_N to Entry(0x006b, 0x004b), // k, K
        KeyEvent.KEYCODE_M to Entry(0x006d, 0x004d), // m, M
        KeyEvent.KEYCODE_COMMA to Entry(0x002c, 0x003c), // ,, <
        KeyEvent.KEYCODE_PERIOD to Entry(0x002e, 0x003e), // ., >
        KeyEvent.KEYCODE_SLASH to Entry(0x002f, 0x003f), // /, ?
    )

}