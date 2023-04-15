package io.github.lee0701.mboard.input

import android.content.res.AssetManager
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import io.github.lee0701.mboard.dictionary.EmptyDictionary
import io.github.lee0701.mboard.module.table.CodeConvertTable
import io.github.lee0701.mboard.module.softkeyboard.Keyboard
import io.github.lee0701.mboard.module.external.HanjaConverter
import io.github.lee0701.mboard.module.table.JamoCombinationTable
import io.github.lee0701.mboard.service.MBoardIME

object InputEnginePresets {

    fun LatinQWERTY(mBoardIME: MBoardIME): InputEngine {
        val keyboard = loadKeyboard(mBoardIME.assets, "soft_qwerty_mobile.yaml")
        val convertTable = CodeConvertTable(mapOf())
        return InputEnginePreset.Latin(keyboard, convertTable).create(mBoardIME)
    }

    fun LatinDvorak(mBoardIME: MBoardIME): InputEngine {
        val keyboard = loadKeyboard(mBoardIME.assets, "soft_qwerty_mobile_dvorak_custom.yaml")
        val convertTable = loadConvertTable(mBoardIME.assets, "table_latin_dvorak.yaml")
        return InputEnginePreset.Latin(keyboard, convertTable).create(mBoardIME)
    }

    fun LatinColemak(mBoardIME: MBoardIME): InputEngine {
        val keyboard = loadKeyboard(mBoardIME.assets, "soft_qwerty_mobile_with_semicolon.yaml")
        val convertTable = loadConvertTable(mBoardIME.assets, "table_latin_colemak.yaml")
        return InputEnginePreset.Latin(keyboard, convertTable).create(mBoardIME)
    }

    fun Hangul2SetKS5002(mBoardIME: MBoardIME, hanja: Boolean): InputEngine {
        val keyboard = loadKeyboard(mBoardIME.assets, "common/soft_qwerty_mobile.yaml")
        val convertTable = loadConvertTable(mBoardIME.assets, "hangul_2set/table_ks5002.yaml")
        val combinationTable = loadJamoCombinationTable(mBoardIME.assets, "hangul_2set/comb_hangul_2set_ks5002.yaml")
        return if(hanja) {
            val dictionary = HanjaConverter.loadDictionary(mBoardIME) ?: EmptyDictionary()
            InputEnginePreset.Hanja(keyboard, convertTable, combinationTable, dictionary).create(mBoardIME)
        } else {
            InputEnginePreset.Hangul(keyboard, convertTable, combinationTable).create(mBoardIME)
        }
    }

    fun Hangul2SetOldHangul(mBoardIME: MBoardIME, hanja: Boolean): InputEngine {
        val keyboard = loadKeyboard(mBoardIME.assets, "soft_qwerty_mobile.yaml")
        val convertTable = loadConvertTable(mBoardIME.assets, "table_hangul_2set_old_hangul.yaml")
        val combinationTable = loadJamoCombinationTable(mBoardIME.assets, "comb_hangul_full.yaml")
        return if(hanja) {
            val dictionary = HanjaConverter.loadDictionary(mBoardIME) ?: EmptyDictionary()
            InputEnginePreset.Hanja(keyboard, convertTable, combinationTable, dictionary).create(mBoardIME)
        } else {
            InputEnginePreset.Hangul(keyboard, convertTable, combinationTable).create(mBoardIME)
        }
    }

    private fun Hangul3Set390(mBoardIME: MBoardIME, hanja: Boolean): InputEngine {
        val keyboard = loadKeyboard(mBoardIME.assets, "soft_qwerty_mobile_3set_390.yaml")
        val convertTable = loadConvertTable(mBoardIME.assets, "table_hangul_3set_390.yaml")
        val combinationTable = loadJamoCombinationTable(mBoardIME.assets, "comb_hangul_3set_390.yaml")
        return if(hanja) {
            val dictionary = HanjaConverter.loadDictionary(mBoardIME) ?: EmptyDictionary()
            InputEnginePreset.Hanja(keyboard, convertTable, combinationTable, dictionary).create(mBoardIME)
        } else {
            InputEnginePreset.Hangul(keyboard, convertTable, combinationTable).create(mBoardIME)
        }
    }

    private fun Hangul3Set391(mBoardIME: MBoardIME, hanja: Boolean): InputEngine {
        val keyboard = loadKeyboard(mBoardIME.assets, "soft_qwerty_mobile_3set_391.yaml")
        val convertTable = loadConvertTable(mBoardIME.assets, "table_hangul_3set_391.yaml")
        val combinationTable = loadJamoCombinationTable(mBoardIME.assets, "comb_hangul_3set_391.yaml")
        return if(hanja) {
            val dictionary = HanjaConverter.loadDictionary(mBoardIME) ?: EmptyDictionary()
            InputEnginePreset.Hanja(keyboard, convertTable, combinationTable, dictionary).create(mBoardIME)
        } else {
            InputEnginePreset.Hangul(keyboard, convertTable, combinationTable).create(mBoardIME)
        }
    }

    private fun Hangul3Set391Strict(mBoardIME: MBoardIME, hanja: Boolean): InputEngine {
        val keyboard = loadKeyboard(mBoardIME.assets, "soft_qwerty_mobile_3set_391.yaml")
        val convertTable = loadConvertTable(mBoardIME.assets, "table_hangul_3set_391_strict.yaml")
        val combinationTable = loadJamoCombinationTable(mBoardIME.assets, "comb_hangul_3set_391_strict.yaml")
        return if(hanja) {
            val dictionary = HanjaConverter.loadDictionary(mBoardIME) ?: EmptyDictionary()
            InputEnginePreset.Hanja(keyboard, convertTable, combinationTable, dictionary).create(mBoardIME)
        } else {
            InputEnginePreset.Hangul(keyboard, convertTable, combinationTable).create(mBoardIME)
        }
    }

    fun SymbolsG(mBoardIME: MBoardIME): InputEngine {
        val keyboard = loadKeyboard(mBoardIME.assets, "soft_qwerty_mobile_with_semicolon.yaml")
        val convertTable = loadConvertTable(mBoardIME.assets, "table_symbol_g.yaml")
        return InputEnginePreset.Symbol(keyboard, convertTable).create(mBoardIME)
    }

    private fun loadKeyboard(assets: AssetManager, name: String): Keyboard {
        return Yaml.default.decodeFromStream(assets.open(name))
    }

    private fun loadConvertTable(assets: AssetManager, name: String): CodeConvertTable {
        val customKeysTable: CodeConvertTable = Yaml.default.decodeFromStream(assets.open("table_custom_keys.yaml"))
        return customKeysTable + Yaml.default.decodeFromStream(assets.open(name))
    }

    private fun loadJamoCombinationTable(assets: AssetManager, name: String): JamoCombinationTable {
        return Yaml.default.decodeFromStream(assets.open(name))
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