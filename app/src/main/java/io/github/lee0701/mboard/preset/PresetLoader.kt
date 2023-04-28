package io.github.lee0701.mboard.preset

import android.content.Context
import android.util.TypedValue
import androidx.preference.PreferenceManager
import kotlin.math.roundToInt

class PresetLoader(
    val context: Context,
) {
    val pref = PreferenceManager.getDefaultSharedPreferences(context)
    val screenMode = pref.getString("layout_screen_mode", "mobile") ?: "mobile"

    val unifyHeight: Boolean = pref.getBoolean("appearance_unify_height", false)
    val rowHeight: Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
        pref.getFloat("appearance_keyboard_height", 55f), context.resources.displayMetrics).toInt()

    fun mod(preset: InputEnginePreset): InputEnginePreset {
        return preset.copy(
            layout = modLayout(preset.layout),
            size = InputEnginePreset.Size(rowHeight = modHeight(preset.size.rowHeight)),
        )
    }

    fun modHeight(height: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            height.toFloat(),
            context.resources.displayMetrics
        ).roundToInt()
    }

    fun modSize(size: InputEnginePreset.Size): InputEnginePreset.Size {
        val rowHeight: Int = if(size.defaultHeight) rowHeight
        else TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            size.rowHeight.toFloat(),
            context.resources.displayMetrics
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

}