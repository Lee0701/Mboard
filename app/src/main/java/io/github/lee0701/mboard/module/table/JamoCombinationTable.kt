package io.github.lee0701.mboard.module.table

import kotlinx.serialization.Serializable

@Serializable
data class JamoCombinationTable(
    val list: List<List<Int>> = listOf(),
) {
    val map: Map<Pair<Int, Int>, Int> = list.associate { (a, b, result) -> (a to b) to result }

    operator fun plus(another: JamoCombinationTable): JamoCombinationTable {
        return JamoCombinationTable(this.list + another.list)
    }
}