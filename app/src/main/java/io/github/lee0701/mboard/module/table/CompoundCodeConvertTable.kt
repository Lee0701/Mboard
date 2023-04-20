package io.github.lee0701.mboard.module.table

import io.github.lee0701.mboard.service.KeyboardState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("compound")
data class CompoundCodeConvertTable(
    @Serializable private val tables: List<CodeConvertTable>,
): CodeConvertTable {
    override fun get(keyCode: Int, state: KeyboardState): Int? {
        tables.forEach {
            return it.get(keyCode, state) ?: return@forEach
        }
        return null
    }

    override fun getAllForState(state: KeyboardState): Map<Int, Int> {
        return tables.reversed().fold(mapOf()) { acc, table -> acc + table.getAllForState(state) }
    }
}