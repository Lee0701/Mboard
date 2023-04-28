package io.github.lee0701.mboard.preset

import android.content.Context
import io.github.lee0701.mboard.module.component.Component
import io.github.lee0701.mboard.module.component.CandidatesComponent
import kotlinx.serialization.Serializable

@Serializable
sealed interface ComponentPreset {

    fun inflate(context: Context, preset: InputEnginePreset): Component

    @Serializable
    object MainKeyboard: ComponentPreset {
        override fun inflate(context: Context, preset: InputEnginePreset): Component {
            TODO("Not yet implemented")
        }
    }

    @Serializable
    object NumberKeyboard: ComponentPreset {
        override fun inflate(context: Context, preset: InputEnginePreset): Component {
            TODO("Not yet implemented")
        }
    }

    @Serializable
    object TextEdit: ComponentPreset {
        override fun inflate(context: Context, preset: InputEnginePreset): Component {
            TODO("Not yet implemented")
        }
    }

    @Serializable
    object Candidates: ComponentPreset {
        override fun inflate(context: Context, preset: InputEnginePreset): Component {
            return CandidatesComponent()
        }
    }

    @Serializable
    object LanguageSwitcher: ComponentPreset {
        override fun inflate(context: Context, preset: InputEnginePreset): Component {
            TODO("Not yet implemented")
        }
    }
}