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

class MboardIME: InputMethodService(), KeyboardListener {

    private val doubleTapGap: Int = 500

    private var inputView: FrameLayout? = null
    private var keyboardView: Keyboard.ViewWrapper? = null

    private val keyCharacterMap: KeyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD)
    private var keyboardState: KeyboardState = KeyboardState()
    private var shiftClickedTime: Long = 0
    private var shiftPressing: Boolean = false
    private var inputWhileShiftPressed: Boolean = false

    override fun onCreate() {
        super.onCreate()
    }

    override fun onCreateInputView(): View {
        val inputView = FrameLayout(this, null)
        val keyboardView = Layout.LAYOUT.initView(this, this)
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
                deleteText(1, 0)
            }
            KeyEvent.KEYCODE_SPACE -> {
                commitText(' ')
            }
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                if(!sendDefaultEditorAction(true)) return sendDownUpKeyEvents(code)
            }
            else -> {
                val charCode = keyCharacterMap.get(code, keyboardState.asMetaState())
                if(charCode > 0) {
                    val ch = charCode.toChar().let { if(lastState.shiftState.pressed) it.uppercaseChar() else it }
                    commitText(ch)
                    if(lastState.shiftState.pressed) inputWhileShiftPressed = true
                    if(!lastState.shiftState.locked && !shiftPressing) {
                        keyboardState = lastState.copy(shiftState = ModifierState())
                    }
                }
            }
        }
        updateView()
    }

    private fun commitText(char: Char) {
        val inputConnection = currentInputConnection ?: return
        inputConnection.commitText(char.toString(), 1)
    }

    private fun commitText(charSequence: CharSequence) {
        val inputConnection = currentInputConnection ?: return
        inputConnection.commitText(charSequence, 1)
    }

    private fun deleteText(beforeLength: Int, afterLength: Int) {
        val inputConnection = currentInputConnection ?: return
        inputConnection.deleteSurroundingText(beforeLength, afterLength)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun updateView() {
        updateShiftedLabels()
    }

    private fun updateShiftedLabels() {
        val range = KeyEvent.KEYCODE_A .. KeyEvent.KEYCODE_Z
        val labels = range
            .associateWith { code -> keyCharacterMap.get(code, keyboardState.asMetaState()).toChar().toString() }
        updateLabels(labels)
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

}