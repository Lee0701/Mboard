package io.github.lee0701.mboard.module.hangul

import io.github.lee0701.mboard.input.JamoCombinationTable
import kotlinx.serialization.Serializable

@Serializable
data class JamoCombinationTable(
    val list: List<List<Int>> = listOf(),
) {
    fun inflate(): JamoCombinationTable {
        return JamoCombinationTable(list.map { (k1, k2, v) -> k1 to k2 to v }.toMap())
    }
}