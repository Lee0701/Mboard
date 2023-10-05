package io.github.lee0701.mboard.preset.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object HexIntKeyOutputSerializer: KeyOutputSerializer {
    private const val prefix = "0x"

    override fun serialize(value: Int): String {
        return prefix + value.toString(16).padStart(4, '0')
    }

    override fun deserialize(value: String): Int? {
        if(!value.startsWith(prefix)) return null
        return value.replaceFirst(prefix, "").toIntOrNull(16)
    }
}