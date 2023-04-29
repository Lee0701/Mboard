package io.github.lee0701.mboard.module.inputengine

import android.graphics.drawable.Drawable
import io.github.lee0701.mboard.dictionary.AbstractTrieDictionary
import io.github.lee0701.mboard.module.candidates.Candidate
import io.github.lee0701.mboard.module.candidates.CandidateListener
import io.github.lee0701.mboard.module.candidates.DefaultCandidate
import io.github.lee0701.mboard.module.kokr.Hangul
import io.github.lee0701.mboard.preset.softkeyboard.Keyboard
import io.github.lee0701.mboard.service.KeyboardState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.util.PriorityQueue
import kotlin.math.sqrt

class HanjaConverterInputEngine(
    override val inputEngine: WordComposingInputEngine,
): InputEngineWrapper(inputEngine), CandidateListener {

    override fun onKey(code: Int, state: KeyboardState) {
        inputEngine.onKey(code, state)
        inputEngine.predict()
    }

    override fun onDelete() {
        inputEngine.onDelete()
    }

    override fun onTextAroundCursor(before: String, after: String) {
    }

    override fun onCandidateItemClicked(candidate: Candidate) {
    }

    fun onSystemKey(code: Int): Boolean {
        onReset()
        onResetComponents()
        return listener?.onSystemKey(code) == true
    }

    fun onEditorAction(code: Int) {
        onReset()
        onResetComponents()
        listener?.onEditorAction(code)
    }

    override fun onReset() {
        inputEngine.onReset()
        listener?.onCandidates(listOf())
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