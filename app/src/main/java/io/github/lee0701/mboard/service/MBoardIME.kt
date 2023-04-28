package io.github.lee0701.mboard.service

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.CursorAnchorInfo
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import com.charleskorn.kaml.decodeFromStream
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.module.candidates.Candidate
import io.github.lee0701.mboard.module.candidates.CandidateListener
import io.github.lee0701.mboard.module.candidates.DefaultHanjaCandidate
import io.github.lee0701.mboard.module.inputengine.InputEngine
import io.github.lee0701.mboard.preset.InputEnginePreset
import io.github.lee0701.mboard.preset.PresetLoader
import java.io.File

class MBoardIME: InputMethodService(), InputEngine.Listener, CandidateListener, OnSharedPreferenceChangeListener {
    val handler: Handler = Handler(Looper.getMainLooper())

    private val sharedPreferences: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
    private var inputEngineSwitcher: InputEngineSwitcher? = null

    override fun onCreate() {
        super.onCreate()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        reload(sharedPreferences)
    }

    private fun reload(pref: SharedPreferences, force: Boolean = false) {
        val loader = PresetLoader(this)

        val (latinPreset, hangulPreset, symbolPreset) = loadPresets(this)

        val latinModule = loader.modLatin(latinPreset)
        val latinSymbolModule = loader.modSymbol(symbolPreset, "en")
        val hangulModule = loader.modHangul(hangulPreset)
        val hangulSymbolModule = loader.modSymbol(symbolPreset, "ko")

        val latinInputEngine = latinModule.inflate(this, this)
        val latinSymbolInputEngine = latinSymbolModule.inflate(this, this)
        val hangulInputEngine = hangulModule.inflate(this, this)
        val hangulSymbolInputEngine = hangulSymbolModule.inflate(this, this)

        latinInputEngine.symbolsInputEngine = latinSymbolInputEngine
        latinInputEngine.alternativeInputEngine = hangulInputEngine

        hangulInputEngine.symbolsInputEngine = hangulSymbolInputEngine
        hangulInputEngine.alternativeInputEngine = latinInputEngine

        val engines = listOf(
            latinInputEngine,
            hangulInputEngine,
            latinSymbolInputEngine,
            hangulSymbolInputEngine,
        )
        val table = arrayOf(
            intArrayOf(0, 2),
            intArrayOf(1, 3),
        )
        val switcher = InputEngineSwitcher(engines, table)
        this.inputEngineSwitcher = switcher

        if(force) reloadView()
    }

    override fun onCreateInputView(): View {
        return inputEngineSwitcher?.initView(this) ?: View(this)
    }

    override fun onCandidates(list: List<Candidate>) {
        val sorted = list.sortedByDescending { it.score }
        inputEngineSwitcher?.showCandidates(sorted)
    }

    override fun onItemClicked(candidate: Candidate) {
        if(candidate is DefaultHanjaCandidate) {
            val inputEngine = inputEngineSwitcher?.getCurrentEngine()
            if(inputEngine is CandidateListener) {
                inputEngine.onItemClicked(candidate)
            }
        } else {
            onComposingText(candidate.text)
            onFinishComposing()
            resetCurrentEngine()
            onCandidates(listOf())
        }
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        resetCurrentEngine()
    }

    override fun onFinishInput() {
        super.onFinishInput()
    }

