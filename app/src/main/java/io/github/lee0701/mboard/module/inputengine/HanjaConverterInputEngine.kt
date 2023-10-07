package io.github.lee0701.mboard.module.inputengine

import android.graphics.drawable.Drawable
import io.github.lee0701.mboard.module.candidates.Candidate
import io.github.lee0701.mboard.module.candidates.CandidateListener
import io.github.lee0701.mboard.module.component.InputViewComponent
import io.github.lee0701.mboard.preset.softkeyboard.Keyboard
import io.github.lee0701.mboard.service.KeyboardState

class HanjaConverterInputEngine(
    getInputEngine: (InputEngine.Listener) -> InputEngine,
    private val hanjaConverter: HanjaConverter,
    override val listener: InputEngine.Listener,
): InputEngine, InputEngine.Listener, CandidateListener {

    private val inputEngine: InputEngine = getInputEngine(this)

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
        convertCurrentComposing()
    }

    override fun onFinishComposing() {
        listener.onFinishComposing()
        composingChar = ""
        composingWordStack.clear()
        convertCurrentComposing()
    }

    override fun onCommitText(text: CharSequence) {
        if(text.isEmpty()) return
        composingWordStack += composingWordStack.lastOrNull().orEmpty() + text.toString()
        updateView()
        convertCurrentComposing()
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
        convertCurrentComposing()
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

    fun convert(text: String) {
        hanjaConverter.convert(text)
    }

    private fun convertCurrentComposing() {
        this.convert(currentComposing)
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