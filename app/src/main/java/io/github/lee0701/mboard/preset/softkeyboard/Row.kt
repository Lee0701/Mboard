package io.github.lee0701.mboard.preset.softkeyboard

import kotlinx.serialization.Serializable

@Serializable
data class Row(
    @Serializable val keys: List<RowItem> = listOf(),
)