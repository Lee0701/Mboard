package io.github.lee0701.mboard.dictionary

class MutableTrieDictionary(
): AbstractTrieDictionary(), MutableDictionary {

    override val root: Node = Node()

    override fun put(key: List<Int>, value: Map<Int, Int>) {
        var p = root
        for(c in key) {
            p = p.children.getOrPut(c) { Node() }
        }
        p.entries = value.toMutableMap()
    }

    data class Node(
        override var children: MutableMap<Int, Node> = mutableMapOf(),
        override var entries: MutableMap<Int, Int> = mutableMapOf(),
    ): AbstractTrieDictionary.Node

}