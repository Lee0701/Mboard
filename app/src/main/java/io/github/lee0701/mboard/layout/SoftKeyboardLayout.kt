package io.github.lee0701.mboard.layout

import android.view.KeyEvent
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.keyboard.*

object SoftKeyboardLayout {

    val ROW_BOTTOM = Row(listOf(
        Key(KeyEvent.KEYCODE_SYM, null, "?12", width = 1.5f, type = Key.Type.Modifier),
        Key(KeyEvent.KEYCODE_COMMA, ",", type = Key.Type.AlphanumericAlt),
        Key(KeyEvent.KEYCODE_LANGUAGE_SWITCH, null, icon = R.drawable.keyic_language, type = Key.Type.ModifierAlt),
        Key(KeyEvent.KEYCODE_SPACE, null, "", width = 4f, type = Key.Type.Space),
        Key(KeyEvent.KEYCODE_PERIOD, ".", type = Key.Type.AlphanumericAlt),
        Key(KeyEvent.KEYCODE_ENTER, null, icon = R.drawable.keyic_enter, width = 1.5f, type = Key.Type.Return),
    ))

    val ROW_NUMBERS = Row(listOf(
        Key(KeyEvent.KEYCODE_1, "1"),
        Key(KeyEvent.KEYCODE_2, "2"),
        Key(KeyEvent.KEYCODE_3, "3"),
        Key(KeyEvent.KEYCODE_4, "4"),
        Key(KeyEvent.KEYCODE_5, "5"),
        Key(KeyEvent.KEYCODE_6, "6"),
        Key(KeyEvent.KEYCODE_7, "7"),
        Key(KeyEvent.KEYCODE_8, "8"),
        Key(KeyEvent.KEYCODE_9, "9"),
        Key(KeyEvent.KEYCODE_0, "0"),
    ))

    val LAYOUT_QWERTY_MOBILE = Keyboard(listOf(
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
            Key(KeyEvent.KEYCODE_DEL, null, icon = R.drawable.keyic_backspace, width = 1.5f, repeatable = true, type = Key.Type.Modifier),
        )),
        ROW_BOTTOM,
    ), 220f)

    val LAYOUT_QWERTY_MOBILE_WITH_SEMICOLON = LAYOUT_QWERTY_MOBILE.let { layout ->
        val row2 = layout.rows[1].copy(padding = 0f, keys = layout.rows[1].keys + listOf(Key(KeyEvent.KEYCODE_SEMICOLON, ";")))
        layout.copy(rows = layout.rows.take(1) + listOf(row2) + layout.rows.takeLast(2))
    }

    val LAYOUT_QWERTY_DVORAK_CUSTOM = LAYOUT_QWERTY_MOBILE_WITH_SEMICOLON.let { layout ->
        val keys = layout.rows[2].keys
        val newKeys = keys.take(1) + keys.drop(2).dropLast(1) + listOf(Key(KeyEvent.KEYCODE_SLASH, "/")) + keys.takeLast(1)
        val row3 = layout.rows[2].copy(keys = newKeys)
        layout.copy(rows = layout.rows.take(2) + row3 + layout.rows.takeLast(1))
    }

    val LAYOUT_QWERTY_MOBILE_WITH_NUM = LAYOUT_QWERTY_MOBILE.let { layout ->
        layout.copy(rows = listOf(ROW_NUMBERS) + layout.rows, height = 275f)
    }

    val LAYOUT_QWERTY_SEBEOLSIK_390_MOBILE = LAYOUT_QWERTY_MOBILE_WITH_NUM.let { layout ->
        val additionalKeys2 = listOf(
            Key(KeyEvent.KEYCODE_SEMICOLON, ";"),
        )
        val additionalKeys3Left = listOf(
            Key(KeyEvent.KEYCODE_SHIFT_LEFT, null, icon = R.drawable.keyic_shift, type = Key.Type.Modifier),
        )
        val additionalKeys3Right = listOf(
            Key(CustomKeycode.KEYCODE_SEBEOL_390_0, ""),
            Key(CustomKeycode.KEYCODE_SEBEOL_390_1, ""),
            Key(CustomKeycode.KEYCODE_SEBEOL_390_2, ""),
            Key(CustomKeycode.KEYCODE_SEBEOL_390_3, ""),
            Key(KeyEvent.KEYCODE_DEL, null, icon = R.drawable.keyic_backspace, repeatable = true, type = Key.Type.Modifier),
        )
        val row2 = Row(keys = layout.rows[2].keys + additionalKeys2)
        val row3 = Row(keys = additionalKeys3Left + layout.rows[3].keys.drop(1).dropLast(4) + additionalKeys3Right)
        val bottomRow = Row(listOf(
            Key(KeyEvent.KEYCODE_SYM, null, "?12", width = 1.5f, type = Key.Type.Modifier),
            Key(CustomKeycode.KEYCODE_COMMA_PERIOD, ",", type = Key.Type.AlphanumericAlt),
            Key(KeyEvent.KEYCODE_LANGUAGE_SWITCH, null, icon = R.drawable.keyic_language, type = Key.Type.ModifierAlt),
            Key(KeyEvent.KEYCODE_SPACE, null, "", width = 4f, type = Key.Type.Space),
            Key(KeyEvent.KEYCODE_SLASH, "/", type = Key.Type.AlphanumericAlt),
            Key(KeyEvent.KEYCODE_ENTER, null, icon = R.drawable.keyic_enter, width = 1.5f, type = Key.Type.Return),
        ))
        val rows = listOf(layout.rows[0], layout.rows[1], row2, row3, bottomRow)
        return@let layout.copy(rows = rows)
    }

    val LAYOUT_QWERTY_SEBEOLSIK_391_MOBILE = LAYOUT_QWERTY_MOBILE_WITH_NUM.let { layout ->
        val additionalKeys2 = listOf(
            Key(KeyEvent.KEYCODE_SEMICOLON, ";"),
        )
        val additionalKeys3Left = listOf(
            Key(KeyEvent.KEYCODE_SHIFT_LEFT, null, icon = R.drawable.keyic_shift, type = Key.Type.Modifier),
        )
        val additionalKeys3Right = listOf(
            Key(KeyEvent.KEYCODE_APOSTROPHE, "'"),
            Key(KeyEvent.KEYCODE_DEL, null, icon = R.drawable.keyic_backspace, repeatable = true, type = Key.Type.Modifier),
        )
        val row2 = Row(keys = layout.rows[2].keys + additionalKeys2)
        val row3 = Row(keys = additionalKeys3Left + layout.rows[3].keys.drop(1).dropLast(1) + additionalKeys3Right)
        val bottomRow = Row(listOf(
            Key(KeyEvent.KEYCODE_SYM, null, "?12", width = 1.5f, type = Key.Type.Modifier),
            Key(CustomKeycode.KEYCODE_COMMA_PERIOD, ",", type = Key.Type.AlphanumericAlt),
            Key(KeyEvent.KEYCODE_LANGUAGE_SWITCH, null, icon = R.drawable.keyic_language, type = Key.Type.ModifierAlt),
            Key(KeyEvent.KEYCODE_SPACE, null, "", width = 4f, type = Key.Type.Space),
            Key(KeyEvent.KEYCODE_SLASH, "/", type = Key.Type.AlphanumericAlt),
            Key(KeyEvent.KEYCODE_ENTER, null, icon = R.drawable.keyic_enter, width = 1.5f, type = Key.Type.Return),
        ))
        val rows = listOf(layout.rows[0], layout.rows[1], row2, row3, bottomRow)
        return@let layout.copy(rows = rows)
    }

}