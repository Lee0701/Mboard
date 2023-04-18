package io.github.lee0701.mboard.dictionary

import java.io.File
import java.text.Normalizer

fun main() {
    val corpusFile = File("dict_src/ko.txt")
    val tsvFile = File("dict/ko.tsv")
    val diskDictFile = File("dict/ko.bin")
    genDiskDictFromTsv(tsvFile, diskDictFile)
}

fun genDiskDictFromTsv(tsvFile: File, outFile: File) {
    val br = tsvFile.bufferedReader()
    val dict = MutableTrieDictionary()
    var i = 0
    while(true) {
        val line = br.readLine() ?: break
        if('\t' in line) {
            val (key, value) = line.split('\t')
            dict.put(Normalizer.normalize(key, Normalizer.Form.NFD).map { it.code }, mapOf(i to value.toInt()))
        }
        i += 1
    }
    DiskTrieDictionary.build(dict).write(outFile.outputStream())
}

fun genTsvFromCorpus(corpusFile: File, outFile: File) {
    val replacements = ",.?!"
    val br = corpusFile.bufferedReader()
    val dict = mutableMapOf<String, Int>()
    while(true) {
        val line = br.readLine() ?: break
        val tokens = line.split(' ')
            .map { tok -> replacements.fold(tok) { t, c -> t.replace(c.toString(), "") }.trim() }
            .filter { tok -> tok.all { it in '가'..'힣' } }
        tokens.forEach { dict[it] = dict.getOrPut(it) { 0 } + 1 }
    }
    val result = dict
        .map { (k, v) -> k to v }
        .sortedByDescending { it.second }
        .map { (k, v) -> "$k\t$v" }
        .joinToString("\n")
    outFile.writeBytes(result.toByteArray())
}