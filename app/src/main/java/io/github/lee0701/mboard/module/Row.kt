package io.github.lee0701.mboard.module

import io.github.lee0701.mboard.keyboard.Row

data class Row(
    val keys: List<Key>,
    val padding: Float = 0f,
) {
    fun inflate(): Row {
        return Row(
            keys = this.keys.map { it.inflate() },
            padding = this.padding,
        )
    }
}