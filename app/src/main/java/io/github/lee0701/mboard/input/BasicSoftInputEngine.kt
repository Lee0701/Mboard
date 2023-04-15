package io.github.lee0701.mboard.input

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.module.softkeyboard.Keyboard
import io.github.lee0701.mboard.service.KeyboardState
import io.github.lee0701.mboard.service.ModifierState
import io.github.lee0701.mboard.view.candidates.BasicCandidatesViewManager
import io.github.lee0701.mboard.view.keyboard.*

class BasicSoftInputEngine(
    private val keyboard: Keyboard,
    getInputEngine: (InputEngine.Listener) -> InputEngine,
    private val autoUnlockShift: Boolean = true,
    override val listener: InputEngine.Listener,
): SoftInputEngine {
    private val inputEngine: InputEngine = getInputEngine(listener)

    var symbolsInputEngine: InputEngine? = null
    var alternativeInputEngine: InputEngine? = null

    private var doubleTapGap: Int = 500
    private var keyboardViewType: String = "canvas"

    private var longPressAction: FlickLongPressAction = FlickLongPressAction.Shifted
    private var flickUpAction: FlickLongPressAction = FlickLongPressAction.Shifted
    private var flickDownAction: FlickLongPressAction = FlickLongPressAction.Symbols
    private var flickLeftAction: FlickLongPressAction = FlickLongPressAction.None
    private var flickRightAction: FlickLongPressAction = FlickLongPressAction.None

    private var keyboardView: KeyboardView? = null

    private var keyboardState: KeyboardState = KeyboardState()
    private var shiftClickedTime: Long = 0
    private var ignoreCode: Int = 0
    private var inputHappened: Boolean = false

    override fun initView(context: Context): View? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        doubleTapGap = preferences.getFloat("behaviour_double_tap_gap", 500f).toInt()
        keyboardViewType = preferences.getString("appearance_keyboard_view_type", "canvas") ?: keyboardViewType
        longPressAction = FlickLongPressAction.of(preferences.getString("behaviour_long_press_action", "shift") ?: "shift")
        flickUpAction = FlickLongPressAction.of(preferences.getString("behaviour_flick_action_up", "shift") ?: "shift")
        flickDownAction = FlickLongPressAction.of(preferences.getString("behaviour_flick_action_down", "symbol") ?: "symbol")
        flickLeftAction = FlickLongPressAction.of(preferences.getString("behaviour_flick_action_left", "none") ?: "none")
        flickRightAction = FlickLongPressAction.of(preferences.getString("behaviour_flick_action_", "none") ?: "none")

        val name = preferences.getString("appearance_theme", "theme_dynamic")
        val theme = Themes.ofName(name)
        keyboardView = when(keyboardViewType) {
            "stacked_view" -> StackedViewKeyboardView(context, null, keyboard, theme, this)
            else -> CanvasKeyboardView(context, null, keyboard, theme, this)
        }
        return keyboardView
    }

    override fun onKey(code: Int, state: KeyboardState) {
        onPrintingKey(code, state)
        updateView()
    }

    fun onKeyRepeat(code: Int, state: KeyboardState) {

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

    override fun onItemClicked(candidate: Candidate) {
        if(inputEngine is BasicCandidatesViewManager.Listener) inputEngine.onItemClicked(candidate)
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
        return keyboard.rows.flatMap { it.keys }.associate { it.code to label(it.label.orEmpty()) }
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
        if(ignoreCode == code) {
            ignoreCode = 0
            return
        }
        when(code) {
            KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> {
            }
            KeyEvent.KEYCODE_DEL -> {
                inputEngine.onDelete()
            }
            KeyEvent.KEYCODE_SPACE -> {
                val state = keyboardState.copy()
                onReset()
                keyboardState = state
                listener.onCommitText(" ")
                autoUnlockShift()
            }
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                onReset()
                listener.onEditorAction(code)
                autoUnlockShift()
            }
            else -> {
                onPrintingKey(code, keyboardState)
                autoUnlockShift()
            }
        }
        updateView()
    }

    override fun onKeyLongClick(code: Int, output: String?) {
        longPressAction.onKey(code, keyboardState, this)
        ignoreCode = code
        inputHappened = true
    }

    private fun onPrintingKey(code: Int, keyboardState: KeyboardState) {
        inputEngine.onKey(code, keyboardState)
        inputHappened = true
    }

    override fun onKeyFlick(direction: FlickDirection, code: Int, output: String?) {
        val action = when(direction) {
            FlickDirection.Up -> flickUpAction
            FlickDirection.Down -> flickDownAction
            FlickDirection.Left -> flickLeftAction
            FlickDirection.Right -> flickRightAction
            else -> FlickLongPressAction.None
        }
        action.onKey(code, keyboardState, this)
        ignoreCode = code
        inputHappened = true
    }

    private fun autoUnlockShift() {
        if(!autoUnlockShift) return
        if(keyboardState.shiftState.pressing && inputHappened) return
        val lastState = keyboardState
        val lastShiftState = lastState.shiftState
        if(!lastShiftState.locked && !lastShiftState.pressing) {
            keyboardState = lastState.copy(shiftState = ModifierState())
        }
    }

    override fun getHeight(): Int {
        val displayMetrics = keyboardView?.context?.resources?.displayMetrics
        return if(displayMetrics != null)
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, keyboard.height, displayMetrics).toInt()
        else 0
    }
}