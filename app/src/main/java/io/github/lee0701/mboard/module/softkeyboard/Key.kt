package io.github.lee0701.mboard.module.softkeyboard

import io.github.lee0701.mboard.module.serialization.KeyCodeSerializer
import kotlinx.serialization.Serializable

@Serializable
data class Key(
    @Serializable(with = KeyCodeSerializer::class) val code: Int = 0,
    val output: String? = null,
    val label: String? = output,
    val iconType: KeyIconType? = null,
    val width: Float = 1f,
    val repeatable: Boolean = false,
    val type: KeyType = KeyType.Alphanumeric,
)