package io.github.lee0701.mboard.preset

import android.content.Context
import io.github.lee0701.mboard.module.candidates.CandidateListener
import io.github.lee0701.mboard.module.component.Component
import io.github.lee0701.mboard.module.component.CandidatesComponent
import io.github.lee0701.mboard.module.component.EmptyComponent
import io.github.lee0701.mboard.module.component.KeyboardComponent
import kotlinx.serialization.Serializable

@Serializable
sealed interface ComponentPreset {

    fun inflate(context: Context): Component

    @Serializable
    data class Keyboard(
        val isMainKeyboard: Boolean = false,
    ): ComponentPreset {
        private var preset: InputEnginePreset? = null
        override fun inflate(context: Context): Component {
            val preset = preset ?: return EmptyComponent
            val keyboard = InputEnginePreset
                .loadSoftKeyboards(context, preset.layout.softKeyboard)
            return KeyboardComponent(
                keyboard = keyboard,
                unifyHeight = preset.size.unifyHeight,
                rowHeight = preset.size.rowHeight,
                autoUnlockShift = preset.autoUnlockShift,
                disableTouch = preset.type == InputEnginePreset.Type.Symbol,
            )
        }
    }

    @Serializable
    object TextEdit: ComponentPreset {
        override fun inflate(context: Context): Component {
            TODO("Not yet implemented")
        }
    }

    @Serializable
    data class Candidates(
        val listener: CandidateListener,
    ): ComponentPreset {
        override fun inflate(context: Context): Component {
            val candidatesComponent = CandidatesComponent()
            candidatesComponent.listener = listener
            return candidatesComponent
        }
    }

    @Serializable
    object LanguageSwitcher: ComponentPreset {
        override fun inflate(context: Context): Component {
            TODO("Not yet implemented")
        }
    }
}