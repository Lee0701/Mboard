package io.github.lee0701.mboard.dictionary

abstract class MutableTrieDictionary<T>: TrieDictionary<T>(), MutableDictionary<T> {

    override val root: Node<T> = Node()

    override fun put(key: List<Byte>, value: T) {
        var p = root
        for(c in key) {
            p = p.children.getOrPut(c) { Node() }
        }
        p.entry = value
    }

    override fun remove(key: List<Byte>) {
        var p = root
        for(c in key) {
            p = p.children[c] ?: return
        }
        p.entry = null
    }

    data class Node<T>(
        override val children: MutableMap<Byte, Node<T>> = mutableMapOf(),
        override var entry: T? = null,
    ): TrieDictionary.Node<T>

}