package io.github.lee0701.mboard.ime

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import io.github.lee0701.mboard.input.DirectInputEngine
import io.github.lee0701.mboard.input.HangulInputEngine
import io.github.lee0701.mboard.input.InputEngine
import io.github.lee0701.mboard.keyboard.KeyboardListener
import io.github.lee0701.mboard.layout.Layout
import io.github.lee0701.mboard.keyboard.Keyboard
import io.github.lee0701.mboard.layout.HangulLayout

class MboardIME: InputMethodService(), KeyboardListener, InputEngine.Listener {

    private val doubleTapGap: Int = 500

    private var inputView: FrameLayout? = null
    private var keyboardView: Keyboard.ViewWrapper? = null

    private val layout = Layout.LAYOUT_QWERTY_MOBILE
    private val engines: List<InputEngine> by lazy { listOf(
        DirectInputEngine(this),
        HangulInputEngine(HangulLayout.DUBEOL_STANDARD, HangulLayout.COMB_DUBEOL_STANDARD, this),
    ) }
    private val languages: List<Int> = listOf(0, 1)
    private var currentLanguage = 0
    private val currentInputEngine: InputEngine get() = engines[languages[currentLanguage]]
    private var keyboardState: KeyboardState = KeyboardState()
    private var shiftClickedTime: Long = 0
    private var shiftPressing: Boolean = false
    private var inputWhileShiftPressed: Boolean = false

    override fun onCreate() {
        super.onCreate()
    }

    override fun onCreateInputView(): View {
        val inputView = FrameLayout(this, null)
        val keyboardView = layout.initView(this, this)
        inputView.addView(keyboardView.binding.root)
        this.inputView = inputView
        this.keyboardView = keyboardView
        updateView()
        return inputView
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
    }

    override fun onKeyDown(code: Int, output: String?) {
        val lastState = keyboardState
        when(code) {
            KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> {
                keyboardState = lastState.copy(shiftState = lastState.shiftState.copy(pressed = true))
                shiftPressing = true
                inputWhileShiftPressed = false
                updateView()
            }
        }
    }

    override fun onKeyUp(code: Int, output: String?) {
        val lastState = keyboardState
        val shiftState = lastState.shiftState
        val currentTime = System.currentTimeMillis()
        val timeDiff = currentTime - shiftClickedTime
        println("$timeDiff $inputWhileShiftPressed")
        when(code) {
            KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> {
                if(shiftState.locked) {
                    keyboardState = lastState.copy(shiftState = ModifierState())
                } else if(shiftState.pressed) {
                    if(timeDiff < doubleTapGap) {
                        keyboardState = lastState.copy(shiftState = ModifierState(pressed = true, locked = true))
                    } else if(inputWhileShiftPressed) {
                        keyboardState = lastState.copy(shiftState = ModifierState(pressed = false))
                    } else {
                        keyboardState = lastState.copy(shiftState = ModifierState(pressed = true))
                    }
                } else {
                    keyboardState = lastState.copy(shiftState = ModifierState(pressed = true))
                }
                shiftClickedTime = currentTime
                inputWhileShiftPressed = false
                shiftPressing = false
                updateView()
            }
        }
    }

    override fun onKeyClick(code: Int, output: String?) {
        val lastState = keyboardState
        when(code) {
            KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> {
            }
            KeyEvent.KEYCODE_DEL -> {
                currentInputEngine.onDelete()
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
            KeyEvent.KEYCODE_LANGUAGE_SWITCH -> {
                currentLanguage += 1
                if(currentLanguage >= languages.size) currentLanguage = 0
            }
            else -> {
                onPrintingKey(code)
                autoUnlockShift()
            }
        }
        updateView()
    }

    private fun onPrintingKey(code: Int) {
        currentInputEngine.onKey(code, keyboardState)
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
        currentInputEngine.onReset()
    }

    private fun autoUnlockShift() {
        val lastState = keyboardState
        if(!lastState.shiftState.locked) keyboardState = lastState.copy(shiftState = ModifierState())
    }

    private fun updateView() {
        updateLabels(getShiftedLabels() + currentInputEngine.getLabels(keyboardState))
    }

    private fun getShiftedLabels(): Map<Int, CharSequence> {
        fun label(label: String) =
            if(keyboardState.shiftState.pressed || keyboardState.shiftState.locked) label.uppercase()
            else label.lowercase()
        return keyboardView?.keys?.associate { it.key.code to label(it.key.label.orEmpty()) }.orEmpty()
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