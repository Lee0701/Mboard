package io.github.lee0701.mboard.input

import android.graphics.drawable.Drawable
import io.github.lee0701.mboard.dictionary.HanjaDictionary
import io.github.lee0701.mboard.dictionary.ListDictionary
import io.github.lee0701.mboard.service.KeyboardState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        MainScope().launch { convert() }
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

    private suspend fun convert() = withContext(Dispatchers.IO) {
        val result = dictionary.search(currentComposing) ?: return@withContext
        val candidates = result.map { entry -> DefaultCandidate(entry.result, entry.frequency.toFloat()) }
        launch(Dispatchers.Main) { listener.onCandidates(candidates) }
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