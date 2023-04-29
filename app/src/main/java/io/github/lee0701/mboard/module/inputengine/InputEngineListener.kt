package io.github.lee0701.mboard.module.inputengine

import io.github.lee0701.mboard.module.candidates.Candidate

interface InputEngineListener {
    fun onComposingText(text: CharSequence)
    fun onFinishComposing()
    fun onCommitText(text: CharSequence)
    fun onDeleteText(beforeLength: Int, afterLength: Int)
    fun onCandidates(list: List<Candidate>)
    fun onSystemKey(code: Int): Boolean
    fun onEditorAction(code: Int)
}