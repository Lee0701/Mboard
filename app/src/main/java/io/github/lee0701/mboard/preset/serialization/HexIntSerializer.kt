package io.github.lee0701.mboard.preset.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object HexIntSerializer: KSerializer<Int> {
    override val descriptor = PrimitiveSerialDescriptor("Hexadecimal", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Int) {
        val string = "0x" + value.toString(16).padStart(4, '0')
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): Int {
        val string = decoder.decodeString()
        return string.replaceFirst("0x", "").toInt(16)
    }

}