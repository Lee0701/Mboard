package io.github.lee0701.mboard.input

import io.github.lee0701.mboard.dictionary.HanjaDictionary
import io.github.lee0701.mboard.module.table.CodeConvertTable
import io.github.lee0701.mboard.module.softkeyboard.Keyboard
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
        val dictionary: HanjaDictionary,
    ): InputEnginePreset {
        private val hanjaConverter = DictionaryHanjaConverter(dictionary)
        override fun create(ime: MBoardIME): InputEngine {
            return BasicSoftInputEngine(keyboard, { listener ->
                HanjaConverterInputEngine({ l ->
                    HangulInputEngine(codeConvertTable, combinationTable, l)
                }, hanjaConverter, listener)
            }, true, ime)
        }
    }
}