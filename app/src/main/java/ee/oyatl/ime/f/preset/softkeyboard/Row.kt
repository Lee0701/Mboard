package ee.oyatl.ime.f.preset.softkeyboard

import kotlinx.serialization.Serializable

@Serializable
data class Row(
    @Serializable val keys: List<RowItem> = listOf(),
)