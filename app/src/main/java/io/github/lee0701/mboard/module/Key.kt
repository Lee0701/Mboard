package io.github.lee0701.mboard.module

import android.content.Context
import io.github.lee0701.mboard.view.keyboard.Key
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
) {
    fun inflate(): Key {
        return Key(
            this.code,
            this.output,
            this.label,
            this.iconType,
            this.width,
            this.repeatable,
            this.type,
        )
    }
}