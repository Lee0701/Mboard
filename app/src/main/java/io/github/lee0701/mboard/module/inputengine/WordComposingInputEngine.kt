package io.github.lee0701.mboard.module.inputengine

import android.text.util.Rfc822Tokenizer.tokenize
import io.github.lee0701.mboard.dictionary.AbstractTrieDictionary
import io.github.lee0701.mboard.dictionary.getKey
import io.github.lee0701.mboard.module.candidates.Candidate
import io.github.lee0701.mboard.module.candidates.DefaultCandidate
import io.github.lee0701.mboard.module.kokr.Hangul
import io.github.lee0701.mboard.service.KeyboardState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.util.PriorityQueue
import kotlin.math.sqrt

class WordComposingInputEngine(
    inputEngine: InputEngine,
    private val vocab: List<Pair<String, Int>> = listOf(),
    private val prefixDict: AbstractTrieDictionary? = null,
    private val ngramDict: AbstractTrieDictionary? = null,
): InputEngineWrapper(inputEngine), InputEngineListener {

    private var job: Job? = null

    var beforeText = ""
    val composingWordStack: MutableList<String> = mutableListOf()
    var composingChar: String = ""
    val currentComposing: String get() = composingWordStack.lastOrNull().orEmpty() + composingChar

    override fun onKey(code: Int, state: KeyboardState) {
        super.onKey(code, state)
    }

    override fun onSystemKey(code: Int): Boolean = false

    override fun onEditorAction(code: Int) = Unit

    override fun onComposingText(text: CharSequence) {
        composingChar = text.toString()
        updateView()
    }

    override fun onFinishComposing() {
        val reconvert = currentComposing.isNotEmpty()
        listener?.onFinishComposing()
        composingChar = ""
        composingWordStack.clear()
        if(reconvert) predict()
    }

    override fun onCommitText(text: CharSequence) {
        if(text.isEmpty()) return
        composingWordStack += composingWordStack.lastOrNull().orEmpty() + text.toString()
        updateView()
        predict()
    }

    override fun onDeleteText(beforeLength: Int, afterLength: Int) {
        if(composingWordStack.isNotEmpty()) {
            composingWordStack.removeLast()
        } else {
            onReset()
            onResetComponents()
            listener?.onDeleteText(beforeLength, afterLength)
        }
        updateView()
    }

    override fun onCandidates(list: List<Candidate>) {
        listener?.onCandidates(list)
    }

    fun onCandidateItemClicked(candidate: Candidate) {
        listener?.onCommitText(candidate.text)
        composingWordStack += currentComposing
        composingChar = ""
        if(inputEngine is HangulInputEngine) inputEngine.clearStack()

        val newComposingText = currentComposing.drop(candidate.text.length)
        composingWordStack.clear()
        newComposingText.indices.forEach { i ->
            composingWordStack += newComposingText.take(i + 1)
        }
        listener?.onComposingText(newComposingText)
        updateView()
        predict()
    }

    fun predict() {
        if(currentComposing.isNotEmpty()) {
            predictCurrentWord()
        } else {
            predictNextWord()
        }
    }

    private fun predictCurrentWord() {
        val job = job
        if(job != null && job.isActive) job.cancel()
        this.job = CoroutineScope(Dispatchers.IO).launch {
            val key = getKey(currentComposing)
            val prefixDict = prefixDict ?: return@launch
            val ngramDict = ngramDict ?: return@launch
            for(j in key.indices.reversed()) {
                val tokenized = tokenize(key.drop(j))
                val string = tokenized.map { vocab.getOrNull(it)?.first }.dropLast(1).joinToString("")
                val last = tokenized.lastOrNull()
                    ?.let { vocab.getOrNull(it)?.first }
                    ?.let { getKey(it) } ?: listOf()
                if(last.isNotEmpty()) {
                    val ngramResult = ngramDict.search(tokenized.dropLast(1))
                    val prefixResult = prefixDict.searchPrefix(last)
                    val candidates = prefixResult.mapNotNull { (index, _) ->
                        val vocabResult = vocab.getOrNull(index)
                        vocabResult?.let { (text, freq) ->
                            val contextFreq = ngramResult[index] ?: 0
                            DefaultCandidate(string + text, sqrt(freq.toFloat() * contextFreq)) }
                    }.sortedByDescending { it.score }.take(10)
                    launch(Dispatchers.Main) {
                        onCandidates(candidates)
                    }
                }
            }
        }
    }

    private fun predictNextWord() {
        val job = job
        if(job != null && job.isActive) job.cancel()
        this.job = CoroutineScope(Dispatchers.IO).launch {
            val tokens = tokenize(getKey(beforeText.takeLast(10)))
            val candidates = search(tokens)
                .mapNotNull { (k, v) -> vocab.getOrNull(k)?.first?.let { it to v } }
                .map { (text, prob) -> DefaultCandidate(text.let { if(it == "_") " " else it }, prob.toFloat()) }
                .sortedByDescending { it.score }.take(10)
            launch(Dispatchers.Main) {
                onCandidates(candidates)
            }
        }
    }

    private fun search(context: List<Int>): Map<Int, Int> {
        val results = (5 downTo 1).mapNotNull { n -> ngramDict?.search(context.takeLast(n)) }
        val found = results.filter { it.isNotEmpty() }.fold(mapOf<Int, Int>()) { acc, map ->
            (acc.keys + map.keys).associateWith { key -> ((acc[key] ?: 1) * (map[key] ?: 1)) }
        }
        return found.toMap()
    }

    private fun tokenize(key: List<Int>): List<Int> {
        val resultByIndex = key.indices.map { mutableListOf<Pair<Int, Int>>() }
        for(i in key.indices) {
            for(j in key.size downTo i) {
                val k = key.subList(i, j)
                if(k.isEmpty()) continue
                val value = prefixDict?.search(k) ?: continue
                if(value.isNotEmpty()) {
                    resultByIndex[i] += value.entries.first().key to k.size
                }
            }
        }

        data class IncompleteWord(
            val indices: List<Int>,
            val len: Int,
            val score: Int,
        ): Comparable<IncompleteWord> {
            override fun compareTo(other: IncompleteWord): Int =
                compareByDescending<IncompleteWord> { it.len }.compare(this, other)
        }

        val pq = PriorityQueue<IncompleteWord>()
        resultByIndex.firstOrNull()
            ?.map { (id, len) -> IncompleteWord(listOf(id), len, vocab.getOrNull(id)?.second ?: 0) }
            ?.forEach { pq.offer(it) }

        while(pq.isNotEmpty()) {
            val c = pq.poll() ?: continue
            if(c.len == key.size) {
                return c.indices
            }
            resultByIndex[c.len].forEach { (id, len) ->
                pq.offer(IncompleteWord(c.indices + id, c.len + len, c.score + (vocab.getOrNull(id)?.second ?: 0)))
            }
        }
        return listOf()
    }

    private fun getKey(text: String): List<Int> {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
            .map {
                return@map if(Hangul.isConsonant(it.code)) Hangul.consonantToCho(it.code)
                else if(Hangul.isVowel(it.code)) Hangul.vowelToJung(it.code)
                else if(it == ' ') '_'.code
                else it.code
            }
    }

    private fun updateView() {
        listener?.onComposingText(currentComposing)
    }

}