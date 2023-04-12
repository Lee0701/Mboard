package io.github.lee0701.mboard.dictionary

abstract class AbstractTrieDictionary: Dictionary {

    abstract val root: Node

    override fun search(key: List<Int>): Map<Int, Int> {
        var p = root
        for(c in key) {
            p = p.children[c] ?: return emptyMap()
        }
        return p.entries
    }

    override fun searchPrefix(key: List<Int>): Map<Int, Int> {
        var p = root
        for(c in key) {
            p = p.children[c] ?: return emptyMap()
        }
        return recursive(p, listOf(), 5)
            .flatMap { it.second.entries.map { (index, freq) -> index to freq } }.toMap()
    }

    override fun entries(): Map<List<Int>, Map<Int, Int>> {
        return recursive(root, listOf(), -1).toMap()
    }

    private fun recursive(p: Node, key: List<Int>, depth: Int): List<Pair<List<Int>, Map<Int, Int>>> {
        if(depth == 0) return emptyList()
        return listOfNotNull(key to p.entries) + p.children.flatMap { child ->
            recursive(child.value, key + child.key, depth-1)
                .filter { it.first.isNotEmpty() && it.second.isNotEmpty() }
        }
    }

    interface Node {
        val children: Map<Int, Node>
        val entries: Map<Int, Int>
    }
}