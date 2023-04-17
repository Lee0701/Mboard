package io.github.lee0701.mboard.module.serialization

import android.view.KeyEvent
import io.github.lee0701.mboard.module.table.CustomKeyCode
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object KeyCodeSerializer: KSerializer<Int> {
    override val descriptor = PrimitiveSerialDescriptor("Hexadecimal", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Int) {
        val custom = CustomKeyCode.values().find { it.second == value }?.first
        val string = custom ?: KeyEvent.keyCodeToString(value)
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): Int {
        val string = decoder.decodeString()
        val keyCode = try {
            CustomKeyCode.keyCodeFromString(string) ?: KeyEvent.keyCodeFromString(string)
        } catch(ex: IllegalArgumentException) {
            val keyCode = KeyEvent.keyCodeFromString(string)
            if(keyCode > 0) keyCode else string.toIntOrNull() ?: 0
        }
        return keyCode
    }

}