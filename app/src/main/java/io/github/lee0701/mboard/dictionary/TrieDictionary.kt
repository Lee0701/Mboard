package io.github.lee0701.mboard.dictionary

abstract class TrieDictionary<T>: Dictionary<T> {

    abstract val root: Node<T>

    override fun search(key: List<Byte>): T? {
        var p = root
        for(c in key) {
            p = p.children[c] ?: return null
        }
        return p.entry
    }

    interface Node<T> {
        val children: Map<Byte, Node<T>>
        val entry: T?
    }

}