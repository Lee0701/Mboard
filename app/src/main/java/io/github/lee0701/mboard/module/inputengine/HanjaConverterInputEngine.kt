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
import io.github.lee0701.mboard.service.KeyboardState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HanjaConverterInputEngine(
    getInputEngine: (InputEngine.Listener) -> InputEngine,
    private val hanjaConverter: HanjaConverter?,
    private val predictor: Predictor?,
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
            onResetComponents()
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
        if(job != null && job.isActive) return
        this.job = CoroutineScope(Dispatchers.IO).launch {
            val text = beforeText + currentComposing
            val from = beforeText.length
            val to = text.length
            val composingText = ComposingText(text = text, from = from, to = to)
            val candidates = if(currentComposing.isNotBlank()) {
                val extraCandidates =
                    listOf(composingText.composing.substring(0, 1), composingText.composing)
                        .map { DefaultHanjaCandidate(it.toString(), it.toString(), "") }
                extraCandidates + hanjaConverter?.convertPrefix(composingText)
                    ?.flatten().orEmpty()
                    .map { DefaultHanjaCandidate(it.hanja, it.hangul, it.extra) }
            } else {
                predictor?.predict(composingText)
                    ?.top(10).orEmpty()
                    .map { DefaultHanjaCandidate(it.hanja, it.hangul, it.extra) }
            }
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
    }

    override fun getLabels(state: KeyboardState): Map<Int, CharSequence> {
        return inputEngine.getLabels(state)
    }

    override fun getIcons(state: KeyboardState): Map<Int, Drawable> {
        return emptyMap()
    }

    override fun getMoreKeys(state: KeyboardState): Map<Int, Keyboard> {
        return mapOf()
    }
}