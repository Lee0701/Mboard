package io.github.lee0701.mboard.dictionary

open class ReadOnlyTrieDictionary<T>(
    map: Map<List<Byte>, T>
): TrieDictionary<T>() {

    override val root: Node<T> = Node.build(map)

    override fun entries(): List<Pair<List<Byte>, T>> {
        return getEntriesRecursive(root, listOf(), -1)
    }

    private fun getEntriesRecursive(p: Node<T>, key: List<Byte>, depth: Int): List<Pair<List<Byte>, T>> {
        if(depth == 0) return listOf()
        return p.entry?.let { listOfNotNull(key to it) }.orEmpty() +
                p.children.flatMap { getEntriesRecursive(it.value, key + it.key, depth-1) }
    }

    companion object {
        fun <T> of(map: Map<String, T>) = ReadOnlyTrieDictionary(map.mapKeys { (k, _) -> k.encodeToByteArray().toList() })
    }

    data class Node<T>(
        override val children: Map<Byte, Node<T>> = mapOf(),
        override val entry: T? = null
    ): TrieDictionary.Node<T> {
        companion object {
            fun <T> build(map: Map<List<Byte>, T>, key: List<Byte> = listOf()): Node<T> {
                val filtered = map.filterKeys { it.take(key.size) == key }
                val keys = filtered.keys.filter { it.size > key.size }.map { it[key.size] }.toSet()
                val children = keys.associateWith { c -> build(filtered, key + c) }
                return Node(children, map[key])
            }
        }
    }

}