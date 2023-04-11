package io.github.lee0701.mboard.dictionary

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer

class DiskTrieDictionary(
    private val data: ByteBuffer,
): AbstractTrieDictionary() {

    override val root get() = Node(data.getInt(data.capacity() - 4))

    fun write(outputStream: OutputStream) {
        outputStream.write(this.data.array())
    }

    inner class Node(
        private val address: Int,
    ): AbstractTrieDictionary.Node {
        private val childrenCount: Short = data.getShort(address)
        private val entriesAddress: Int = address + 2 + childrenCount*8
        private val entriesCount: Short = data.getShort(entriesAddress)

        override val children: Map<Int, Node> get() = (0 until childrenCount).associate { i ->
            val addr = address + 2 + i*8
            data.getInt(addr) to Node(data.getInt(addr + 4))
        }
        override val entries: List<Int> get() = (0 until entriesCount).map { i ->
            val addr = entriesAddress + 2 + i*4
            data.getInt(addr)
        }
    }

    companion object {
        fun build(dictionary: AbstractTrieDictionary): DiskTrieDictionary {
            val os = ByteArrayOutputStream()
            val dos = DataOutputStream(os)

            val rootAddress = dictionary.root.write(dos)
            dos.writeInt(rootAddress)

            return DiskTrieDictionary(ByteBuffer.wrap(os.toByteArray()))
        }
        private fun AbstractTrieDictionary.Node.write(dos: DataOutputStream): Int {
            val childrenMap = children.mapValues { (_, node) -> node.write(dos) }
            val start = dos.size()
            dos.writeShort(children.size)
            childrenMap.forEach { (c, addr) ->
                dos.writeInt(c)
                dos.writeInt(addr)
            }
            val entries = this.entries
            dos.writeShort(entries.size)
            entries.forEach { entry -> dos.writeInt(entry) }
            return start
        }
    }
}