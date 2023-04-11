package io.github.lee0701.mboard.dictionary

import java.io.File

fun main() {
    val inFile = File("dict_src/corpus.txt")
    val outDictFile = File("dict_src/dict.bin")
    val outVocabFile = File("dict_src/tokens.tsv")
    val (dictionary, words) = buildDict(inFile, outDictFile, outVocabFile)

    val searchResult = dictionary.search("우리".map { it.code })
    if(searchResult.isNotEmpty()) {
        println(words[searchResult.first()])
    }
}

fun buildDict(inFile: File, outDictFile: File, outVocabFile: File): Pair<AbstractTrieDictionary, List<Pair<String, Int>>> {
    val dictionary = MutableTrieDictionary()
    val vocabulary = mutableMapOf<String, Int>()
    val br = inFile.bufferedReader()
    var i = 0
    while(true) {
        val line = br.readLine() ?: break
        val tokens = line.split(' ')
        tokens.forEach { token ->
            val word = vocabulary.getOrPut(token) { 0 }
            vocabulary += token to word + 1
            i += 1
        }
    }
    val sorted = vocabulary.entries.sortedByDescending { (k, v) -> v }.map { (k, v) -> k to v }
    sorted.forEachIndexed { index, (k, v) ->
        val result = dictionary.search(k.map { it.code })
        dictionary.put(k.map { it.code }, (result + index).distinct())
    }

    println(sorted.size)
    println(dictionary.entries().size)

    val bw = outVocabFile.bufferedWriter()
    sorted.forEach { (key, value) ->
        bw.appendLine("$key\t$value")
    }

    val diskDictionary = DiskTrieDictionary.build(dictionary)
    diskDictionary.write(outDictFile.outputStream())

    return diskDictionary to sorted.toList()
}