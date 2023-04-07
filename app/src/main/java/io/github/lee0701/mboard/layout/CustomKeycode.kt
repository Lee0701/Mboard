package io.github.lee0701.mboard.layout

import io.github.lee0701.mboard.module.CodeConvertTable.Entry

object CustomKeycode {
    const val KEYCODE_COMMA_PERIOD = 0x102c
    const val KEYCODE_PERIOD_COMMA = 0x102e

    const val KEYCODE_SEBEOL_390_0 = 0x1030
    const val KEYCODE_SEBEOL_390_1 = 0x1031
    const val KEYCODE_SEBEOL_390_2 = 0x1032
    const val KEYCODE_SEBEOL_390_3 = 0x1033

    val LAYOUT_CUSTOM_KEYS = mapOf(
        KEYCODE_COMMA_PERIOD to Entry(0x002c, 0x002e),
    )
}