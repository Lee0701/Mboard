package io.github.lee0701.mboard.module

import io.github.lee0701.mboard.view.keyboard_legacy.Row
import kotlinx.serialization.Serializable

@Serializable
data class Row(
    val keys: List<Key> = listOf(),
    val padding: Float = 0f,
) {
    fun inflate(): Row {
        return Row(
            keys = this.keys.map { it.inflate() },
            padding = this.padding,
        )
    }
}