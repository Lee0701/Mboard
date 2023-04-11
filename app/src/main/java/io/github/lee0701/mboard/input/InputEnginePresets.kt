package io.github.lee0701.mboard.input

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.dictionary.EmptyDictionary
import io.github.lee0701.mboard.module.CodeConvertTable
import io.github.lee0701.mboard.module.Keyboard
import io.github.lee0701.mboard.module.external.HanjaConverter
import io.github.lee0701.mboard.module.hangul.JamoCombinationTable
import io.github.lee0701.mboard.service.MBoardIME

object InputEnginePresets {

    fun LatinQWERTY(mBoardIME: MBoardIME): InputEngine {
        return BasicSoftInputEngine(
            Yaml.default.decodeFromStream<Keyboard>(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile)),
            { listener -> DirectInputEngine(listener) },
            autoUnlockShift = true,
            mBoardIME,
        )
    }

    fun LatinDvorak(mBoardIME: MBoardIME): InputEngine {
        val keyboard = Yaml.default.decodeFromStream<Keyboard>(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile_dvorak_custom))
        val convertTable = Yaml.default.decodeFromStream<CodeConvertTable>(mBoardIME.resources.openRawResource(R.raw.table_latin_dvorak))
        return InputEnginePreset.Latin(keyboard, convertTable).create(mBoardIME)
    }

    fun LatinColemak(mBoardIME: MBoardIME): InputEngine {
        val keyboard = Yaml.default.decodeFromStream<Keyboard>(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile_with_semicolon))
        val convertTable = Yaml.default.decodeFromStream<CodeConvertTable>(mBoardIME.resources.openRawResource(R.raw.table_latin_colemak))
        return InputEnginePreset.Latin(keyboard, convertTable).create(mBoardIME)
    }

    fun Hangul2SetKS5002(mBoardIME: MBoardIME, hanja: Boolean): InputEngine {
        val keyboard = Yaml.default.decodeFromStream<Keyboard>(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile))
        val convertTable = Yaml.default.decodeFromStream<CodeConvertTable>(mBoardIME.resources.openRawResource(R.raw.table_hangul_2set_ks5002))
        val combinationTable = Yaml.default.decodeFromStream<JamoCombinationTable>(mBoardIME.resources.openRawResource(R.raw.comb_hangul_2set_ks5002))
        return if(hanja) {
            val dictionary = HanjaConverter.loadDictionary(mBoardIME) ?: EmptyDictionary()
            InputEnginePreset.Hanja(keyboard, convertTable, combinationTable, dictionary).create(mBoardIME)
        } else {
            InputEnginePreset.Hangul(keyboard, convertTable, combinationTable).create(mBoardIME)
        }
    }

    fun Hangul2SetOldHangul(mBoardIME: MBoardIME, hanja: Boolean): InputEngine {
        val keyboard = Yaml.default.decodeFromStream<Keyboard>(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile))
        val convertTable = Yaml.default.decodeFromStream<CodeConvertTable>(mBoardIME.resources.openRawResource(R.raw.table_hangul_2set_old_hangul))
        val combinationTable = Yaml.default.decodeFromStream<JamoCombinationTable>(mBoardIME.resources.openRawResource(R.raw.comb_hangul_full))
        return if(hanja) {
            val dictionary = HanjaConverter.loadDictionary(mBoardIME) ?: EmptyDictionary()
            InputEnginePreset.Hanja(keyboard, convertTable, combinationTable, dictionary).create(mBoardIME)
        } else {
            InputEnginePreset.Hangul(keyboard, convertTable, combinationTable).create(mBoardIME)
        }
    }

    private fun Hangul3Set390(mBoardIME: MBoardIME, hanja: Boolean): InputEngine {
        val keyboard = Yaml.default.decodeFromStream<Keyboard>(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile_3set_390))
        val convertTable = Yaml.default.decodeFromStream<CodeConvertTable>(mBoardIME.resources.openRawResource(R.raw.table_hangul_3set_390))
        val combinationTable = Yaml.default.decodeFromStream<JamoCombinationTable>(mBoardIME.resources.openRawResource(R.raw.comb_hangul_3set_390))
        return if(hanja) {
            val dictionary = HanjaConverter.loadDictionary(mBoardIME) ?: EmptyDictionary()
            InputEnginePreset.Hanja(keyboard, convertTable, combinationTable, dictionary).create(mBoardIME)
        } else {
            InputEnginePreset.Hangul(keyboard, convertTable, combinationTable).create(mBoardIME)
        }
    }

    private fun Hangul3Set391(mBoardIME: MBoardIME, hanja: Boolean): InputEngine {
        val keyboard = Yaml.default.decodeFromStream<Keyboard>(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile_3set_391))
        val convertTable = Yaml.default.decodeFromStream<CodeConvertTable>(mBoardIME.resources.openRawResource(R.raw.table_hangul_3set_391))
        val combinationTable = Yaml.default.decodeFromStream<JamoCombinationTable>(mBoardIME.resources.openRawResource(R.raw.comb_hangul_3set_391))
        return if(hanja) {
            val dictionary = HanjaConverter.loadDictionary(mBoardIME) ?: EmptyDictionary()
            InputEnginePreset.Hanja(keyboard, convertTable, combinationTable, dictionary).create(mBoardIME)
        } else {
            InputEnginePreset.Hangul(keyboard, convertTable, combinationTable).create(mBoardIME)
        }
    }

    private fun Hangul3Set391Strict(mBoardIME: MBoardIME, hanja: Boolean): InputEngine {
        val keyboard = Yaml.default.decodeFromStream<Keyboard>(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile_3set_391))
        val convertTable = Yaml.default.decodeFromStream<CodeConvertTable>(mBoardIME.resources.openRawResource(R.raw.table_hangul_3set_391_strict))
        val combinationTable = Yaml.default.decodeFromStream<JamoCombinationTable>(mBoardIME.resources.openRawResource(R.raw.comb_hangul_3set_391_strict))
        return if(hanja) {
            val dictionary = HanjaConverter.loadDictionary(mBoardIME) ?: EmptyDictionary()
            InputEnginePreset.Hanja(keyboard, convertTable, combinationTable, dictionary).create(mBoardIME)
        } else {
            InputEnginePreset.Hangul(keyboard, convertTable, combinationTable).create(mBoardIME)
        }
    }

    fun SymbolsG(mBoardIME: MBoardIME): InputEngine {
        val keyboard = Yaml.default.decodeFromStream<Keyboard>(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile))
        val convertTable = Yaml.default.decodeFromStream<CodeConvertTable>(mBoardIME.resources.openRawResource(R.raw.table_symbol_g))
        return InputEnginePreset.Symbol(keyboard, convertTable).create(mBoardIME)
    }

    fun of(key: String, ime: MBoardIME, hanja: Boolean = false): InputEngine? {
        return when(key) {
            "layout_latin_qwerty" -> LatinQWERTY(ime)
            "layout_latin_dvorak" -> LatinDvorak(ime)
            "layout_latin_colemak" -> LatinColemak(ime)
            "layout_2set_ks5002" -> Hangul2SetKS5002(ime, hanja)
            "layout_2set_old_hangul" -> Hangul2SetOldHangul(ime, hanja)
            "layout_3set_390" -> Hangul3Set390(ime, hanja)
            "layout_3set_391" -> Hangul3Set391(ime, hanja)
            "layout_3set_391_strict" -> Hangul3Set391Strict(ime, hanja)
            else -> null
        }
    }
}