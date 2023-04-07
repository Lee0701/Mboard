package io.github.lee0701.mboard.module

import io.github.lee0701.mboard.keyboard.Keyboard

data class Keyboard(
    val rows: List<Row>,
    val height: Float,
) {
    fun inflate(): Keyboard {
        return Keyboard(
            rows = this.rows.map { it.inflate() },
            height = this.height,
        )
    }
}