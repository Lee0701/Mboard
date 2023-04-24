package io.github.lee0701.mboard.input

import android.graphics.drawable.Drawable
import io.github.lee0701.mboard.dictionary.AbstractTrieDictionary
import io.github.lee0701.mboard.module.candidates.Candidate
import io.github.lee0701.mboard.module.candidates.CandidateListener
import io.github.lee0701.mboard.module.candidates.DefaultCandidate
import io.github.lee0701.mboard.module.candidates.DefaultHanjaCandidate
import io.github.lee0701.mboard.module.component.InputViewComponent
import io.github.lee0701.mboard.module.inputengine.HangulInputEngine
import io.github.lee0701.mboard.module.inputengine.InputEngine
import io.github.lee0701.mboard.module.kokr.Hangul
import io.github.lee0701.mboard.preset.softkeyboard.Keyboard
import io.github.lee0701.mboard.service.KeyboardState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.util.PriorityQueue
import kotlin.math.sqrt

class PredictingInputEngine(
    getInputEngine: (InputEngine.Listener) -> InputEngine,
    private val vocab: List<Pair<String, Int>>,
    private val prefixDict: AbstractTrieDictionary,
    private val ngramDict: AbstractTrieDictionary,
    override val listener: InputEngine.Listener,
): InputEngine, InputEngine.Listener, CandidateListener {
    private var job: Job? = null

    private val inputEngine: InputEngine = getInputEngine(this)
    override var components: List<InputViewComponent> = inputEngine.components
    override var symbolsInputEngine: InputEngine? = null
    override var alternativeInputEngine: InputEngine? = null

    private val composingWordStack: MutableList<String> = mutableListOf()
    private var composingChar: String = ""
    private var beforeText = ""

    private val currentComposing: String get() = composingWordStack.lastOrNull().orEmpty() + composingChar

    override fun onKey(code: Int, state: KeyboardState) {
        inputEngine.onKey(code, state)
        predict()
    }

    override fun onDelete() {
        inputEngine.onDelete()
    }

    override fun onTextAroundCursor(before: String, after: String) {
        this.beforeText = before
    }

    override fun onComposingText(text: CharSequence) {
        composingChar = text.toString()
        updateView()
    }

    override fun onFinishComposing() {
        val reconvert = currentComposing.isNotEmpty()
        listener.onFinishComposing()
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
            listener.onDeleteText(beforeLength, afterLength)
        }
        updateView()
    }

    override fun onCandidates(list: List<Candidate>) {
        listener.onCandidates(list)
    }

    override fun onItemClicked(candidate: Candidate) {
        listener.onCommitText(candidate.text)
        composingWordStack += currentComposing
        composingChar = ""
        if(inputEngine is HangulInputEngine) inputEngine.clearStack()

        val newComposingText = currentComposing.drop(candidate.text.length)
        composingWordStack.clear()
        newComposingText.indices.forEach { i ->
            composingWordStack += newComposingText.take(i + 1)
        }
        listener.onComposingText(newComposingText)
        updateView()
        predict()
    }

    override fun onSystemKey(code: Int): Boolean {
        onReset()
        return listener.onSystemKey(code)
    }

    override fun onEditorAction(code: Int) {
        onReset()
        return listener.onEditorAction(code)
    }

    private fun predict() {
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
            key.indices.reversed().map { j ->
                val tokenized = tokenize(key.drop(j))
                async {
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
                                val contextFreq = ngramResult[index]?.toFloat() ?: 1f
                                DefaultCandidate(string + text, sqrt(freq.toFloat() * contextFreq)) }
                        }.sortedByDescending { it.score }.take(10)
                        return@async candidates
                    } else {
                        return@async listOf()
                    }
                }
            }.awaitAll().also { candidates ->
                launch(Dispatchers.Main) {
                    onCandidates(candidates.last())
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
        val results = (5 downTo 1).map { n -> ngramDict.search(context.takeLast(n)) }
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
                val value = prefixDict.search(k)
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
        listener.onComposingText(currentComposing)
    }

    override fun onReset() {
        inputEngine.onReset()
        listener.onCandidates(listOf())
        updateView()
    }

    override fun getLabels(state: KeyboardState): Map<Int, CharSequence> {
        return inputEngine.getLabels(state)
    }

    override fun getIcons(state: KeyboardState): Map<Int, Drawable> {
        return inputEngine.getIcons(state)
    }

    override fun getMoreKeys(state: KeyboardState): Map<Int, Keyboard> {
        return inputEngine.getMoreKeys(state)
    }
}