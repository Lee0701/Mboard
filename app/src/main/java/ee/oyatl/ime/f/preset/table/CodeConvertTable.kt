package ee.oyatl.ime.f.preset.table

import ee.oyatl.ime.f.service.KeyboardState
import kotlinx.serialization.Serializable

@Serializable
sealed interface CodeConvertTable {
    fun get(keyCode: Int, state: KeyboardState): Int?
    fun getAllForState(state: KeyboardState): Map<Int, Int>
    fun getReversed(charCode: Int, entryKey: SimpleCodeConvertTable.EntryKey): Int?

    operator fun plus(table: CodeConvertTable): CodeConvertTable
}