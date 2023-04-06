package io.github.lee0701.mboard.ime

import android.inputmethodservice.InputMethodService
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import io.github.lee0701.mboard.keyboard.KeyboardListener
import io.github.lee0701.mboard.layout.Layout
import io.github.lee0701.mboard.keyboard.Keyboard
import io.github.lee0701.mboard.layout.HangulLayout

class MboardIME: InputMethodService(), KeyboardListener, HangulInputSequence.Listener {

    private val doubleTapGap: Int = 500

    private var inputView: FrameLayout? = null
    private var keyboardView: Keyboard.ViewWrapper? = null

    private val keyCharacterMap: KeyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD)
    private val inputSequence = HangulInputSequence(HangulLayout.SEBEOL_390, HangulLayout.COMB_SEBEOL_390, this)
    private var keyboardState: KeyboardState = KeyboardState()

    override fun onCreate() {
        super.onCreate()
    }

    override fun onCreateInputView(): View {
        val inputView = FrameLayout(this, null)
        val keyboardView = Layout.LAYOUT_QWERTY_SEBEOLSIK_390_MOBILE.initView(this, this)
        inputView.addView(keyboardView.binding.root)
        this.inputView = inputView
        this.keyboardView = keyboardView
        updateView()
        return inputView
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
    }

    override fun onKeyPressed(code: Int, output: String?) {
        val lastState = keyboardState
        val currentTime = System.currentTimeMillis()
        when(code) {
            KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> {
                val timeDiff = currentTime - lastState.time
                val shiftState = lastState.shiftState
                val newShiftState =
                    if(shiftState.locked) shiftState.copy(pressed = false, locked = false)
                    else if(shiftState.pressed && timeDiff < doubleTapGap) shiftState.copy(pressed = true, locked = true)
                    else shiftState.copy(pressed = !shiftState.pressed, locked = false)
                val newState = lastState.copy(shiftState = newShiftState)
                keyboardState = newState
            }
            KeyEvent.KEYCODE_DEL -> {
                inputSequence.onDelete()
//                onDeleteText(1, 0)
            }
            KeyEvent.KEYCODE_SPACE -> {
                resetInput()
                onCommitText(" ")
                autoUnlockShift()
            }
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                resetInput()
                autoUnlockShift()
                if(!sendDefaultEditorAction(true)) return sendDownUpKeyEvents(code)
            }
            else -> {
                onPrintingKey(code)
                autoUnlockShift()
            }
        }
        updateView()
    }

    private fun onPrintingKey(code: Int) {
        inputSequence.onKey(code, keyboardState)
//        val charCode = keyCharacterMap.get(code, keyboardState.asMetaState())
//        if(charCode > 0) {
//            val ch = charCode.toChar().let { if(keyboardState.shiftState.pressed) it.uppercaseChar() else it }
//            onCommitText(ch.toString())
//        }
    }

    override fun onComposingText(text: CharSequence) {
        val inputConnection = currentInputConnection ?: return
        inputConnection.setComposingText(text, 1)
    }

    override fun onFinishComposing() {
        val inputConnection = currentInputConnection ?: return
        inputConnection.finishComposingText()
    }

    override fun onCommitText(text: CharSequence) {
        val inputConnection = currentInputConnection ?: return
        inputConnection.commitText(text, 1)
    }

    override fun onDeleteText(beforeLength: Int, afterLength: Int) {
        val inputConnection = currentInputConnection ?: return
        inputConnection.deleteSurroundingText(beforeLength, afterLength)
    }

    private fun resetInput() {
        inputSequence.reset()
    }

    private fun autoUnlockShift() {
        val lastState = keyboardState
        if(!lastState.shiftState.locked) keyboardState = lastState.copy(shiftState = ModifierState())
    }

    private fun updateView() {
        updateLabels(getShiftedLabels() + inputSequence.getLabels(keyboardState))
    }

    private fun getShiftedLabels(): Map<Int, CharSequence> {
        val range = KeyEvent.KEYCODE_A .. KeyEvent.KEYCODE_Z
        return range.associateWith { code ->
            keyCharacterMap.get(code, keyboardState.asMetaState()).toChar().toString()
        }
    }

    private fun updateLabels(labels: Map<Int, CharSequence>) {
        val keys = keyboardView?.keys ?: return
        keys.map { key ->
            val label = labels[key.key.code]
            if(label != null) key.binding.label.text = label
        }
    }

    override fun onComputeInsets(outInsets: Insets?) {
        super.onComputeInsets(outInsets)
        val inputView = this.inputView ?: return
        val keyboardView = this.keyboardView ?: return
        if(outInsets != null) {
            outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_VISIBLE
            val visibleTopY = inputView.height - keyboardView.binding.root.height
            outInsets.visibleTopInsets = visibleTopY
            outInsets.contentTopInsets = visibleTopY
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}