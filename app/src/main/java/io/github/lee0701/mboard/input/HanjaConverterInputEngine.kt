package io.github.lee0701.mboard.input

import android.graphics.drawable.Drawable
import io.github.lee0701.mboard.dictionary.HanjaDictionary
import io.github.lee0701.mboard.dictionary.ListDictionary
import io.github.lee0701.mboard.service.KeyboardState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HanjaConverterInputEngine(
    getInputEngine: (InputEngine.Listener) -> InputEngine,
    private val dictionary: ListDictionary<HanjaDictionary.Entry>,
    override val listener: InputEngine.Listener,
): InputEngine, InputEngine.Listener {

    private val inputEngine: InputEngine = getInputEngine(this)
    private val composingWordStack: MutableList<String> = mutableListOf()
    private var composingChar: String = ""
    private val currentComposing: String get() = composingWordStack.lastOrNull().orEmpty() + composingChar

    override fun onKey(code: Int, state: KeyboardState) {
        inputEngine.onKey(code, state)
    }

    override fun onDelete() {
        inputEngine.onDelete()
    }

    override fun onComposingText(text: CharSequence) {
        composingChar = text.toString()
        updateView()
        CoroutineScope(Dispatchers.IO).launch { convert() }
    }

    override fun onFinishComposing() {
        listener.onFinishComposing()
        composingChar = ""
        composingWordStack.clear()
    }

    override fun onCommitText(text: CharSequence) {
        composingChar = text.toString()
        val newComposingText = currentComposing
        if(composingWordStack.lastOrNull() != newComposingText) composingWordStack += newComposingText
        updateView()
    }

    override fun onDeleteText(beforeLength: Int, afterLength: Int) {
        if(composingWordStack.isNotEmpty()) composingWordStack.removeLast()
        else listener.onDeleteText(beforeLength, afterLength)
        updateView()
    }

    override fun onCandidates(list: List<Candidate>) {
        listener.onCandidates(list)
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
        val result = dictionary.search(currentComposing) ?: return
        val candidates = result.map { entry -> DefaultCandidate(entry.result, entry.frequency.toFloat()) }
        listener.onCandidates(candidates)
    }

    private fun updateView() {
        listener.onComposingText(currentComposing)
    }

    override fun onReset() {
        inputEngine.onReset()
        updateView()
    }

    override fun getLabels(state: KeyboardState): Map<Int, CharSequence> {
        return inputEngine.getLabels(state)
    }

    override fun getIcons(state: KeyboardState): Map<Int, Drawable> {
        return emptyMap()
    }
}