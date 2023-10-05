package io.github.lee0701.mboard.preset.serialization

interface KeyOutputSerializer {
    fun serialize(value: Int): String?
    fun deserialize(value: String): Int?
}