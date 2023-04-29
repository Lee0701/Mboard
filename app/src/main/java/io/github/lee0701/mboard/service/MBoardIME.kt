package io.github.lee0701.mboard.service

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.charleskorn.kaml.decodeFromStream
import com.google.android.material.color.DynamicColors
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.input.BasicSoftInputEngine
import io.github.lee0701.mboard.input.Candidate
import io.github.lee0701.mboard.input.DefaultHanjaCandidate
import io.github.lee0701.mboard.input.InputEngine
import io.github.lee0701.mboard.input.SoftInputEngine
import io.github.lee0701.mboard.module.InputEnginePreset
import io.github.lee0701.mboard.view.candidates.BasicCandidatesViewManager
import io.github.lee0701.mboard.view.keyboard.Themes
import java.io.File
import kotlin.math.roundToInt

class MBoardIME: InputMethodService(), InputEngine.Listener, BasicCandidatesViewManager.Listener, OnSharedPreferenceChangeListener {

    private val sharedPreferences: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
    private var inputViewWrapper: ViewGroup? = null
    private var defaultCandidatesViewManager: BasicCandidatesViewManager? = null
    private var inputEngineSwitcher: InputEngineSwitcher? = null

    override fun onCreate() {
        super.onCreate()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        reload(sharedPreferences)
    }

    private fun reload(pref: SharedPreferences, force: Boolean = false) {
        val screenMode = pref.getString("layout_screen_mode", "mobile")

        val unifyHeight: Boolean = pref.getBoolean("appearance_unify_height", false)
        val rowHeight: Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            pref.getFloat("appearance_keyboard_height", 55f), resources.displayMetrics).toInt()

        fun modSize(size: InputEnginePreset.Size): InputEnginePreset.Size {
            val rowHeight: Int = if(size.defaultHeight) rowHeight
            else TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                size.rowHeight.toFloat(),
                resources.displayMetrics
            ).toInt()
            return size.copy(
                unifyHeight = unifyHeight,
                rowHeight = rowHeight
            )
        }

        fun modFilenames(fileNames: List<String>): List<String> {
            return fileNames.map { it.format(screenMode) }
        }

        fun modLayout(layout: InputEnginePreset.Layout): InputEnginePreset.Layout {
            return InputEnginePreset.Layout(
                softKeyboard = modFilenames(layout.softKeyboard),
                moreKeysTable = modFilenames(layout.moreKeysTable),
                codeConvertTable = modFilenames(layout.codeConvertTable),
                combinationTable = modFilenames(layout.combinationTable),
            )
        }

        fun modPreset(preset: InputEnginePreset): InputEnginePreset {
            return preset.copy(
                layout = modLayout(preset.layout),
                size = modSize(preset.size),
            )
        }

        fun modLatin(preset: InputEnginePreset): InputEnginePreset = modPreset(preset)
        fun modHangul(preset: InputEnginePreset): InputEnginePreset = modPreset(preset)
        fun modSymbol(preset: InputEnginePreset, language: String): InputEnginePreset {
            return when(language) {
                "ko" -> preset.copy(
                    layout = preset.layout.copy(
                        softKeyboard = modFilenames(preset.layout.softKeyboard),
                        moreKeysTable = modFilenames(preset.layout.moreKeysTable) + "symbol/morekeys_symbols_hangul.yaml",
                        codeConvertTable = modFilenames(preset.layout.codeConvertTable),
                        overrideTable = modFilenames(preset.layout.overrideTable) + "symbol/override_currency_won.yaml",
                        combinationTable = modFilenames(preset.layout.combinationTable),
                    ),
                    size = modSize(preset.size),
                )
                else -> modPreset(preset)
            }
        }

        val (latinPreset, hangulPreset, symbolPreset) = loadPresets(this)

        val latinModule = modLatin(latinPreset)
        val latinSymbolModule = modSymbol(symbolPreset, "en")
        val hangulModule = modHangul(hangulPreset)
        val hangulSymbolModule = modSymbol(symbolPreset, "ko")

        val latinInputEngine = latinModule.inflate(this, this)
        val latinSymbolInputEngine = latinSymbolModule.inflate(this, this)
        val hangulInputEngine = hangulModule.inflate(this, this)
        val hangulSymbolInputEngine = hangulSymbolModule.inflate(this, this)

        if(latinInputEngine is BasicSoftInputEngine) {
            latinInputEngine.symbolsInputEngine = latinSymbolInputEngine
            latinInputEngine.alternativeInputEngine = hangulInputEngine
        }
        if(hangulInputEngine is BasicSoftInputEngine) {
            hangulInputEngine.symbolsInputEngine = hangulSymbolInputEngine
            hangulInputEngine.alternativeInputEngine = latinInputEngine
        }

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

        defaultCandidatesViewManager = BasicCandidatesViewManager(this)

        if(force) reloadView()
    }

    override fun onCreateInputView(): View {
        val currentInputEngine = inputEngineSwitcher?.getCurrentEngine()
        val inputViewWrapper = LinearLayoutCompat(this, null).apply {
            orientation = LinearLayoutCompat.VERTICAL
        }

        if(currentInputEngine is SoftInputEngine && currentInputEngine.showCandidatesView) {
            val candidatesView = defaultCandidatesViewManager?.initView(this)
            if(candidatesView != null) {
                candidatesView.layoutParams = LinearLayoutCompat.LayoutParams(
                    LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                    resources.getDimension(R.dimen.candidates_view_height).roundToInt()
                )
                inputViewWrapper.addView(candidatesView)
            }
        }

        val keyboardView = inputEngineSwitcher?.initView(this)
        if(keyboardView != null) {
            if(currentInputEngine is SoftInputEngine) {
                keyboardView.layoutParams = LinearLayoutCompat.LayoutParams(
                    LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                    currentInputEngine.getHeight(),
                )
            }
            inputViewWrapper.addView(keyboardView)
            val name = sharedPreferences.getString("appearance_theme", "theme_dynamic")
            val theme = Themes.ofName(name)
            val context = ContextThemeWrapper(this, theme.keyboardBackground)
            val typedValue = TypedValue()
            val keyboardContext = DynamicColors.wrapContextIfAvailable(context, theme.keyboardBackground)
            keyboardContext.theme.resolveAttribute(R.attr.background, typedValue, true)
            val color = ContextCompat.getColor(this, typedValue.resourceId)
            setNavBarColor(color)
        }
        this.inputViewWrapper = inputViewWrapper
        inputEngineSwitcher?.updateView()
        return inputViewWrapper
    }

    override fun onCandidates(list: List<Candidate>) {
        val sorted = list.sortedByDescending { it.score }
        defaultCandidatesViewManager?.showCandidates(sorted)
    }

    override fun onItemClicked(candidate: Candidate) {
        if(candidate is DefaultHanjaCandidate) {
            val inputEngine = inputEngineSwitcher?.getCurrentEngine()
            if(inputEngine is BasicCandidatesViewManager.Listener) {
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
                true
            }
            KeyEvent.KEYCODE_SYM -> {
                resetCurrentEngine()
                inputEngineSwitcher?.nextExtra()
                reloadView()
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