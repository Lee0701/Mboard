package io.github.lee0701.mboard.dictionary

import java.io.*

import java.nio.ByteBuffer

class DiskListTrieDictionary(
    private val data: ByteBuffer
): ListDictionary<DiskListTrieDictionary.Entry>, PredictingDictionary<DiskListTrieDictionary.Entry> {

    constructor(inputStream: InputStream): this(ByteBuffer.wrap(inputStream.readBytes()))

    private val root get() = Node(data.getInt(data.capacity() - 4))

    override fun search(key: List<Byte>): List<Entry> {
        // root
        var p = root
        for(c in key) {
            p = p.children[c] ?: return listOf()
        }
        return p.entries
    }

    override fun predict(key: List<Byte>): List<Pair<List<Byte>, Entry>> {
        var p = root
        for(c in key) {
            p = p.children[c] ?: return listOf()
        }
        return getEntriesRecursive(p, key.toList(), key.size * 2)
    }

    private fun getEntriesRecursive(p: Node, key: List<Byte>, depth: Int): List<Pair<List<Byte>, Entry>> {
        if(depth == 0) return listOf()
        return p.entries.map { key to it } + p.children.flatMap { getEntriesRecursive(it.value, key + it.key, depth-1) }
    }

    override fun entries(): List<Pair<List<Byte>, List<Entry>>> {
        return getEntriesRecursive(root, listOf(), -1).groupBy { it.first }.map { (k, v) -> k to v.map { it.second } }
    }

    fun write(outputStream: OutputStream) {
        outputStream.write(data.array())
    }

    inner class Node(
        private val address: Int,
    ) {
        private val childrenCount: Short = data.getShort(address)
        private val entryAddress: Int = address + 2 + childrenCount*6
        private val entryCount: Short = data.getShort(entryAddress)

        val children: Map<Byte, Node> get() = (0 until childrenCount).associate { i->
            val addr = address + 2 + i*6
            data.get(addr) to Node(data.getInt(addr + 2))
        }
        val entries: List<Entry> get() = run {
            var p = entryAddress + 2
            (0 until entryCount).map { i ->
                val stringCount = data.get(p)
                p += 1
                val strings = mutableListOf<String>()
                for(j in 0 until stringCount) {
                    val len = data.get(p)
                    p += 1
                    strings += (0 until len).map { data.get(p + it) }.toByteArray().decodeToString()
                    p += len
                }
                val intCount = data.get(p + 1)
                p += 1
                val ints = (0 until intCount).map { data.getInt(p + it*4) }
                p += ints.size * 4
                Entry(strings.toList(), ints)
            }
        }
    }

    data class Entry(
        val stringFields: List<String>,
        val intFields: List<Int>,
    ) {
        fun write(dos: DataOutputStream) {
            dos.writeByte(stringFields.size)
            stringFields.forEach { str ->
                val list = str.encodeToByteArray()
                dos.writeByte(list.size)
                dos.write(list)
            }
            dos.writeByte(intFields.size)
            intFields.forEach { int ->
                dos.writeInt(int)
            }
        }
    }

    companion object {
        fun build(dictionary: TrieDictionary<List<Entry>>): DiskListTrieDictionary {
            val os = ByteArrayOutputStream()
            val dos = DataOutputStream(os)

            val rootAddress = dictionary.root.write(dos)
            dos.writeInt(rootAddress)

            return DiskListTrieDictionary(ByteBuffer.wrap(os.toByteArray()))
        }
        private fun TrieDictionary.Node<List<Entry>>.write(dos: DataOutputStream): Int {
            val childrenMap = children.mapValues { (_, node) ->
                node.write(dos)
            }
            val start = dos.size()
            dos.writeByte(children.size)
            childrenMap.forEach { (c, address) ->
                dos.writeByte(c.toInt())
                dos.writeInt(address)
            }
            val entries = this.entry ?: listOf()
            dos.writeByte(entries.size)
            entries.forEach { entry ->
                entry.write(dos)
            }
            return start
        }
    }

}
