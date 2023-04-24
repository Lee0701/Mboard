package io.github.lee0701.mboard.module.inputengine

import android.graphics.drawable.Drawable
import io.github.lee0701.converter.library.engine.ComposingText
import io.github.lee0701.converter.library.engine.HanjaConverter
import io.github.lee0701.converter.library.engine.Predictor
import io.github.lee0701.mboard.module.candidates.Candidate
import io.github.lee0701.mboard.module.candidates.CandidateListener
import io.github.lee0701.mboard.module.candidates.DefaultHanjaCandidate
import io.github.lee0701.mboard.module.component.InputViewComponent
import io.github.lee0701.mboard.preset.softkeyboard.Keyboard
import io.github.lee0701.mboard.charset.Hangul
import io.github.lee0701.mboard.dictionary.AbstractTrieDictionary
import io.github.lee0701.mboard.module.softkeyboard.Keyboard
import io.github.lee0701.mboard.service.KeyboardState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.util.PriorityQueue

class HanjaConverterInputEngine(
    getInputEngine: (InputEngine.Listener) -> InputEngine,
    private val vocab: List<Pair<String, Int>>,
    private val prefixDict: AbstractTrieDictionary,
    private val ngramDict: AbstractTrieDictionary,
    override val listener: InputEngine.Listener,
): InputEngine, InputEngine.Listener, CandidateListener {

    private var job: Job? = null
    private val inputEngine = getInputEngine(this)

    override var components: List<InputViewComponent> = inputEngine.components
    override var alternativeInputEngine: InputEngine? = inputEngine.alternativeInputEngine
    override var symbolsInputEngine: InputEngine? = inputEngine.symbolsInputEngine

    private val composingWordStack: MutableList<String> = mutableListOf()
    private var composingChar: String = ""
    private var beforeText = ""

    private val currentComposing: String get() = composingWordStack.lastOrNull().orEmpty() + composingChar

    override fun onKey(code: Int, state: KeyboardState) {
        inputEngine.onKey(code, state)
        convert()
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
        val reconvert = composingWordStack.isNotEmpty() || composingChar.isNotEmpty()
        listener.onFinishComposing()
        composingChar = ""
        composingWordStack.clear()
        if(reconvert) convert()
    }

    override fun onCommitText(text: CharSequence) {
        if(text.isEmpty()) return
        composingWordStack += composingWordStack.lastOrNull().orEmpty() + text.toString()
        updateView()
        convert()
    }

    override fun onDeleteText(beforeLength: Int, afterLength: Int) {
        if(composingWordStack.isNotEmpty()) {
            composingWordStack.removeLast()
        } else {
            onReset()
            onResetComponents()
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
        convert()
    }

    override fun onSystemKey(code: Int): Boolean {
        onReset()
        onResetComponents()
        return listener.onSystemKey(code)
    }

    override fun onEditorAction(code: Int) {
        onReset()
        onResetComponents()
        listener.onEditorAction(code)
    }

    private fun convert() {
        val job = job
        if(job != null && job.isActive) job.cancel()
        if(currentComposing.isNotEmpty()) {
            this.job = CoroutineScope(Dispatchers.IO).launch {
                val key = getKey(currentComposing)
                val candidates = prefixDict.searchPrefix(key).mapNotNull { (index, value) -> vocab.getOrNull(index)
                    ?.let { (text, freq) -> DefaultCandidate(text, freq.toFloat()) } }
                    .sortedByDescending { it.score }.take(10)
                launch(Dispatchers.Main) {
                    onCandidates(candidates)
                }
            }
        } else {
            this.job = CoroutineScope(Dispatchers.IO).launch {
                val tokens = tokenize(beforeText.takeLast(10))
                val candidates = search(tokens)
                    .mapNotNull { (k, v) -> vocab.getOrNull(k)?.first?.let { it to v } }
                    .map { (v, prob) -> DefaultCandidate(v, prob.toFloat()) }
                    .sortedByDescending { it.score }.take(10)
                launch(Dispatchers.Main) {
                    onCandidates(candidates)
                }
            }
        }
    }

    private fun search(context: List<Int>): Map<Int, Int> {
        val results = (5 downTo 1).map { n -> ngramDict.search(context.takeLast(n)) }
        val found = results.filter { it.isNotEmpty() }.fold(mapOf<Int, Int>()) { acc, map ->
            (acc.keys + map.keys).map { key -> key to ((acc[key] ?: 1) * (map[key]?: 1)) }.toMap() }
        return found.toMap()
    }

    private fun tokenize(text: String): List<Int> {
        val key = getKey(text)
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
                compareBy<IncompleteWord> { it.len }.compare(this, other)
        }

        val pq = PriorityQueue<IncompleteWord>()
        resultByIndex.firstOrNull()
            ?.map { (id, len) -> IncompleteWord(listOf(id), len, vocab.getOrNull(id)?.second ?: 0) }
            ?.forEach { pq.offer(it) }
        while(pq.isNotEmpty()) {
            val c = pq.poll() ?: continue
//            val len = if(c.indices.lastOrNull() == 0x20) c.len-1 else c.len
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
        return emptyMap()
    }

    override fun getMoreKeys(state: KeyboardState): Map<Int, Keyboard> {
        return inputEngine.getMoreKeys(state)
    }
}