package io.github.lee0701.mboard.module.softkeyboard

import io.github.lee0701.mboard.module.serialization.KeyCodeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable()
sealed interface KeyLike {
    val width: Float
}

@SerialName("spacer")
@Serializable
data class Spacer(
    @Serializable override val width: Float = 1f,
): KeyLike

@SerialName("key")
@Serializable
data class Key(
    @Serializable(with = KeyCodeSerializer::class) val code: Int = 0,
    val output: String? = null,
    val label: String? = output,
    val backgroundType: KeyBackgroundType? = null,
    val iconType: KeyIconType? = null,
    override val width: Float = 1f,
    val repeatable: Boolean = false,
    val type: KeyType = KeyType.Alphanumeric,
): KeyLike