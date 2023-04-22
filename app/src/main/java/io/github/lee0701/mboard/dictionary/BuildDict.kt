package io.github.lee0701.mboard.dictionary

import java.io.DataOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.text.Normalizer

fun main() {
    val testSearch = true
    val genPrefixDict = false
    val genNgramDict = true

    if(genPrefixDict) {
        val inFile = File("dict_src/corpus.txt")
        val outDictFile = File("dict_src/dict-prefix.bin")
        val outVocabFile = File("dict_src/vocab.tsv")
        val (dictionary, vocab) = buildPrefixDict(inFile, outDictFile, outVocabFile, 10)
    }
    if(genNgramDict) {
        val inFile = File("dict_src/corpus-100k.txt")
        val inVocabFile = File("dict_src/vocab.tsv")
        val outFile = File("dict_src/dict-ngram.bin")
        val (dictionary, vocab) = buildNgramDict(inFile, inVocabFile, outFile, 5, 4)
    }
    if(testSearch) {
        val prefixDict = DiskTrieDictionary(ByteBuffer.wrap(File("dict_src/dict-prefix.bin").readBytes()))
        val ngramDict = DiskTrieDictionary(ByteBuffer.wrap(File("dict_src/dict-ngram.bin").readBytes()))
        val vocab = File("dict_src/vocab.tsv").bufferedReader().readLines()
            .map { it.split('\t') }.filter { it.size == 2 }.mapIndexed { i, (k, v) -> k to i }.toMap()
        val revVocab = vocab.map { (k, v) -> v to k }.toMap()
        println(prefixDict.search(getKey("가능")).map { "${it.key} ${revVocab[it.key]} ${it.value}" })
        val searchResult = ngramDict.search("우리 나라 _".split(' ').map { vocab[it] ?: -1 })
        println(searchResult.map { (k, v) -> revVocab[k] to v })
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
    minFreq: Int = 1,
): Pair<AbstractTrieDictionary, Map<String, Int>> {
    val vocabulary = inVocabFile.bufferedReader().readLines()
        .map { it.split('\t') }.filter { it.size == 2 }.mapIndexed { i, (k, v) -> k to i }.toMap()
    val br = inFile.bufferedReader()
    var i = 0
    var map = (2..grams).associateWith { mutableMapOf<List<Int>, Map<Int, Int>>() }
    while(true) {
        val line = br.readLine() ?: break
        val tokens = line.split(' ').map { vocabulary[it] ?: -1 }
        tokens.indices.forEach { j ->
            (2 .. grams).map { n ->
                val sliced = tokens.drop(j).take(n)
                if(sliced.size >= 2) {
                    val key = sliced.dropLast(1)
                    val value = sliced.last()
                    val existing = map[n]?.get(key) ?: mapOf(value to 0)
                    val newValue = existing + mapOf(value to (existing[value] ?: 0) + 1)
                    map[n]?.put(key, newValue)
                }
            }
        }
        i += 1
        if(i % 1000 == 0) println(i)
        if(i % 10000 == 0) {
            map.forEach { (n, gramMap) ->
                println("$n: before ${gramMap.size}")
                val mapped = gramMap.map { (key, value) ->
                    val values = value.mapValues { (_, v) -> (v.toFloat() / n * 2).toInt() }.filter { (_, v) -> v > 0 }.toMap()
                    if(values.isEmpty()) null else key to values
                }.filterNotNull().toMap()
                println("   after ${mapped.size}")
                mapped.toMutableMap()
            }
            File("dict_src/map.txt").writeBytes(map.toString().toByteArray())
        }
    }
    val ngramDict = MutableTrieDictionary()
    map.entries.forEach { (n, gramMap) ->
        println("# $n gram")
        gramMap.entries.forEachIndexed { index, (key, value) ->
            val filtered = value.filter { (_, freq) -> freq >= minFreq }
            ngramDict.put(key, filtered)
            if(index % 1000 == 0) println(index)
        }
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

fun getKey(string: String): List<Int> {
    return Normalizer.normalize(string, Normalizer.Form.NFD).map { it.code }
}