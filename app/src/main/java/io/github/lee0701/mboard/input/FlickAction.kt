package io.github.lee0701.mboard.input

import io.github.lee0701.mboard.service.KeyboardState

sealed interface FlickAction {
    fun onKey(code: Int, keyboardState: KeyboardState, inputEngine: InputEngine)

    object None: FlickAction {
        override fun onKey(code: Int, keyboardState: KeyboardState, inputEngine: InputEngine) {
        }
    }

    object Shifted: FlickAction {
        override fun onKey(code: Int, keyboardState: KeyboardState, inputEngine: InputEngine) {
            inputEngine.onKey(code, makeShiftOn(keyboardState))
        }
    }

    object Symbols: FlickAction {
        override fun onKey(code: Int, keyboardState: KeyboardState, inputEngine: InputEngine) {
            if(inputEngine is BasicSoftInputEngine) {
                inputEngine.symbolsInputEngine?.onKey(code, keyboardState)
            }
        }
    }

    object ShiftedSymbols: FlickAction {
        override fun onKey(code: Int, keyboardState: KeyboardState, inputEngine: InputEngine) {
            if(inputEngine is BasicSoftInputEngine) {
                inputEngine.symbolsInputEngine?.onKey(code, makeShiftOn(keyboardState))
            }
        }
    }

    object AlternativeLanguage: FlickAction {
        override fun onKey(code: Int, keyboardState: KeyboardState, inputEngine: InputEngine) {
            if(inputEngine is BasicSoftInputEngine) {
                inputEngine.alternativeInputEngine?.onKey(code, keyboardState)
            }
        }
    }

    companion object {
        fun of(value: String): FlickAction {
            return when(value) {
                "symbol" -> Symbols
                "shift" -> Shifted
                "shift_symbol" -> ShiftedSymbols
                "alternative_language" -> AlternativeLanguage
                else -> None
            }
        }

        fun makeShiftOn(keyboardState: KeyboardState): KeyboardState
                = keyboardState.copy(shiftState = keyboardState.shiftState.copy(pressed = true))
    }

}