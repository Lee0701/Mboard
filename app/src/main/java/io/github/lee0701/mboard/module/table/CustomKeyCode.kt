package io.github.lee0701.mboard.module.table

object CustomKeyCode {

    private val table = mapOf(
        "KEYCODE_COMMA_PERIOD" to 0x102c,
        "KEYCODE_PERIOD_COMMA" to 0x102e,

        "KEYCODE_SEBEOL_390_0" to 0x1030,
        "KEYCODE_SEBEOL_390_1" to 0x1031,
        "KEYCODE_SEBEOL_390_2" to 0x1032,
        "KEYCODE_SEBEOL_390_3" to 0x1033,
    )

    private val revTable = table.entries.associate { (key, value) -> value to key }

    fun values(): Set<Pair<String, Int>> = table.entries.map { (k, v) -> k to v }.toSet()

    fun keyCodeFromString(str: String): Int? = table[str]
    fun keyCodeToString(code: Int): String? = revTable[code]

}