package io.github.lee0701.mboard.layout

import android.view.KeyEvent
import io.github.lee0701.mboard.input.CodeConverter.Entry

object SymbolLayout {

    val LAYOUT_SYMBOLS_G = mapOf(
        KeyEvent.KEYCODE_Q to Entry('1', '~'),
        KeyEvent.KEYCODE_W to Entry('2', '`'),
        KeyEvent.KEYCODE_E to Entry('3', '|'),
        KeyEvent.KEYCODE_R to Entry('4', '•'),
        KeyEvent.KEYCODE_T to Entry('5', '√'),
        KeyEvent.KEYCODE_Y to Entry('6', 'π'),
        KeyEvent.KEYCODE_U to Entry('7', '÷'),
        KeyEvent.KEYCODE_I to Entry('8', '×'),
        KeyEvent.KEYCODE_O to Entry('9', '¶'),
        KeyEvent.KEYCODE_P to Entry('0', '∆'),

        KeyEvent.KEYCODE_A to Entry('@', '€'),
        KeyEvent.KEYCODE_S to Entry('#', '¥'),
        KeyEvent.KEYCODE_D to Entry('£', '$'),
        KeyEvent.KEYCODE_F to Entry('_', '¢'),
        KeyEvent.KEYCODE_G to Entry('&', '^'),
        KeyEvent.KEYCODE_H to Entry('-', '°'),
        KeyEvent.KEYCODE_J to Entry('+', '='),
        KeyEvent.KEYCODE_K to Entry('(', '{'),
        KeyEvent.KEYCODE_L to Entry(')', '}'),
        KeyEvent.KEYCODE_SEMICOLON to Entry('/', '\\'),

        KeyEvent.KEYCODE_Z to Entry('*', '%'),
        KeyEvent.KEYCODE_X to Entry('"', '©'),
        KeyEvent.KEYCODE_C to Entry('\\', '®'),
        KeyEvent.KEYCODE_V to Entry(':', '™'),
        KeyEvent.KEYCODE_B to Entry(';', '✓'),
        KeyEvent.KEYCODE_N to Entry('!', '['),
        KeyEvent.KEYCODE_M to Entry('?', ']'),
    )

}