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
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import com.google.android.material.color.DynamicColors
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.input.*
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

    private fun reload(sharedPreferences: SharedPreferences, force: Boolean = false) {
        val hanjaConversionEnabled = sharedPreferences.getBoolean("input_hanja_conversion", false)
        val latinPresetKey = sharedPreferences.getString("layout_latin_preset", "layout_qwerty")!!
        val hangulPresetKey = sharedPreferences.getString("layout_hangul_preset", "layout_3set_390")!!

        // TODO: complete input engine.
        val latinInputEngine = InputEnginePresets.of(latinPresetKey, this)
//        val latinInputEngine = Yaml.default.decodeFromStream<InputEnginePreset>(assets.open("preset/preset_tablet_latin_qwerty.yaml")).inflate(this)
        val hangulInputEngine = InputEnginePresets.of(hangulPresetKey, this, hanjaConversionEnabled)
//        val hangulInputEngine = Yaml.default.decodeFromStream<InputEnginePreset>(assets.open("preset/preset_tablet_2set_ks5002.yaml")).inflate(this)
//        val hangulInputEngine = Yaml.default.decodeFromStream<InputEnginePreset>(assets.open("preset/preset_mobile_2set_ks5002_with_num.yaml")).inflate(this)
        val symbolInputEngine = InputEnginePresets.SymbolsG(this)

        if(latinInputEngine is BasicSoftInputEngine) {
            latinInputEngine.symbolsInputEngine = symbolInputEngine
            latinInputEngine.alternativeInputEngine = hangulInputEngine
        }
        if(hangulInputEngine is BasicSoftInputEngine) {
            hangulInputEngine.symbolsInputEngine = symbolInputEngine
            hangulInputEngine.alternativeInputEngine = latinInputEngine
        }

        val engines = listOf(
            latinInputEngine,
            hangulInputEngine,
            symbolInputEngine,
        ).map { it ?: DirectInputEngine(this) }

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
            inputEngineSwitcher?.getCurrentEngine()?.onReset()
            onCandidates(listOf())
        }
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
        return when(code) {
            KeyEvent.KEYCODE_LANGUAGE_SWITCH -> {
                inputEngineSwitcher?.getCurrentEngine()?.onReset()
                inputEngineSwitcher?.nextLanguage()
                reloadView()
                true
            }
            KeyEvent.KEYCODE_SYM -> {
                inputEngineSwitcher?.getCurrentEngine()?.onReset()
                inputEngineSwitcher?.nextExtra()
                reloadView()
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
        super.onComputeInsets(outInsets)
        outInsets?.contentTopInsets = outInsets?.visibleTopInsets
    }

    private fun setNavBarColor(@ColorInt color: Int) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.window?.navigationBarColor = color
        }
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