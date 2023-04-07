package io.github.lee0701.mboard.module

import io.github.lee0701.mboard.keyboard.Key

data class Key(
    val code: Int,
    val output: String?,
    val label: String? = output,
    val icon: Int? = null,
    val width: Float = 1f,
    val repeatable: Boolean = false,
    val type: KeyType = KeyType.Alphanumeric,
) {
    fun inflate(): Key {
        return Key(
            this.code,
            this.output,
            this.label,
            this.icon,
            this.width,
            this.repeatable,
            this.type,
        )
    }
}