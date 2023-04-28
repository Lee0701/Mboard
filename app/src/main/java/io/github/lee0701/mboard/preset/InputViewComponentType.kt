package io.github.lee0701.mboard.preset

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.module.candidates.CandidateListener
import io.github.lee0701.mboard.module.component.CandidatesComponent
import io.github.lee0701.mboard.module.component.EmptyComponent
import io.github.lee0701.mboard.module.component.InputViewComponent
import io.github.lee0701.mboard.module.component.KeyboardComponent
import io.github.lee0701.mboard.settings.KeyboardLayoutSettingsFragment

enum class InputViewComponentType(
    @DrawableRes val iconRes: Int,
    @StringRes val titleRes: Int,
) {
    MainKeyboard(
        R.drawable.baseline_keyboard_24,
        R.string.pref_layout_component_main_keyboard_title),
    NumberRow(
        R.drawable.baseline_123_24,
        R.string.pref_layout_component_number_row_title),
    Candidates(
        R.drawable.baseline_abc_24,
        R.string.pref_layout_component_candidates_title),
    TextSelection(R.drawable.baseline_text_select_move_forward_character,
        R.string.pref_layout_component_text_edit_title),
    LanguageTab(R.drawable.baseline_language_24,
        R.string.pref_layout_component_language_switcher_title);

    fun inflate(context: Context, preset: InputEnginePreset, disableTouch: Boolean): InputViewComponent {
        val loader = PresetLoader(context)
        return when(this) {
            MainKeyboard -> {
                KeyboardComponent(
                    keyboard = InputEnginePreset.loadSoftKeyboards(context, preset.layout.softKeyboard),
                    unifyHeight = preset.size.unifyHeight,
                    rowHeight = preset.size.rowHeight,
                    autoUnlockShift = preset.autoUnlockShift,
                    disableTouch = disableTouch,
                )
            }
            NumberRow -> {
                val layouts = loader.modFilenames(
                    listOf(KeyboardLayoutSettingsFragment.NUMBER_ROW_ID))
                KeyboardComponent(
                    keyboard = InputEnginePreset.loadSoftKeyboards(context, layouts),
                    unifyHeight = preset.size.unifyHeight,
                    rowHeight = preset.size.rowHeight,
                    autoUnlockShift = preset.autoUnlockShift,
                    disableTouch = disableTouch,
                )
            }
            Candidates -> {
                CandidatesComponent(
                    width = context.resources.displayMetrics.widthPixels,
                    height = preset.size.rowHeight,
                    disableTouch = disableTouch,
                ).apply {
                    if(context is CandidateListener) listener = context
                }
            }
            else -> {
                EmptyComponent
            }
        }
    }
}