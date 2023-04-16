package io.github.lee0701.mboard.input

import io.github.lee0701.converter.library.engine.HanjaConverter
import io.github.lee0701.converter.library.engine.Predictor
import io.github.lee0701.mboard.module.softkeyboard.Keyboard
import io.github.lee0701.mboard.module.table.CodeConvertTable
import io.github.lee0701.mboard.module.table.JamoCombinationTable
import io.github.lee0701.mboard.service.MBoardIME

sealed interface InputEnginePreset {

    fun create(ime: MBoardIME): InputEngine

    data class Symbol(
        val keyboard: Keyboard,
        val codeConvertTable: CodeConvertTable,
    ): InputEnginePreset {
        override fun create(ime: MBoardIME): InputEngine {
            return BasicSoftInputEngine(keyboard, { listener ->
                CodeConverterInputEngine(codeConvertTable, listener)
            }, false, ime)
        }
    }

    data class Latin(
        val keyboard: Keyboard,
        val codeConvertTable: CodeConvertTable,
    ): InputEnginePreset {
        override fun create(ime: MBoardIME): InputEngine {
            return BasicSoftInputEngine(keyboard, { listener ->
                CodeConverterInputEngine(codeConvertTable, listener)
            }, true, ime)
        }
    }

    data class Hangul(
        val keyboard: Keyboard,
        val codeConvertTable: CodeConvertTable,
        val combinationTable: JamoCombinationTable,
    ): InputEnginePreset {
        override fun create(ime: MBoardIME): InputEngine {
            return BasicSoftInputEngine(keyboard, { listener ->
                HangulInputEngine(codeConvertTable, combinationTable, listener)
            }, true, ime)
        }
    }

    data class Hanja(
        val keyboard: Keyboard,
        val codeConvertTable: CodeConvertTable,
        val combinationTable: JamoCombinationTable,
        val hanjaConverter: HanjaConverter,
        val predictor: Predictor?,
    ): InputEnginePreset {
        override fun create(ime: MBoardIME): InputEngine {
            return BasicSoftInputEngine(keyboard, { listener ->
                HanjaConverterInputEngine({ l ->
                    HangulInputEngine(codeConvertTable, combinationTable, l)
                }, hanjaConverter, predictor, listener)
            }, true, ime)
        }
    }
}