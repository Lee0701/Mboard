package io.github.lee0701.mboard.input

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import io.github.lee0701.mboard.ime.KeyboardState
import io.github.lee0701.mboard.ime.ModifierState
import io.github.lee0701.mboard.keyboard.Keyboard

class BasicSoftInputEngine(
    initSoftKeyboard: () -> Keyboard,
    initInputEngine: (InputEngine.Listener) -> InputEngine,
    override val listener: InputEngine.Listener,
): SoftInputEngine {

    private val softKeyboard = initSoftKeyboard()
    private val inputEngine = initInputEngine(listener)

    private val doubleTapGap: Int = 500

    private var softKeyboardWrapper: Keyboard.ViewWrapper? = null

    private var keyboardState: KeyboardState = KeyboardState()
    private var shiftClickedTime: Long = 0
    private var inputWhileShiftPressed: Boolean = false

    override fun onKey(code: Int, state: KeyboardState) {
        inputEngine.onKey(code, state)
    }

    override fun onDelete() {
        inputEngine.onDelete()
    }

    override fun onReset() {
        inputEngine.onReset()
        updateView()
    }

    override fun getLabels(state: KeyboardState): Map<Int, CharSequence> {
        return inputEngine.getLabels(state)
    }

    override fun initView(context: Context): View {
        val softKeyboardWrapper = softKeyboard.initView(context, this)
        this.softKeyboardWrapper = softKeyboardWrapper
        return softKeyboardWrapper.binding.root
    }

    override fun getView(): View {
        return softKeyboardWrapper!!.binding.root
    }

    private fun updateView() {
        updateLabels(getShiftedLabels() + inputEngine.getLabels(keyboardState))
    }

    private fun getShiftedLabels(): Map<Int, CharSequence> {
        fun label(label: String) =
            if(keyboardState.shiftState.pressed || keyboardState.shiftState.locked) label.uppercase()
            else label.lowercase()
        return softKeyboardWrapper?.keys?.associate { it.key.code to label(it.key.label.orEmpty()) }.orEmpty()
    }

    private fun updateLabels(labels: Map<Int, CharSequence>) {
        val keys = softKeyboardWrapper?.keys ?: return
        keys.map { key ->
            val label = labels[key.key.code]
            if(label != null) key.binding.label.text = label
        }
    }

    override fun onKeyDown(code: Int, output: String?) {
        val lastState = keyboardState
        when(code) {
            KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> {
                keyboardState = lastState.copy(shiftState = lastState.shiftState.copy(pressed = true))
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
                updateView()
            }
        }
    }

    override fun onKeyClick(code: Int, output: String?) {
        if(listener.onSystemKey(code)) return
        when(code) {
            KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> {
            }
            KeyEvent.KEYCODE_DEL -> {
                inputEngine.onDelete()
            }
            KeyEvent.KEYCODE_SPACE -> {
                onReset()
                listener.onCommitText(" ")
                autoUnlockShift()
            }
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                onReset()
                listener.onEditorAction(code)
                autoUnlockShift()
            }
            else -> {
                onPrintingKey(code)
                autoUnlockShift()
            }
        }
        updateView()
    }

    private fun onPrintingKey(code: Int) {
        inputEngine.onKey(code, keyboardState)
    }

    private fun autoUnlockShift() {
        val lastState = keyboardState
        if(!lastState.shiftState.locked) keyboardState = lastState.copy(shiftState = ModifierState())
    }

    override fun onComputeInsets(inputView: View, outInsets: InputMethodService.Insets?) {
        if(outInsets != null) {
            outInsets.touchableInsets = InputMethodService.Insets.TOUCHABLE_INSETS_VISIBLE
            val visibleTopY = inputView.height - (softKeyboardWrapper?.binding?.root?.height ?: return)
            outInsets.visibleTopInsets = visibleTopY
            outInsets.contentTopInsets = visibleTopY
        }
    }
}