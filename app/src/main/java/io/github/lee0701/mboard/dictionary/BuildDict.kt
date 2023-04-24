package io.github.lee0701.mboard.dictionary

import java.io.DataOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.text.Normalizer
import kotlin.math.log10
import kotlin.math.roundToInt

fun main() {
//    val testSearch = false
    val genPrefixDict = false
    val genNgramDict = false

    val testSearch = true
//    val genPrefixDict = true
//    val genNgramDict = true

    if(genPrefixDict) {
        val inFile = File("dict_src/corpus.txt")
        val outDictFile = File("dict_src/dict-prefix.bin")
        val outVocabFile = File("dict_src/vocab.tsv")
        val (dictionary, vocab) = buildPrefixDict(inFile, outDictFile, outVocabFile, 10)
    }
    if(genNgramDict) {
        val inFile = File("dict_src/corpus-30k.txt")
        val inVocabFile = File("dict_src/vocab.tsv")
        val outFile = File("dict_src/dict-ngram.bin")
        val (dictionary, vocab) = buildNgramDict(inFile, inVocabFile, outFile, 5, 10)
    }
    if(testSearch) {
        val prefixDict = DiskTrieDictionary(ByteBuffer.wrap(File("dict_src/dict-prefix.bin").readBytes()))
        val ngramDict = DiskTrieDictionary(ByteBuffer.wrap(File("dict_src/dict-ngram.bin").readBytes()))
        val vocab = File("dict_src/vocab.tsv").bufferedReader().readLines()
            .map { it.split('\t') }.filter { it.size == 2 }.mapIndexed { i, (k, v) -> k to i }.toMap()
        val revVocab = vocab.map { (k, v) -> v to k }.toMap()
        System.`in`.bufferedReader().forEachLine { line ->
            val key = line.split(' ').map { vocab[it] ?: -1 }
            val results = (5 downTo 1).map { n -> ngramDict.search(key.takeLast(n)).mapValues { (_, v) -> v * n } }
            val found = results.firstOrNull { it.isNotEmpty() } ?: return@forEachLine
            val sorted = found.map { (k, v) -> revVocab[k] to v }.sortedByDescending { (k, v) -> v }
            println(sorted.joinToString(", ") { (k, v) -> "$k=$v" })
        }
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
        }
        i += 1
        if(i % 1000 == 0) println("$i lines")
    }

    val max = log10(vocabulary.entries.maxOf { it.value }.toFloat())
    val sorted = vocabulary.entries
        .sortedByDescending { (k, v) -> v }
        .map { (k, v) -> k to v }
        .map { (k, v) -> k to (log10(v.toFloat()) / max * 255).toInt() }
        .filter { (k, v) -> v >= minFreq }

    sorted.forEachIndexed { index, (k, v) ->
        val key = getKey(k)
        val existing = dictionary.search(key)
        val newValue = existing + (index to (existing[index] ?: 0) + 1)
        dictionary.put(key, newValue)
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
    minFreq: Int = 10,
): Pair<AbstractTrieDictionary, Map<String, Int>> {
    val vocabulary = inVocabFile.bufferedReader().readLines()
        .map { it.split('\t') }.filter { it.size == 2 }.mapIndexed { i, (k, v) -> k to i }.toMap()
    val dicts = mutableListOf(mutableMapOf<List<Int>, Int>())

    val br = inFile.bufferedReader()
    var i = 0
    while(true) {
        val line = br.readLine() ?: break
        val tokens = line.split(' ').map { vocabulary[it] ?: -1 }
        val dict = dicts.last()
        tokens.indices.forEach { j ->
            (2 .. grams).map { n ->
                val sliced = tokens.drop(j).take(n)
                if(-1 in sliced) return@forEach
                if(sliced.size >= 2) {
                    val newValue = dict.getOrElse(sliced) { 0 } + 1
                    dict += sliced to newValue
                }
            }
        }
        i++
        if(i % 1000 == 0) println("$i lines")
        if(i % 10000 == 0) {
            dicts += mutableMapOf()
        }
    }

    val resultDict = MutableTrieDictionary()

    var d = 0
    for(dict in dicts) {
        val max = log10(dict.values.maxOrNull()?.toFloat() ?: continue)
        var i = 0
        println("# dict $d")
        dict.keys.forEach { list ->
            if(list.size < 2) return@forEach
            if(-1 in list) return@forEach
            val key = list.dropLast(1)
            val valueKey = list.last()
            val value = log10((dict[list] ?: 0f).toFloat() + 1f) / max * 255
            if(value < minFreq) return@forEach
            val existing = resultDict.search(key)
            val newMap = existing + mapOf(valueKey to value.roundToInt())
            resultDict.put(key, newMap)
            i += 1
//            val dictItems = resultDict.entries().size
            if(i % 10000 == 0) println("$i items")
        }
        d += 1
    }

    println(resultDict.entries().entries.take(10))

    println(resultDict.entries().size)
    val diskDictionary = DiskTrieDictionary.build(resultDict)
    diskDictionary.write(outFile.outputStream())
    println(diskDictionary.entries().size)

    return diskDictionary to vocabulary
}

fun buildHanjaDict(inFile: File, freqHanjaFile: File, freqHanjaeoFile: File, outFile: File) {
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

fun getKey(string: String): List<Int> {
    return Normalizer.normalize(string, Normalizer.Form.NFD).map { it.code }
}