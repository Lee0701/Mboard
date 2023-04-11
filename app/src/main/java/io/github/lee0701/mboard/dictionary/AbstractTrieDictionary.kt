package io.github.lee0701.mboard.dictionary

abstract class AbstractTrieDictionary: Dictionary {

    abstract val root: Node

    override fun search(key: List<Int>): List<Int> {
        var p = root
        for(c in key) {
            p = p.children[c] ?: return emptyList()
        }
        return p.entries
    }

    override fun entries(): Map<CharSequence, List<Int>> {
        fun recursive(p: Node, key: CharSequence, depth: Int): List<Pair<CharSequence, List<Int>>> {
            if(depth == 0) return emptyList()
            return listOfNotNull(key to p.entries) + p.children.flatMap { child ->
                recursive(child.value, "$key${child.key}", depth-1)
                    .filter { it.first.isNotBlank() && it.second.isNotEmpty() }
            }
        }
        return recursive(root, "", -1).toMap()
    }

    interface Node {
        val children: Map<Int, Node>
        val entries: List<Int>
    }
}