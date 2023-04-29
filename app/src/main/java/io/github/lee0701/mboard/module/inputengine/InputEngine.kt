package io.github.lee0701.mboard.module.inputengine

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup.LayoutParams
import androidx.appcompat.widget.LinearLayoutCompat
import io.github.lee0701.mboard.module.candidates.Candidate
import io.github.lee0701.mboard.module.component.InputViewComponent
import io.github.lee0701.mboard.preset.softkeyboard.Keyboard
import io.github.lee0701.mboard.service.KeyboardState

interface InputEngine {

    var listener: Listener?
    var components: List<InputViewComponent>
    var symbolsInputEngine: InputEngine?
    var alternativeInputEngine: InputEngine?

    fun initView(context: Context): View? {
        val componentViews = components.map { it.initView(context) }
        return LinearLayoutCompat(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            orientation = LinearLayoutCompat.VERTICAL
            componentViews.forEach { addView(it) }
        }
    }
    fun onReset()
    fun onResetComponents() {
        components.forEach { it.reset() }
    }

    fun onKey(code: Int, state: KeyboardState)
    fun onDelete()
    fun onTextAroundCursor(before: String, after: String)


    fun getLabels(state: KeyboardState): Map<Int, CharSequence>
    fun getIcons(state: KeyboardState): Map<Int, Drawable>
    fun getMoreKeys(state: KeyboardState): Map<Int, Keyboard>

    interface Listener {
        fun onComposingText(text: CharSequence)
        fun onFinishComposing()
        fun onCommitText(text: CharSequence)
        fun onDeleteText(beforeLength: Int, afterLength: Int)
        fun onCandidates(list: List<Candidate>)
        fun onSystemKey(code: Int): Boolean
        fun onEditorAction(code: Int)
    }
}