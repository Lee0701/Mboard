package io.github.lee0701.mboard.service

data class ModifierState(
    val pressed: Boolean = false,
    val locked: Boolean = false,
    val pressing: Boolean = pressed,
)