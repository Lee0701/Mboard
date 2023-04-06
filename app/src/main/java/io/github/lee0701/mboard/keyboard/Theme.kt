package io.github.lee0701.mboard.keyboard

data class Theme(
    val keyboard: Int,
    val key: Map<Key.Type, Int> = mapOf(),
    val popup: Int,
)