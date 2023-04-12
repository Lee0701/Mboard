package io.github.lee0701.mboard.input

import android.graphics.drawable.Drawable
import io.github.lee0701.mboard.dictionary.AbstractTrieDictionary
import io.github.lee0701.mboard.service.KeyboardState
import io.github.lee0701.mboard.view.candidates.BasicCandidatesViewManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.Normalizer

class PredictingInputEngine(
    getInputEngine: (InputEngine.Listener) -> InputEngine,
    private val prefixDict: AbstractTrieDictionary,
    private val ngramDict: AbstractTrieDictionary,
    private val vocabulary: List<Pair<String, Int>>,
    override val listener: InputEngine.Listener,
): InputEngine, InputEngine.Listener, BasicCandidatesViewManager.Listener {

    private val inputEngine: InputEngine = getInputEngine(this)
    private val composingWordStack: MutableList<String> = mutableListOf()
    private var composingChar: String = ""
    private val currentComposing: String get() = composingWordStack.lastOrNull().orEmpty() + composingChar
    private var textBeforeCursor: String = ""
    private var tokenized: List<Int> = listOf()

    override fun onKey(code: Int, state: KeyboardState) {
        inputEngine.onKey(code, state)
    }

    override fun onDelete() {
        inputEngine.onDelete()
    }

    override fun onTextAroundCursor(before: String, after: String) {
        textBeforeCursor = before
        tokenized = tokenize(before)
        convert()
    }

    override fun onComposingText(text: CharSequence) {
        composingChar = text.toString()
        updateView()
        convert()
    }

    override fun onFinishComposing() {
        listener.onFinishComposing()
        composingChar = ""
        composingWordStack.clear()
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
            listener.onDeleteText(beforeLength, afterLength)
        }
        updateView()
    }

    override fun onCandidates(list: List<Candidate>) {
        listener.onCandidates(list)
    }

    override fun onItemClicked(candidate: Candidate) {
        if(candidate is DefaultHanjaCandidate) {
            listener.onCommitText(candidate.text)
            val currentComposing = currentComposing
            composingChar = ""
            onReset()
            val newComposingText = currentComposing.drop(candidate.text.length)
            composingWordStack.clear()
            newComposingText.indices.forEach { i ->
                composingWordStack += newComposingText.take(i + 1)
            }
            listener.onComposingText(newComposingText)
            updateView()
            convert()
        }
    }

    override fun onSystemKey(code: Int): Boolean {
        onReset()
        return listener.onSystemKey(code)
    }

    override fun onEditorAction(code: Int) {
        onReset()
        return listener.onEditorAction(code)
    }

    private fun convert() = CoroutineScope(Dispatchers.IO).launch {
        if(currentComposing.isEmpty()) {
            val candidates = search(tokenized).entries.filter { (v, _) -> v != -1 }
                .map { (v, freq) -> DefaultCandidate(vocabulary[v].first, vocabulary[v].second * freq.toFloat()) }
            launch(Dispatchers.Main) { onCandidates(candidates) }
        } else if(currentComposing.length > 1) {
            val key = getKey(currentComposing)
            val candidates = prefixDict.searchPrefix(key)
                .mapNotNull { (v, freq) -> vocabulary.getOrNull(v)?.let { DefaultCandidate(it.first, it.second * freq.toFloat()) } }
            launch(Dispatchers.Main) { onCandidates(candidates) }
        }
    }

    private fun search(key: List<Int>): Map<Int, Int> {
        val gramResults = (5 downTo 1).map { n ->
            val slicedKey = key.takeLast(n)
            n to ngramDict.search(slicedKey)
        }
        return gramResults.fold(mapOf()) { acc, (n, map) ->
            (acc.keys.toList() + map.keys.toList()).associateWith { key -> ((map[key] ?: 0) + (map[key] ?: 0)) / 2 * n }
        }
    }

    private fun tokenize(text: String): List<Int> {
        val tokens = text.split(' ').map { getKey(it) }
        val result = mutableListOf<Int>()
        tokens.forEach { token ->
            var i = 0
            while(i < token.size) {
                var found = false
                for(j in 5 downTo 1) {
                    val sliced = token.drop(i).take(j + 1)
                    val max = prefixDict.search(sliced).map { (index, _) -> index }
                        .maxByOrNull { vocabulary[it].first.length }
                    if(max != null) {
                        result += max
                        i += vocabulary[max].first.length
                        found = true
                        break
                    }
                }
                if(!found) {
                    i += 1
                }
            }
        }
        return result.toList()
    }

    fun getKey(string: String): List<Int> {
        return Normalizer.normalize(string, Normalizer.Form.NFD).map { it.code }
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
}