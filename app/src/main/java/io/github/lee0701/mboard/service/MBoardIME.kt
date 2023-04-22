package io.github.lee0701.mboard.service

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.annotation.ColorInt
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
import io.github.lee0701.mboard.input.DirectInputEngine
import io.github.lee0701.mboard.input.InputEngine
import io.github.lee0701.mboard.input.SoftInputEngine
import io.github.lee0701.mboard.module.InputEnginePreset
import io.github.lee0701.mboard.view.candidates.BasicCandidatesViewManager
import io.github.lee0701.mboard.view.keyboard.Themes
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
        val latinFilename = pref.getString("layout_latin_preset", null)?.format(screenMode) ?: "preset/preset_mobile_latin_qwerty.yaml"
        val hangulFilename = pref.getString("layout_hangul_preset", null)?.format(screenMode) ?: "preset/preset_mobile_2set_ks5002.yaml"
        val symbolFilename = pref.getString("layout_symbol_preset", null)?.format(screenMode) ?: "preset/preset_mobile_symbol_g.yaml"
        val yaml = InputEnginePreset.yaml

        val hanjaConversionEnabled = pref.getBoolean("input_hanja_conversion", false)
        val hanjaPredictionEnabled = pref.getBoolean("input_hanja_prediction", false)

        fun modHangul(preset: InputEnginePreset): InputEnginePreset {
            if(preset !is InputEnginePreset.Hangul) return preset
            if(hanjaConversionEnabled && hanjaPredictionEnabled) {
                return InputEnginePreset.PredictingHangulHanja(
                    softKeyboard = preset.softKeyboard,
                    moreKeysTable = preset.moreKeysTable,
                    hangulTable = preset.hangulTable,
                    combinationTable = preset.combinationTable,
                )
            }
            if(hanjaConversionEnabled) {
                return InputEnginePreset.HangulHanja(
                    softKeyboard = preset.softKeyboard,
                    moreKeysTable = preset.moreKeysTable,
                    hangulTable = preset.hangulTable,
                    combinationTable = preset.combinationTable,
                )
            }
            return preset
        }

        val latinModule = yaml.decodeFromStream<InputEnginePreset>(assets.open(latinFilename))
        val hangulModule = modHangul(yaml.decodeFromStream<InputEnginePreset>(assets.open(hangulFilename)))
        val symbolModule = yaml.decodeFromStream<InputEnginePreset>(assets.open(symbolFilename))

        val latinInputEngine = latinModule.inflate(this)
        val hangulInputEngine = hangulModule.inflate(this)
        val symbolInputEngine = symbolModule.inflate(this)

        if(latinInputEngine is BasicSoftInputEngine) {
            latinInputEngine.symbolsInputEngine = symbolInputEngine
            latinInputEngine.alternativeInputEngine = hangulInputEngine
        }
        if(hangulInputEngine is BasicSoftInputEngine) {
            hangulInputEngine.symbolsInputEngine = symbolInputEngine
            hangulInputEngine.alternativeInputEngine = latinInputEngine
        }

        val empty = DirectInputEngine(this)

        val engines = listOf(
            latinInputEngine ?: empty,
            hangulInputEngine ?: empty,
            symbolInputEngine ?: empty,
        )

        val table = arrayOf(
            intArrayOf(0, 2),
            intArrayOf(1, 2),
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

        val candidatesView = defaultCandidatesViewManager?.initView(this)
        if(candidatesView != null) {
            candidatesView.layoutParams = LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                resources.getDimension(R.dimen.candidates_view_height).roundToInt()
            )
            inputViewWrapper.addView(candidatesView)
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
        if(sharedPreferences != null) reload(sharedPreferences, true)
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        super.onEvaluateFullscreenMode()
        return false
    }

    override fun onEvaluateInputViewShown(): Boolean {
        return super.onEvaluateInputViewShown()
    }
}