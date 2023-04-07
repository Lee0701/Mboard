package io.github.lee0701.mboard.keyboard

import io.github.lee0701.mboard.module.KeyType

data class Theme(
    val keyboard: Int,
    val key: Map<KeyType, Int> = mapOf(),
    val popup: Int,
)