package io.github.lee0701.mboard.module.softkeyboard

import kotlinx.serialization.Serializable

@Serializable
data class Row(
    @Serializable val keys: List<KeyLike> = listOf(),
)