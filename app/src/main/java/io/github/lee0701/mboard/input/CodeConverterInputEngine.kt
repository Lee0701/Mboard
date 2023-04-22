package io.github.lee0701.mboard.input

import android.graphics.drawable.Drawable
import android.view.KeyCharacterMap
import io.github.lee0701.mboard.module.softkeyboard.Keyboard
import io.github.lee0701.mboard.module.table.CodeConvertTable
import io.github.lee0701.mboard.module.table.MoreKeysTable
import io.github.lee0701.mboard.module.table.SimpleCodeConvertTable
import io.github.lee0701.mboard.service.KeyboardState

class CodeConverterInputEngine(
    private val table: CodeConvertTable,
    private val moreKeysTable: MoreKeysTable,
    override val listener: InputEngine.Listener,
): InputEngine {

    private val keyCharacterMap: KeyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD)

    override fun onKey(code: Int, state: KeyboardState) {
        val converted = table.get(code, state) ?: keyCharacterMap.get(code, state.asMetaState())
        listener.onCommitText(converted.toChar().toString())
    }

    override fun onDelete() {
        listener.onDeleteText(1, 0)
    }

    override fun onTextAroundCursor(before: String, after: String) {
    }

    override fun onReset() {
        listener.onFinishComposing()
    }

    override fun getLabels(state: KeyboardState): Map<Int, CharSequence> {
        val codeMap = table.getAllForState(state).mapValues { (_, code) -> code.toChar().toString() }
        return DirectInputEngine.getLabels(keyCharacterMap, state) + codeMap
    }

    override fun getIcons(state: KeyboardState): Map<Int, Drawable> {
        return emptyMap()
    }

    override fun getMoreKeys(state: KeyboardState): Map<Int, Keyboard> {
        return moreKeysTable.map.mapNotNull { (code, value) ->
            val key = table.getReversed(code, SimpleCodeConvertTable.EntryKey.fromKeyboardState(state))
            if(key == null) null
            else key to value
        }.toMap()
    }
}