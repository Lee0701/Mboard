package ee.oyatl.ime.f.module.inputengine

import android.graphics.drawable.Drawable
import android.view.KeyCharacterMap
import ee.oyatl.ime.f.module.component.InputViewComponent
import ee.oyatl.ime.f.module.kokr.Hangul
import ee.oyatl.ime.f.module.kokr.HangulCombiner
import ee.oyatl.ime.f.preset.softkeyboard.Keyboard
import ee.oyatl.ime.f.preset.table.CharOverrideTable
import ee.oyatl.ime.f.preset.table.CodeConvertTable
import ee.oyatl.ime.f.preset.table.JamoCombinationTable
import ee.oyatl.ime.f.preset.table.LayeredCodeConvertTable
import ee.oyatl.ime.f.preset.table.LayeredCodeConvertTable.Companion.BASE_LAYER_NAME
import ee.oyatl.ime.f.preset.table.MoreKeysTable
import ee.oyatl.ime.f.service.KeyboardState

data class HangulInputEngine(
    private val convertTable: CodeConvertTable,
    private val moreKeysTable: MoreKeysTable,
    private val overrideTable: CharOverrideTable,
    private val jamoCombinationTable: JamoCombinationTable,
    override val listener: InputEngine.Listener,
): InputEngine {

    override var components: List<InputViewComponent> = listOf()

    private val keyCharacterMap: KeyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD)

    override var alternativeInputEngine: InputEngine? = null
    override var symbolsInputEngine: InputEngine? = null

    private val hangulCombiner = HangulCombiner(jamoCombinationTable)
    private val stateStack: MutableList<HangulCombiner.State> = mutableListOf()
    private val hangulState: HangulCombiner.State get() = stateStack.lastOrNull() ?: HangulCombiner.State()
    private val layerIdByHangulState: String get() {
        val cho = hangulState.cho
        val jung = hangulState.jung
        val jong = hangulState.jong

        return if(jong != null && jong and 0xff00000 == 0) "\$jong"
        else if(jung != null && jung and 0xff00000 == 0) "\$jung"
        else if(cho != null && cho and 0xff00000 == 0) "\$cho"
        else "base"
    }

    override fun onKey(code: Int, state: KeyboardState) {
        val converted =
            if(convertTable is LayeredCodeConvertTable) convertTable.get(layerIdByHangulState, code, state)
            else convertTable.get(code, state)
        if(converted == null) {
            val char = keyCharacterMap.get(code, state.asMetaState())
            onReset()
            if(char > 0) listener.onCommitText(char.toChar().toString())
        } else {
            val override = overrideTable.get(converted)
            val (text, hangulStates) = hangulCombiner.combine(hangulState, override ?: converted)
            if(text.isNotEmpty()) clearStack()
            this.stateStack += hangulStates
            if(text.isNotEmpty()) listener.onCommitText(text)
            listener.onComposingText(hangulStates.lastOrNull()?.composed ?: "")
        }
    }

    override fun onDelete() {
        if(stateStack.size >= 1) {
            stateStack.removeLast()
            listener.onComposingText(stateStack.lastOrNull()?.composed ?: "")
        }
        else listener.onDeleteText(1, 0)
    }

    override fun onTextAroundCursor(before: String, after: String) {
    }

    override fun onReset() {
        listener.onFinishComposing()
        clearStack()
    }

    fun clearStack() {
        stateStack.clear()
    }

    override fun getLabels(state: KeyboardState): Map<Int, CharSequence> {
        val table =
            if(convertTable is LayeredCodeConvertTable)
                convertTable.get(layerIdByHangulState) ?: convertTable.get(BASE_LAYER_NAME)
            else convertTable
        val codeMap = table?.getAllForState(state).orEmpty()
            .mapValues { (_, code) -> overrideTable.get(code) ?: code }
            .mapValues { (_, output) ->
                val ch = output and 0xffffff
                if(Hangul.isModernJamo(ch)) {
                    if(Hangul.isCho(ch)) Hangul.choToCompatConsonant(ch.toChar()).toString()
                    else if(Hangul.isJung(ch)) Hangul.jungToCompatVowel(ch.toChar()).toString()
                    else if(Hangul.isJong(ch)) Hangul.jongToCompatConsonant(ch.toChar()).toString()
                    else ch.toChar().toString()
                } else ch.toChar().toString()
            }
        return DirectInputEngine.getLabels(keyCharacterMap, state) + codeMap
    }

    override fun getIcons(state: KeyboardState): Map<Int, Drawable> {
        return emptyMap()
    }

    override fun getMoreKeys(state: KeyboardState): Map<Int, Keyboard> {
        return mapOf()
    }
}