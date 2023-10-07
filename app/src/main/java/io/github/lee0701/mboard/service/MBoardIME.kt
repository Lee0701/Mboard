package io.github.lee0701.mboard.service

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.charleskorn.kaml.decodeFromStream
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.module.candidates.Candidate
import io.github.lee0701.mboard.module.candidates.CandidateListener
import io.github.lee0701.mboard.module.candidates.DefaultCandidate
import io.github.lee0701.mboard.module.inputengine.InputEngine
import io.github.lee0701.mboard.preset.InputEnginePreset
import io.github.lee0701.mboard.preset.PresetLoader
import io.github.lee0701.mboard.preset.table.CustomKeycode
import io.github.lee0701.mboard_lib.conversion.Constants
import io.github.lee0701.mboard_lib.conversion.ExternalConversionResultBroadcastReceiver
import java.io.File

class MBoardIME: InputMethodService(), InputEngine.Listener, CandidateListener {
    private val handler: Handler = Handler(Looper.getMainLooper())

    private val clipboard: ClipboardManager by lazy { getSystemService(CLIPBOARD_SERVICE) as ClipboardManager }
    private var inputEngineSwitcher: InputEngineSwitcher? = null
    private var externalConversionResultBroadcastReceiver: ExternalConversionResultBroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        val externalConversionListener = ExternalConversionResultAdaptingListener(this)
        externalConversionResultBroadcastReceiver = ExternalConversionResultBroadcastReceiver(externalConversionListener)
        registerExternalConversionReceiver()
        reload()
    }

    private fun reload() {
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

        reloadView()
    }

    override fun onCreateInputView(): View {
        return inputEngineSwitcher?.initView(this) ?: View(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        reload()
    }

    override fun onCandidates(list: List<Candidate>) {
//        val sorted = list.sortedByDescending { it.score }
        inputEngineSwitcher?.showCandidates(list)
    }

    override fun onItemClicked(candidate: Candidate) {
        onComposingText(candidate.text)
        onFinishComposing()
        resetCurrentEngine()
        onCandidates(listOf())
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        resetCurrentEngine()
    }

    override fun onFinishInput() {
        super.onFinishInput()
    }

    override fun onSystemKey(code: Int): Boolean {
        val inputConnection = currentInputConnection ?: return false
        val extractedText = inputConnection.getExtractedText(ExtractedTextRequest(), 0)
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
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                resetCurrentEngine()
                sendDownUpKeyEvents(code)
                true
            }
            CustomKeycode.KEYCODE_COPY.code -> {
                val selectedText = inputConnection.getSelectedText(0)?.toString().orEmpty()
                val clip = ClipData.newPlainText(selectedText, selectedText)
                clipboard.setPrimaryClip(clip)
                true
            }
            CustomKeycode.KEYCODE_CUT.code -> {
                val selectedText = inputConnection.getSelectedText(0)?.toString().orEmpty()
                val clip = ClipData.newPlainText(selectedText, selectedText)
                clipboard.setPrimaryClip(clip)
                inputConnection.commitText("", 1)
                true
            }
            CustomKeycode.KEYCODE_PASTE.code -> {
                val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString().orEmpty()
                inputConnection.commitText(text, 1)
                true
            }
            CustomKeycode.KEYCODE_SELECT_ALL.code -> {
                extractedText.selectionStart = 0
                extractedText.selectionEnd = extractedText.text.length
                inputConnection.setSelection(extractedText.selectionStart, extractedText.selectionEnd)
                true
            }
            CustomKeycode.KEYCODE_EXPAND_SELECTION_LEFT.code -> {
                extractedText.selectionStart -= 1
                inputConnection.setSelection(extractedText.selectionStart, extractedText.selectionEnd)
                true
            }
            CustomKeycode.KEYCODE_EXPAND_SELECTION_RIGHT.code -> {
                extractedText.selectionEnd += 1
                inputConnection.setSelection(extractedText.selectionStart, extractedText.selectionEnd)
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
        if(text.isEmpty() && inputConnection.getSelectedText(0)?.isNotEmpty() == true) return
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

    // Still needed for pre-lollipop devices
    @Deprecated("Deprecated in Java")
    override fun onViewClicked(focusChanged: Boolean) {
        if(Build.VERSION.SDK_INT >= 34) return
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
        super.onDestroy()
        unregisterExternalConversionReceiver()
        externalConversionResultBroadcastReceiver = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val reload = intent?.getBooleanExtra(ACTION_RELOAD, false) == true
        if(reload) reload()
        return START_STICKY
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        super.onEvaluateFullscreenMode()
        return false
    }

    override fun onEvaluateInputViewShown(): Boolean {
        return super.onEvaluateInputViewShown()
    }

    private fun registerExternalConversionReceiver() {
        val receiver = externalConversionResultBroadcastReceiver ?: return
        ContextCompat.registerReceiver(
            this,
            receiver,
            IntentFilter(Constants.ACTION_CONVERT_TEXT_RESULT),
            Constants.PERMISSION_RECEIVE_CONVERTED_TEXT,
            handler,
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ContextCompat.RECEIVER_EXPORTED
            else ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun unregisterExternalConversionReceiver() {
        val receiver = externalConversionResultBroadcastReceiver ?: return
        unregisterReceiver(receiver)
    }

    class ExternalConversionResultAdaptingListener(
        val listener: InputEngine.Listener
    ): ExternalConversionResultBroadcastReceiver.Listener {
        override fun onCandidates(candidates: List<List<String>>) {
            val adaptedCandidates = candidates.map { (hangul, hanja, extra) -> DefaultCandidate(hanja) }
            listener.onCandidates(adaptedCandidates)
        }
    }

    companion object {
        const val ACTION_RELOAD = "io.github.lee0701.mboard.MBoardIME.ACTION_RELOAD"

        fun sendReloadIntent(activity: Activity) {
            val intent = Intent(activity, MBoardIME::class.java)
            intent.putExtra(MBoardIME.ACTION_RELOAD, true)
            activity.startService(intent)
        }

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