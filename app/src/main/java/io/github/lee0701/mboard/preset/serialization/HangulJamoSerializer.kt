package io.github.lee0701.mboard.preset.serialization

import io.github.lee0701.mboard.module.kokr.Hangul
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object HangulJamoSerializer: KeyOutputSerializer {
    private const val placeholder = "_"
    override fun serialize(value: Int): String? {
        return when {
            Hangul.isJung(value) -> placeholder + Hangul.jungToCompatVowel(value.toChar()) + placeholder
            Hangul.isCho(value) -> Hangul.choToCompatConsonant(value.toChar()) + placeholder
            Hangul.isJong(value) -> placeholder + Hangul.choToCompatConsonant(value.toChar())
            else -> null
        }
    }

    override fun deserialize(value: String): Int? {
        if(value == placeholder) return null
        val first = value.startsWith(placeholder)
        val last = value.endsWith(placeholder)
        val char = value.drop(if(first) placeholder.length else 0)
            .dropLast(if(last) placeholder.length else 0)
            .codePointAt(0)
        return when {
            value.length == 1 && Hangul.isCompatJamo(char) -> char
            !Hangul.isCompatJamo(char) -> null
            first && last -> Hangul.vowelToJung(char)
            first -> Hangul.consonantToJong(char)
            last -> Hangul.consonantToCho(char)
            else -> null
        }
    }
}