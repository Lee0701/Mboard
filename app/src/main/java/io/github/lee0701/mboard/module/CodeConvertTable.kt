package io.github.lee0701.mboard.module

import io.github.lee0701.mboard.input.CodeConverter

data class CodeConvertTable(
    val map: Map<Int, Entry> = mapOf(),
) {
    data class Entry(
        val base: Int,
        val shift: Int = base,
        val capsLocked: Int = shift,
        val alt: Int = base,
        val altShift: Int = shift,
    ) {
        fun inflate(): CodeConverter.Entry {
            return CodeConverter.Entry(
                base = this.base,
                shift = this.shift,
                capsLocked = this.capsLocked,
                alt = this.alt,
                altShift = this.altShift,
            )
        }
    }

    fun inflate(): CodeConverter {
        return CodeConverter(this.map.mapValues { it.value.inflate() })
    }
}