package io.github.lee0701.mboard.preset.serialization

object KeyCharSerializer: KeyOutputSerializer {
    override fun serialize(value: Int): String? {
        if(value < 0x20) return null
        return value.toChar().toString()
    }

    override fun deserialize(value: String): Int? {
        return value.toIntOrNull()
    }
}