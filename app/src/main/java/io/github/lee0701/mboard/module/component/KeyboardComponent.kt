package io.github.lee0701.mboard.module.component

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.KeyEvent
import android.view.View
import androidx.preference.PreferenceManager
import io.github.lee0701.mboard.module.candidates.Candidate
import io.github.lee0701.mboard.module.candidates.CandidateListener
import io.github.lee0701.mboard.module.inputengine.InputEngine
import io.github.lee0701.mboard.module.keyboardview.CanvasKeyboardView
import io.github.lee0701.mboard.module.keyboardview.FlickDirection
import io.github.lee0701.mboard.module.keyboardview.FlickLongPressAction
import io.github.lee0701.mboard.module.keyboardview.KeyboardListener
import io.github.lee0701.mboard.module.keyboardview.KeyboardView
import io.github.lee0701.mboard.module.keyboardview.StackedViewKeyboardView
import io.github.lee0701.mboard.module.keyboardview.Themes
import io.github.lee0701.mboard.preset.softkeyboard.Key
import io.github.lee0701.mboard.preset.softkeyboard.Keyboard
import io.github.lee0701.mboard.service.KeyboardState
import io.github.lee0701.mboard.service.ModifierState

class KeyboardComponent(
    private val keyboard: Keyboard,
    private val unifyHeight: Boolean,
    private val rowHeight: Int,
    private val autoUnlockShift: Boolean = true,
    private val disableTouch: Boolean = false,
): InputViewComponent, KeyboardListener, CandidateListener {

    var connectedInputEngine: InputEngine? = null

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

    val currentKeyboardState: KeyboardState get() = keyboardState

    override fun initView(context: Context): View? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        doubleTapGap = preferences.getFloat("behaviour_double_tap_gap", 500f).toInt()
        keyboardViewType = preferences.getString("appearance_keyboard_view_type", "canvas") ?: keyboardViewType
        longPressAction = FlickLongPressAction.of(
            preferences.getString("behaviour_long_press_action", "shift") ?: "shift"
        )
        flickUpAction = FlickLongPressAction.of(
            preferences.getString("behaviour_flick_action_up", "shift") ?: "shift"
        )
        flickDownAction = FlickLongPressAction.of(
            preferences.getString("behaviour_flick_action_down", "symbol") ?: "symbol"
        )
        flickLeftAction = FlickLongPressAction.of(
            preferences.getString("behaviour_flick_action_left", "none") ?: "none"
        )
        flickRightAction = FlickLongPressAction.of(
            preferences.getString("behaviour_flick_action_", "none") ?: "none"
        )

        val name = preferences.getString("appearance_theme", "theme_dynamic")
        val theme = Themes.ofName(name)
        keyboardView = when(keyboardViewType) {
            "stacked_view" -> StackedViewKeyboardView(context, null, keyboard, theme, this, unifyHeight, rowHeight, disableTouch)
            else -> CanvasKeyboardView(context, null, keyboard, theme, this, unifyHeight, rowHeight, disableTouch = disableTouch)
        }
        return keyboardView
    }

    override fun reset() {
        updateView()
        val inputEngine = connectedInputEngine ?: return
        inputEngine.onReset()
    }

    override fun onCandidateItemClicked(candidate: Candidate) {
        val inputEngine = connectedInputEngine ?: return
        if(inputEngine is CandidateListener) inputEngine.onCandidateItemClicked(candidate)
    }

    override fun updateView() {
        val inputEngine = connectedInputEngine ?: return
        updateLabelsAndIcons(
            getShiftedLabels() + inputEngine.getLabels(keyboardState),
            inputEngine.getIcons(keyboardState)
        )
        updateMoreKeys(inputEngine.getMoreKeys(keyboardState))
        keyboardView?.apply {
            invalidate()
        }
    }

    private fun getShiftedLabels(): Map<Int, CharSequence> {
        fun label(label: String) =
            if(keyboardState.shiftState.pressed || keyboardState.shiftState.locked) label.uppercase()
            else label.lowercase()
        return keyboard.rows.flatMap { it.keys }
            .filterIsInstance<Key>()
            .associate { it.code to label(it.label.orEmpty()) }
    }

    private fun updateLabelsAndIcons(labels: Map<Int, CharSequence>, icons: Map<Int, Drawable>) {
        val keyboardView = keyboardView ?: return
        keyboardView.updateLabelsAndIcons(labels, icons)
    }

    private fun updateMoreKeys(moreKeys: Map<Int, Keyboard>) {
        val keyboardView = keyboardView ?: return
        val inputEngine = connectedInputEngine ?: return
        keyboardView.updateMoreKeyKeyboards(inputEngine.getMoreKeys(keyboardState))
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
        val inputEngine = connectedInputEngine ?: return
        if(ignoreCode != 0 && ignoreCode == code) {
            ignoreCode = 0
            return
        }
        when(code) {
            KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> {
            }
            KeyEvent.KEYCODE_CAPS_LOCK -> {
                val currentCapsLockState = keyboardState.shiftState.locked
                val newShiftState = keyboardState.shiftState.copy(pressed = !currentCapsLockState, locked = !currentCapsLockState)
                keyboardState = keyboardState.copy(shiftState = newShiftState)
                updateView()
            }
            KeyEvent.KEYCODE_DEL -> {
                inputEngine.onDelete()
            }
            KeyEvent.KEYCODE_SPACE -> {
                val state = keyboardState.copy()
                reset()
                keyboardState = state
                inputEngine.listener?.onCommitText(" ")
                autoUnlockShift()
            }
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                reset()
                inputEngine.listener?.onEditorAction(code)
                autoUnlockShift()
            }
            else -> {
                if(inputEngine.listener?.onSystemKey(code) != true) {
                    onPrintingKey(code, output, keyboardState)
                }
                autoUnlockShift()
            }
        }
        updateView()
    }

    override fun onKeyLongClick(code: Int, output: String?) {
        val inputEngine = connectedInputEngine ?: return
        longPressAction.onKey(code, keyboardState, inputEngine)
        ignoreCode = code
        inputHappened = true
    }

    private fun onPrintingKey(code: Int, output: String?, keyboardState: KeyboardState) {
        val inputEngine = connectedInputEngine ?: return
        if(code == 0 && output != null) {
            inputEngine.listener?.onCommitText(output)
        } else {
            inputEngine.onKey(code, keyboardState)
        }
        inputHappened = true
    }

    override fun onKeyFlick(direction: FlickDirection, code: Int, output: String?) {
        val inputEngine = connectedInputEngine ?: return
        val action = when(direction) {
            FlickDirection.Up -> flickUpAction
            FlickDirection.Down -> flickDownAction
            FlickDirection.Left -> flickLeftAction
            FlickDirection.Right -> flickRightAction
            else -> FlickLongPressAction.None
        }
        action.onKey(code, keyboardState, inputEngine)
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

}