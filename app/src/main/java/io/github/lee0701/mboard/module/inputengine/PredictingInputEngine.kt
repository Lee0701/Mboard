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
import io.github.lee0701.mboard.preset.InputEnginePreset
import io.github.lee0701.mboard.preset.softkeyboard.Keyboard
import io.github.lee0701.mboard.service.KeyboardState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.Normalizer

class PredictingInputEngine(
    getInputEngine: (InputEngine.Listener) -> InputEngine,
    private val dictionary: AbstractTrieDictionary,
    private val vocab: Map<Int, String>,
    override val listener: InputEngine.Listener,
): InputEngine, InputEngine.Listener, CandidateListener {
    private var job: Job? = null

    override var components: List<InputViewComponent> = listOf()
    override var symbolsInputEngine: InputEngine? = null
    override var alternativeInputEngine: InputEngine? = null
    private val inputEngine: InputEngine = getInputEngine(this)
    private val composingWordStack: MutableList<String> = mutableListOf()
    private var composingChar: String = ""
    private var beforeText = ""

    private val currentComposing: String get() = composingWordStack.lastOrNull().orEmpty() + composingChar

    override fun onKey(code: Int, state: KeyboardState) {
        inputEngine.onKey(code, state)
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
        convert()
    }

    override fun onFinishComposing() {
        listener.onFinishComposing()
        composingChar = ""
        composingWordStack.clear()
        convert()
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
    }

    override fun onSystemKey(code: Int): Boolean {
        onReset()
        return listener.onSystemKey(code)
    }

    override fun onEditorAction(code: Int) {
        onReset()
        return listener.onEditorAction(code)
    }

    private fun convert() {
        val job = job
        if(job != null && job.isActive) return
        val key = Normalizer.normalize(currentComposing, Normalizer.Form.NFD)
            .map {
                return@map if(Hangul.isConsonant(it.code)) Hangul.consonantToCho(it.code)
                else if(Hangul.isVowel(it.code)) Hangul.vowelToJung(it.code)
                else it.code
            }
        this.job = CoroutineScope(Dispatchers.IO).launch {
            val candidates = dictionary.searchPrefix(key)
                .mapNotNull { (index, value) -> vocab[index]?.let { DefaultCandidate(it, value.toFloat()) } }
            delay(50)
            launch(Dispatchers.Main) {
                onCandidates(candidates)
            }
        }
    }

    private fun updateView() {
        listener.onComposingText(currentComposing)
    }

    override fun onReset() {
        inputEngine.onReset()
        listener.onCandidates(listOf())
        updateView()
        convert()
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