    override fun onSystemKey(code: Int): Boolean {
        return when(code) {
            KeyEvent.KEYCODE_LANGUAGE_SWITCH -> {
                resetCurrentEngine()
                inputEngineSwitcher?.nextLanguage()
                reloadView()
                updateView()
                true
            }
            KeyEvent.KEYCODE_SYM -> {
                resetCurrentEngine()
                inputEngineSwitcher?.nextExtra()
                reloadView()
                updateView()
                true
            }
            KeyEvent.KEYCODE_TAB -> {
                sendDownUpKeyEvents(code)
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
        updateTextAroundCursor()
    }

    override fun onCommitText(text: CharSequence) {
        val inputConnection = currentInputConnection ?: return
        inputConnection.commitText(text, 1)
        updateTextAroundCursor()
    }

    override fun onDeleteText(beforeLength: Int, afterLength: Int) {
        val inputConnection = currentInputConnection ?: return
        inputConnection.deleteSurroundingText(beforeLength, afterLength)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        super.onUpdateSelection(
            oldSelStart,
            oldSelEnd,
            newSelStart,
            newSelEnd,
            candidatesStart,
            candidatesEnd
        )
        currentInputConnection?.requestCursorUpdates(InputConnection.CURSOR_UPDATE_MONITOR)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onUpdateCursorAnchorInfo(cursorAnchorInfo: CursorAnchorInfo?) {
        if(cursorAnchorInfo == null) return
        val composingText = cursorAnchorInfo.composingText ?: return
        val selectionEnd = cursorAnchorInfo.selectionStart
        val composingEnd = cursorAnchorInfo.composingTextStart + composingText.length
        if(selectionEnd != composingEnd) resetCurrentEngine()
    }

    // Still needed for pre-lollipop devices
    @Deprecated("Deprecated in Java")
    override fun onViewClicked(focusChanged: Boolean) {
        if(focusChanged) resetCurrentEngine()
    }

    override fun onComputeInsets(outInsets: Insets?) {
        super.onComputeInsets(outInsets)
        outInsets?.contentTopInsets = outInsets?.visibleTopInsets
    }

    private fun setNavBarColor(@ColorInt color: Int) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.window?.navigationBarColor = color
        }
    }

    private fun resetCurrentEngine() {
        val engine = inputEngineSwitcher?.getCurrentEngine() ?: return
        engine.onReset()
        engine.onResetComponents()
        updateTextAroundCursor()
    }

    private fun updateTextAroundCursor() {
        val engine = inputEngineSwitcher?.getCurrentEngine() ?: return
        val inputConnection = currentInputConnection ?: return
        val before = inputConnection.getTextBeforeCursor(100, 0).toString()
        val after = inputConnection.getTextAfterCursor(100, 0).toString()
        engine.onTextAroundCursor(before, after)
    }

    private fun reloadView() {
        setInputView(onCreateInputView())
    }

    private fun updateView() {
        inputEngineSwitcher?.updateView()
    }

    override fun onDestroy() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(sharedPreferences != null) {
            if(key == "requested_restart") {
                if(sharedPreferences.getBoolean(key, false)) reload(sharedPreferences, true)
            } else {
                reload(sharedPreferences, true)
            }
        }
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        super.onEvaluateFullscreenMode()
        return false
    }

    override fun onEvaluateInputViewShown(): Boolean {
        return super.onEvaluateInputViewShown()
    }

    companion object {
        fun loadPresets(context: Context): Triple<InputEnginePreset, InputEnginePreset, InputEnginePreset> {
            fun showToast(fileName: String) {
                val msg = context.getString(R.string.msg_preset_load_failed, fileName)
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }

            val latinFileName = "preset_latin.yaml"
            val hangulFileName = "preset_hangul.yaml"
            val symbolFileName = "preset_symbol.yaml"

            val latinPreset = loadPreset(context, latinFileName, "preset/preset_latin_qwerty.yaml")
                ?: InputEnginePreset().apply { showToast(latinFileName) }
            val hangulPreset = loadPreset(context, hangulFileName, "preset/preset_3set_390.yaml")
                ?: InputEnginePreset().apply { showToast(hangulFileName) }
            val symbolPreset = loadPreset(context, symbolFileName, "preset/preset_symbol_g.yaml")

                ?: InputEnginePreset().apply { showToast(symbolFileName) }
            return Triple(latinPreset, hangulPreset, symbolPreset)
        }

        private fun loadPreset(context: Context, fileName: String, defaultFilename: String): InputEnginePreset? {
            val fromFilesDir = kotlin.runCatching {
                InputEnginePreset.yaml.decodeFromStream<InputEnginePreset>(File(context.filesDir, fileName).inputStream())
            }
            if(fromFilesDir.isSuccess) return fromFilesDir.getOrNull()
            val fromAssets = kotlin.runCatching {
                InputEnginePreset.yaml.decodeFromStream<InputEnginePreset>(context.assets.open(fileName))
            }
            if(fromAssets.isSuccess) return fromAssets.getOrNull()
            val defaultFromAssets = kotlin.runCatching {
                InputEnginePreset.yaml.decodeFromStream<InputEnginePreset>(context.assets.open(defaultFilename))
            }
            if(defaultFromAssets.isSuccess) return defaultFromAssets.getOrNull()
            return null
        }

    }
}