package io.github.lee0701.mboard.input

import android.content.Context
import android.graphics.drawable.Drawable
import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.keyboard.KeyboardView
import io.github.lee0701.mboard.service.KeyboardState
import io.github.lee0701.mboard.service.ModifierState
import io.github.lee0701.mboard.view.keyboard.Keyboard
import io.github.lee0701.mboard.view.keyboard.Themes

class BasicSoftInputEngine(
    private val softKeyboard: Keyboard,
    getInputEngine: (InputEngine.Listener) -> InputEngine,
    private val autoUnlockShift: Boolean = true,
    override val listener: InputEngine.Listener,
): SoftInputEngine {

    private val inputEngine: InputEngine = getInputEngine(listener)

    private var doubleTapGap: Int = 500

    private var keyboardView: KeyboardView? = null

    private var keyboardState: KeyboardState = KeyboardState()
    private var shiftClickedTime: Long = 0
    private var inputHappened: Boolean = false

    override fun onKey(code: Int, state: KeyboardState) {
        inputEngine.onKey(code, state)
        updateView()
    }

    override fun onDelete() {
        inputEngine.onDelete()
    }

    override fun onReset() {
        inputEngine.onReset()
        keyboardState = KeyboardState()
        updateView()
    }

    override fun getLabels(state: KeyboardState): Map<Int, CharSequence> {
        return inputEngine.getLabels(state)
    }

    override fun getIcons(state: KeyboardState): Map<Int, Drawable> {
        val context = keyboardView?.context
        val shiftIconID = if(keyboardState.shiftState.locked) R.drawable.keyic_shift_lock else R.drawable.keyic_shift
        val shiftIcon = context?.let { ContextCompat.getDrawable(it, shiftIconID) }
        return inputEngine.getIcons(state) + shiftIcon?.let { mapOf(KeyEvent.KEYCODE_SHIFT_LEFT to it, KeyEvent.KEYCODE_SHIFT_RIGHT to it) }.orEmpty()
    }

    override fun initView(context: Context): View? {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        doubleTapGap = sharedPreferences.getInt("behaviour_double_tap_gap", 500)
        val name = sharedPreferences.getString("appearance_theme", "theme_dynamic")
        val theme = Themes.map[name] ?: Themes.Static
        keyboardView = KeyboardView(context, null, softKeyboard, theme, this)
        return keyboardView
    }

    override fun updateView() {
        updateLabelsAndIcons(getShiftedLabels() + getLabels(keyboardState), getIcons(keyboardState))
    keyboardView?.apply {
            invalidate()
        }
    }

    private fun getShiftedLabels(): Map<Int, CharSequence> {
        fun label(label: String) =
            if(keyboardState.shiftState.pressed || keyboardState.shiftState.locked) label.uppercase()
            else label.lowercase()
        return softKeyboard.rows.flatMap { it.keys }.associate { it.code to label(it.label.orEmpty()) }
    }

    private fun updateLabelsAndIcons(labels: Map<Int, CharSequence>, icons: Map<Int, Drawable>) {
        val keyboardView = keyboardView ?: return
        keyboardView.updateLabelsAndIcons(labels, icons)
    }

    override fun onKeyDown(code: Int, output: String?) {
        when(code) {
            KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> onShiftKeyDown()
        }
    }

    override fun onKeyUp(code: Int, output: String?) {
        when(code) {
            KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> onShiftKeyUp()
        }
    }

    private fun onShiftKeyDown() {
        val lastState = keyboardState
        val lastShiftState = lastState.shiftState
        val currentShiftState = lastShiftState.copy()
        val newShiftState = currentShiftState.copy()

        keyboardState = lastState.copy(shiftState = newShiftState.copy(pressing = true))
        inputHappened = false
        updateView()
    }

    private fun onShiftKeyUp() {
        val lastState = keyboardState
        val lastShiftState = lastState.shiftState
        val currentShiftState = lastShiftState.copy(pressing = false)

        val currentTime = System.currentTimeMillis()
        val timeDiff = currentTime - shiftClickedTime

        val newShiftState = if(currentShiftState.locked) {
            ModifierState()
        } else if(currentShiftState.pressed) {
            if(timeDiff < doubleTapGap) {
                ModifierState(pressed = true, locked = true)
            } else {
                ModifierState()
            }
        } else if(inputHappened) {
            ModifierState()
        } else {
            ModifierState(pressed = true)
        }

        keyboardState = lastState.copy(shiftState = newShiftState.copy(pressing = false))
        shiftClickedTime = currentTime
        inputHappened = false
        updateView()
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
        inputHappened = true
    }

    private fun autoUnlockShift() {
        if(!autoUnlockShift) return
        val lastState = keyboardState
        val lastShiftState = lastState.shiftState
        if(!lastShiftState.locked && !lastShiftState.pressing) {
            keyboardState = lastState.copy(shiftState = ModifierState())
        }
    }

    override fun onComputeInsets(inputView: View, outInsets: InputMethodService.Insets?) {
        if(outInsets != null) {
            outInsets.touchableInsets = InputMethodService.Insets.TOUCHABLE_INSETS_VISIBLE
            val visibleTopY = inputView.height - (keyboardView?.height ?: return)
            outInsets.visibleTopInsets = visibleTopY
            outInsets.contentTopInsets = visibleTopY
        }
    }

}