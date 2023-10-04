package io.github.lee0701.mboard.preset.serialization

import io.github.lee0701.mboard.module.kokr.Hangul
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object HangulJamoSerializer: KeyOutputSerializer {
    private const val placeholder = '_'
    override fun serialize(value: Int): String? {
        return when {
            Hangul.isJung(value) -> placeholder.toString() + Hangul.jungToCompatVowel(value.toChar()) + placeholder
            Hangul.isCho(value) -> Hangul.choToCompatConsonant(value.toChar()) + placeholder.toString()
            Hangul.isJong(value) -> placeholder.toString() + Hangul.choToCompatConsonant(value.toChar())
            else -> null
        }
    }

    override fun deserialize(value: String): Int? {
        if(Hangul.isCompatJamo(value.codePointAt(0))) return value.codePointAt(0)
        val first = value.firstOrNull() == placeholder
        val last = value.lastOrNull() == placeholder
        val char = value.drop(if(first) 1 else 0).dropLast(if(last) 1 else 0).codePointAt(0)
        if(!Hangul.isJamo(char)) return null
        return when {
            first && last -> Hangul.vowelToJung(char)
            first -> Hangul.consonantToJong(char)
            last -> Hangul.consonantToCho(char)
            else -> null
        }
    }
}