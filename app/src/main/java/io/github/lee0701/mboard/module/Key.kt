package io.github.lee0701.mboard.module

import kotlinx.serialization.Serializable

@Serializable
data class Key(
    val code: Int = 0,
    val output: String? = null,
    val label: String? = output,
    val iconType: KeyIconType? = null,
    val width: Float = 1f,
    val repeatable: Boolean = false,
    val type: KeyType = KeyType.Alphanumeric,
)