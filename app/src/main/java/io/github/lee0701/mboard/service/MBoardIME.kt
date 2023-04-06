package io.github.lee0701.mboard.service

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.preference.PreferenceManager
import io.github.lee0701.mboard.input.DirectInputEngine
import io.github.lee0701.mboard.input.InputEngine
import io.github.lee0701.mboard.input.InputEnginePresets
import io.github.lee0701.mboard.input.SoftInputEngine

class MBoardIME: InputMethodService(), InputEngine.Listener {

    private var inputView: FrameLayout? = null
    private var inputEngineSwitcher: InputEngineSwitcher? = null

    override fun onCreate() {
        super.onCreate()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val hangulPresetKey = sharedPreferences.getString("layout_hangul_preset", "layout_3set_390")!!
        val latinPresetKey = sharedPreferences.getString("layout_latin_preset", "layout_qwerty")!!

        val engines = listOf(
            InputEnginePresets.of(latinPresetKey, this) ?: DirectInputEngine(this),
            InputEnginePresets.of(hangulPresetKey, this) ?: DirectInputEngine(this),
            InputEnginePresets.of("layout_symbols_g", this) ?: DirectInputEngine(this),
        )

        val table = arrayOf(
            intArrayOf(0, 2),
            intArrayOf(1, 2),
        )
        val switcher = InputEngineSwitcher(engines, table)
        switcher.initViews(this)
        this.inputEngineSwitcher = switcher
    }

    override fun onCreateInputView(): View {
        val inputView = FrameLayout(this, null)
        val currentInputEngine = inputEngineSwitcher?.getCurrentEngine()
        val keyboardView =
            if(currentInputEngine is SoftInputEngine) currentInputEngine.initView(this)
            else null
        if(keyboardView != null) {
            inputView.removeAllViews()
            inputView.addView(keyboardView)
        }
        this.inputView = inputView
        return inputView
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        inputEngineSwitcher?.getCurrentEngine()?.onReset()
    }

    override fun onFinishInput() {
        super.onFinishInput()
    }

    override fun onSystemKey(code: Int): Boolean {
        return when(code) {
            KeyEvent.KEYCODE_LANGUAGE_SWITCH -> {
                inputEngineSwitcher?.nextLanguage()
                updateView()
                true
            }
            KeyEvent.KEYCODE_SYM -> {
                inputEngineSwitcher?.nextExtra()
                updateView()
                true
            }
            else -> false
        }
    }

    override fun onEditorAction(code: Int) {
        if(!sendDefaultEditorAction(true)) sendDownUpKeyEvents(code)
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

    override fun onComputeInsets(outInsets: Insets?) {
        val inputView = this.inputView ?: return
        val currentEngine = inputEngineSwitcher?.getCurrentEngine()
        if(currentEngine is SoftInputEngine) currentEngine.onComputeInsets(inputView, outInsets)
        else return super.onComputeInsets(outInsets)
    }

    private fun updateView() {
        val inputView = inputView ?: return
        val inputEngine = inputEngineSwitcher?.getCurrentEngine()
        inputView.removeAllViews()
        if(inputEngine is SoftInputEngine) {
            inputView.addView(inputEngine.getView())
            inputEngine.onReset()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}