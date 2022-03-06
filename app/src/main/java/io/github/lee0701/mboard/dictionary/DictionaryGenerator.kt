package io.github.lee0701.mboard.dictionary

import java.io.File

fun main(args: Array<String>) {
    if(args.size < 2) return println("Usage: java {filename} inFile outFile")
    val inputFile = File(args[0])
    val outputFile = File(args[1])

    fun buildEntry(fields: List<String>): Pair<List<Byte>, DiskListTrieDictionary.Entry> {
        val key = fields.first().encodeToByteArray().toList()
        val mappedFields = fields.drop(1).map { it.toIntOrNull() ?: it }
        val stringFields = mappedFields.filterIsInstance<String>()
        val intFields = mappedFields.filterIsInstance<Int>()
        return key to DiskListTrieDictionary.Entry(stringFields, intFields)
    }
    val entries = inputFile.bufferedReader().readLines()
        .asSequence()
        .map { it.split('\t') }
        .filter { it.isNotEmpty() && it.first().isNotEmpty() }
        .map { buildEntry(it) }
        .groupBy { (k) -> k }
        .map { (k, v) -> k to v.map { it.second } }
        .toMap()
    val trieDictionary = ReadOnlyTrieDictionary(entries)
    val diskDictionary = DiskListTrieDictionary.build(trieDictionary)
    diskDictionary.write(outputFile.outputStream())
}