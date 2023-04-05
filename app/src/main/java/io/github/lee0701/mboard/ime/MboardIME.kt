package io.github.lee0701.mboard.ime

import android.inputmethodservice.InputMethodService
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import io.github.lee0701.mboard.KeyboardListener
import io.github.lee0701.mboard.Layout
import io.github.lee0701.mboard.keyboard.Keyboard

class MboardIME: InputMethodService(), KeyboardListener {

    private var inputView: FrameLayout? = null
    private var keyboardView: Keyboard.ViewWrapper? = null

    private val keyCharacterMap: KeyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD)

    override fun onCreate() {
        super.onCreate()
    }

    override fun onCreateInputView(): View {
        val inputView = FrameLayout(this, null)
        val keyboardView = Layout.LAYOUT.initView(this, this)
        inputView.addView(keyboardView.binding.root)
        this.inputView = inputView
        this.keyboardView = keyboardView
        return inputView
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
    }

    override fun onKey(code: Int, output: String?) {
        sendDownUpKeyEvents(code)
    }

    override fun onDestroy() {
        super.onDestroy()
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