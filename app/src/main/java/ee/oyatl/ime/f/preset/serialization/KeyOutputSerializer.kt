package ee.oyatl.ime.f.preset.serialization

interface KeyOutputSerializer {
    fun serialize(value: Int): String?
    fun deserialize(value: String): Int?
}