package io.github.lee0701.mboard.preset.serialization

import io.github.lee0701.mboard.module.kokr.Hangul
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object HangulJamoSerializer: KSerializer<Int> {
    private const val placeholder = '_'
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("HangulJamo", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Int) {
        val string = when {
            Hangul.isJung(value) -> placeholder.toString() + Hangul.jungToCompatVowel(value.toChar()) + placeholder
            Hangul.isCho(value) -> Hangul.choToCompatConsonant(value.toChar()) + placeholder.toString()
            Hangul.isJong(value) -> placeholder.toString() + Hangul.choToCompatConsonant(value.toChar())
            else -> return HexIntSerializer.serialize(encoder, value)
        }
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): Int {
        val string = decoder.decodeString()
        val first = string.firstOrNull() == placeholder
        val last = string.lastOrNull() == placeholder
        return when {
            first && last -> Hangul.vowelToJung(string.codePointAt(1))
            first -> Hangul.consonantToJong(string.codePointAt(1))
            last -> Hangul.consonantToCho(string.codePointAt(0))
            else -> HexIntSerializer.deserialize(decoder)
        }
    }
}