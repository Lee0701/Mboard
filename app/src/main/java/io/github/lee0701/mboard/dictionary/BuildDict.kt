package io.github.lee0701.mboard.dictionary

import java.io.DataOutputStream
import java.io.File
import java.nio.ByteBuffer

fun main() {
    val testSearch = true
    val genPrefixDict = false
    val genNgramDict = true

    if(genPrefixDict) {
        val inFile = File("dict_src/corpus.txt")
        val outDictFile = File("dict_src/dict-prefix.bin")
        val outVocabFile = File("dict_src/vocab.tsv")
        val (dictionary, vocab) = buildPrefixDict(inFile, outDictFile, outVocabFile, 0)
    }
    if(genNgramDict) {
        val inFile = File("dict_src/corpus_10k.txt")
        val inVocabFile = File("dict_src/vocab.tsv")
        val outFile = File("dict_src/dict-ngram.bin")
        val (dictionary, vocab) = buildNgramDict(inFile, inVocabFile, outFile, 3)
    }
    if(testSearch) {
        val prefixDict = DiskTrieDictionary(ByteBuffer.wrap(File("dict_src/dict-prefix.bin").readBytes()))
        val ngramDict = DiskTrieDictionary(ByteBuffer.wrap(File("dict_src/dict-ngram.bin").readBytes()))
        val vocab = File("dict_src/vocab.tsv").bufferedReader().readLines()
            .map { it.split('\t') }.filter { it.size == 2 }.mapIndexed { i, (k, v) -> k to i }.toMap()
        val revVocab = vocab.map { (k, v) -> v to k }.toMap()
        val searchResult = ngramDict.search("우리 나라 에 는".split(' ').map { vocab[it] ?: -1 })
        println(searchResult.map { revVocab[it] })
    }
}

fun buildPrefixDict(
    inFile: File,
    outDictFile: File,
    outVocabFile: File,
    minFreq: Int = 10,
): Pair<AbstractTrieDictionary, List<Pair<String, Int>>> {
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
    val sorted = vocabulary.entries
        .sortedByDescending { (k, v) -> v }
        .map { (k, v) -> k to v }
        .filter { (k, v) -> v >= minFreq }

    sorted.forEachIndexed { index, (k, v) ->
        val result = dictionary.search(k.map { it.code })
        dictionary.put(k.map { it.code }, (result + index).distinct())
    }

    val bw = outVocabFile.bufferedWriter()
    sorted.forEach { (key, value) ->
        bw.appendLine("$key\t$value")
    }

    val diskDictionary = DiskTrieDictionary.build(dictionary)
    diskDictionary.write(outDictFile.outputStream())

    return diskDictionary to sorted.toList()
}

fun buildNgramDict(
    inFile: File,
    inVocabFile: File,
    outFile: File,
    grams: Int = 3,
): Pair<AbstractTrieDictionary, Map<String, Int>> {
    val vocabulary = inVocabFile.bufferedReader().readLines()
        .map { it.split('\t') }.filter { it.size == 2 }.mapIndexed { i, (k, v) -> k to i }.toMap()
    val ngramDict = MutableTrieDictionary()
    val br = inFile.bufferedReader()
    var i = 0
    while(true) {
        val line = br.readLine() ?: break
        val tokens = line.split(' ').map { vocabulary[it] ?: -1 }
        tokens.forEachIndexed { j, token ->
            (0 .. grams).map { i ->
                val sliced = tokens.drop(j).take(i)
                if(sliced.size >= 2) {
                    val key = sliced.dropLast(1)
                    val existing = ngramDict.search(key)
                    ngramDict.put(key, (existing + sliced.last()).distinct())
                }
            }
        }
        i += 1
        if(i % 1000 == 0) println(i)
    }
    val diskDictionary = DiskTrieDictionary.build(ngramDict)
    diskDictionary.write(outFile.outputStream())
    return diskDictionary to vocabulary
}

fun generateHanjaDictionary(inFile: File, freqHanjaFile: File, freqHanjaeoFile: File, outFile: File) {
    val dictionary = MutableTrieDictionary()
    val hanja = inFile.bufferedReader()
    val freqHanja = freqHanjaFile.bufferedReader().readLines()
        .map { it.split(":") }.map { it[0] to it[1].toInt() }.toMap()
    val freqHanjaeo = freqHanjaeoFile.bufferedReader().readLines()
        .map { it.split(":") }.map { it[0] to it[1].toInt() }.toMap()
    val comment = mutableListOf<String>()
    while(true) {
        val line = hanja.readLine() ?: break
        if(line.isEmpty()) continue
        if(line[0] == '#') comment += line
        else {
            val items = line.split(':')
            if(items.size < 3) continue
            val key = items[0]
            val result = items[1]
            val extra = items[2]
            val frequency = freqHanja[result] ?: freqHanjaeo[result]?.let { it % 10000 } ?: 0
        }
    }
    val outputDir = File("output")
    outputDir.mkdirs()
    val dos = DataOutputStream(File(outputDir, "dict.bin").outputStream())
    dos.write((comment.joinToString("\n") + 0.toChar()).toByteArray())
}