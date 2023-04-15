package io.github.lee0701.mboard.module.serialization

import android.view.KeyEvent
import io.github.lee0701.mboard.layout.CustomKeycode
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object KeyCodeSerializer: KSerializer<Int> {
    override val descriptor = PrimitiveSerialDescriptor("Hexadecimal", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Int) {
        val custom = CustomKeycode.values().find { it.code == value }?.name
        val string = custom ?: KeyEvent.keyCodeToString(value)
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): Int {
        val string = decoder.decodeString()
        val keyCode = try {
            CustomKeycode.valueOf(string).code
        } catch(ex: IllegalArgumentException) {
            val keyCode = KeyEvent.keyCodeFromString(string)
            if(keyCode > 0) keyCode else string.toIntOrNull() ?: 0
        }
    println(keyCode)
        return keyCode
    }

}