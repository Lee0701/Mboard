package io.github.lee0701.mboard.dictionary

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer

class DiskTrieDictionary(
    private val data: ByteBuffer,
): AbstractTrieDictionary() {

    constructor(inputStream: InputStream): this(ByteBuffer.wrap(inputStream.readBytes()))

    override val root get() = Node(data.getInt(data.capacity() - 4))

    fun write(outputStream: OutputStream) {
        outputStream.write(this.data.array())
    }

    inner class Node(
        private val address: Int,
    ): AbstractTrieDictionary.Node {
        private val childrenCount: Int = data.getInt(address)
        private val entriesAddress: Int = address + 4 + childrenCount*8
        private val entriesCount: Int = data.getInt(entriesAddress)

        override val children: Map<Int, Node> get() = (0 until childrenCount).associate { i ->
            val addr = address + 4 + i*8
            data.getInt(addr) to Node(data.getInt(addr + 4))
        }
        override val entries: Map<Int, Int> get() = (0 until entriesCount).associate { i ->
            val addr = entriesAddress + 4 + i*8
            data.getInt(addr) to data.getInt(addr + 4)
        }
    }

    companion object {
        fun build(dictionary: AbstractTrieDictionary): DiskTrieDictionary {
            val os = ByteArrayOutputStream()
            val dos = DataOutputStream(os)

            "dict1".forEach { dos.writeByte(it.code) }

            val rootAddress = dictionary.root.write(dos)
            dos.writeInt(rootAddress)

            return DiskTrieDictionary(ByteBuffer.wrap(os.toByteArray()))
        }

        private fun AbstractTrieDictionary.Node.write(dos: DataOutputStream): Int {
            val childrenMap = children.mapValues { (_, node) -> node.write(dos) }
            val start = dos.size()
            dos.writeInt(children.size)
            childrenMap.forEach { (c, addr) ->
                dos.writeInt(c)
                dos.writeInt(addr)
            }
            dos.writeInt(entries.size)
            entries.forEach { entry ->
                dos.writeInt(entry.key)
                dos.writeInt(entry.value)
            }
            return start
        }
    }
}