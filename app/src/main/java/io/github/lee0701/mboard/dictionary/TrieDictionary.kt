package io.github.lee0701.mboard.dictionary

data class TrieDictionary(
    override val root: Node,
): AbstractTrieDictionary() {

    data class Node(
        override val children: Map<Int, Node>,
        override val entries: List<Int>,
    ): AbstractTrieDictionary.Node {
        companion object {
            fun build(vocabulary: List<CharSequence>, key: CharSequence): Node {
                val filtered = vocabulary.filter { it.take(key.length) == key }
                val keys = filtered.filter { it.length > key.length }.map { it[key.length] }.toSet()
                val children = keys.associateWith { c -> build(vocabulary, "$key$c") }
                val index = vocabulary.indexOf(key)
                val entries = if(index == -1) emptyList() else listOf(index)
                return Node(children.mapKeys { it.key.code }, entries)
            }
        }
    }

    companion object {
        fun build(vocabulary: List<CharSequence>): TrieDictionary {
            return TrieDictionary(Node.build(vocabulary, ""))
        }
    }
}