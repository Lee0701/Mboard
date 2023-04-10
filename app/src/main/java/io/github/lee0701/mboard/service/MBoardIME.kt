package io.github.lee0701.mboard.service

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.input.DirectInputEngine
import io.github.lee0701.mboard.input.InputEngine
import io.github.lee0701.mboard.input.InputEnginePresets
import io.github.lee0701.mboard.input.SoftInputEngine

class MBoardIME: InputMethodService(), InputEngine.Listener, OnSharedPreferenceChangeListener {

    private var inputView: FrameLayout? = null
    private var inputEngineSwitcher: InputEngineSwitcher? = null

    override fun onCreate() {
        super.onCreate()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        reload(sharedPreferences)
    }

    private fun reload(sharedPreferences: SharedPreferences, force: Boolean = false) {
        val hangulPresetKey = sharedPreferences.getString("layout_hangul_preset", "layout_3set_390")!!
        val latinPresetKey = sharedPreferences.getString("layout_latin_preset", "layout_qwerty")!!

        val engines = listOf(
            InputEnginePresets.of(latinPresetKey, this),
            InputEnginePresets.of(hangulPresetKey, this),
            InputEnginePresets.SymbolsG(this),
        ).map { it ?: DirectInputEngine(this) }

        val table = arrayOf(
            intArrayOf(0, 2),
            intArrayOf(1, 2),
        )
        val switcher = InputEngineSwitcher(engines, table)
        this.inputEngineSwitcher = switcher

        if(force) setInputView(onCreateInputView())
    }

    override fun onCreateInputView(): View {
        val inputView = FrameLayout(this, null)
        val keyboardView = inputEngineSwitcher?.initView(this)
        if(keyboardView != null) {
            inputView.removeAllViews()
            inputView.addView(keyboardView)
//            val typedValue = TypedValue()
//            keyboardView.context.theme.resolveAttribute(R.attr.background, typedValue, true)
//            val color = ContextCompat.getColor(this, typedValue.resourceId)
//            setNavBarColor(color)
        }
        this.inputView = inputView
        return inputView
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        val inputEngine = inputEngineSwitcher?.getCurrentEngine()
        inputEngine?.onReset()
    }

    override fun onFinishInput() {
        super.onFinishInput()
    }

    override fun onSystemKey(code: Int): Boolean {
        val inputEngine = inputEngineSwitcher?.getCurrentEngine()
        return when(code) {
            KeyEvent.KEYCODE_LANGUAGE_SWITCH -> {
                inputEngineSwitcher?.nextLanguage()
                inputEngine?.onReset()
                updateView()
                true
            }
            KeyEvent.KEYCODE_SYM -> {
                inputEngineSwitcher?.nextExtra()
                inputEngine?.onReset()
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

    private fun setNavBarColor(@ColorInt color: Int) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.window?.navigationBarColor = color
        }
    }

    private fun updateView() {
        setInputView(onCreateInputView())
    }

    override fun onDestroy() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(sharedPreferences != null) reload(sharedPreferences, true)
    }
}