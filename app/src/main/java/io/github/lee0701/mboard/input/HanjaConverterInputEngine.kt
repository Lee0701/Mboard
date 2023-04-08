package io.github.lee0701.mboard.input

import io.github.lee0701.mboard.dictionary.HanjaDictionary
import io.github.lee0701.mboard.dictionary.ListDictionary
import io.github.lee0701.mboard.service.KeyboardState

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
        convert()
        updateView()
    }

    override fun onFinishComposing() {
        listener.onFinishComposing()
        composingWordStack.clear()
        composingChar = ""
    }

    override fun onCommitText(text: CharSequence) {
        composingChar = text.toString()
        val newComposingText = currentComposing
        if(composingWordStack.lastOrNull() != newComposingText) composingWordStack += newComposingText
        updateView()
    }

    override fun onDeleteText(beforeLength: Int, afterLength: Int) {
        if(composingChar.isNotEmpty()) inputEngine.onDelete()
        else if(composingWordStack.isNotEmpty()) composingWordStack.removeLast()
        else listener.onDeleteText(beforeLength, afterLength)
        updateView()
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
        val result = dictionary.search(currentComposing)
        println(result)
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
}