package io.github.lee0701.mboard.input

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.dictionary.EmptyDictionary
import io.github.lee0701.mboard.module.CodeConvertTable
import io.github.lee0701.mboard.module.Keyboard
import io.github.lee0701.mboard.module.external.HanjaConverter
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
        return BasicSoftInputEngine(
            Yaml.default.decodeFromStream<Keyboard>(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile_dvorak_custom)),
            { listener -> CodeConverterInputEngine(
                Yaml.default.decodeFromStream<CodeConvertTable>(mBoardIME.resources.openRawResource(R.raw.table_latin_dvorak)),
                listener,
            ) },
            autoUnlockShift = true,
            mBoardIME,
        )
    }

    fun LatinColemak(mBoardIME: MBoardIME): InputEngine {
        return BasicSoftInputEngine(
            Yaml.default.decodeFromStream<Keyboard>(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile_with_semicolon)),
            { listener ->CodeConverterInputEngine(
                Yaml.default.decodeFromStream<CodeConvertTable>(mBoardIME.resources.openRawResource(R.raw.table_latin_colemak)),
                listener,
            ) },
            autoUnlockShift = true,
            mBoardIME,
        )
    }

    fun Hangul2SetKS5002(mBoardIME: MBoardIME): InputEngine {
        val dictionary = HanjaConverter.loadDictionary(mBoardIME) ?: EmptyDictionary()
        return BasicSoftInputEngine(
            Yaml.default.decodeFromStream(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile)),
            { listener -> HanjaConverterInputEngine({ listener -> HangulInputEngine(
                Yaml.default.decodeFromStream(mBoardIME.resources.openRawResource(R.raw.table_hangul_2set_ks5002)),
                Yaml.default.decodeFromStream(mBoardIME.resources.openRawResource(R.raw.comb_hangul_2set_ks5002)),
                listener,
            ) }, dictionary, listener) },
            autoUnlockShift = true,
            mBoardIME,
        )
    }

    fun Hangul2SetOldHangul(mBoardIME: MBoardIME): InputEngine {
        return BasicSoftInputEngine(
            Yaml.default.decodeFromStream(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile)),
            { listener -> HangulInputEngine(
                Yaml.default.decodeFromStream(mBoardIME.resources.openRawResource(R.raw.table_hangul_2set_old_hangul)),
                Yaml.default.decodeFromStream(mBoardIME.resources.openRawResource(R.raw.comb_hangul_full)),
                listener,
            ) },
            autoUnlockShift = true,
            mBoardIME,
        )
    }

    private fun Hangul3Set390(mBoardIME: MBoardIME): InputEngine {
        val dictionary = HanjaConverter.loadDictionary(mBoardIME) ?: EmptyDictionary()
        return BasicSoftInputEngine(
            Yaml.default.decodeFromStream(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile_3set_390)),
            { listener -> HanjaConverterInputEngine({ listener -> HangulInputEngine(
                Yaml.default.decodeFromStream(mBoardIME.resources.openRawResource(R.raw.table_hangul_3set_390)),
                Yaml.default.decodeFromStream(mBoardIME.resources.openRawResource(R.raw.comb_hangul_3set_390)),
                listener,
            ) }, dictionary, listener) },
            autoUnlockShift = true,
            mBoardIME,
        )
    }

    private fun Hangul3Set391(mBoardIME: MBoardIME): InputEngine {
        val dictionary = HanjaConverter.loadDictionary(mBoardIME) ?: EmptyDictionary()
        return BasicSoftInputEngine(
            Yaml.default.decodeFromStream(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile_3set_391)),
            { listener -> HanjaConverterInputEngine({ listener -> HangulInputEngine(
                Yaml.default.decodeFromStream(mBoardIME.resources.openRawResource(R.raw.table_hangul_3set_391)),
                Yaml.default.decodeFromStream(mBoardIME.resources.openRawResource(R.raw.comb_hangul_3set_391)),
                listener,
            ) }, dictionary, listener) },
            autoUnlockShift = true,
            mBoardIME,
        )
    }

    private fun Hangul3Set391Strict(mBoardIME: MBoardIME): InputEngine {
        val dictionary = HanjaConverter.loadDictionary(mBoardIME) ?: EmptyDictionary()
        return BasicSoftInputEngine(
            Yaml.default.decodeFromStream(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile_3set_391)),
            { listener -> HanjaConverterInputEngine({ listener -> HangulInputEngine(
                Yaml.default.decodeFromStream(mBoardIME.resources.openRawResource(R.raw.table_hangul_3set_391_strict)),
                Yaml.default.decodeFromStream(mBoardIME.resources.openRawResource(R.raw.comb_hangul_3set_391_strict)),
                listener,
            ) }, dictionary, listener) },
            autoUnlockShift = true,
            mBoardIME,
        )
    }

    fun SymbolsG(mBoardIME: MBoardIME): InputEngine {
        return BasicSoftInputEngine(
            Yaml.default.decodeFromStream(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile)),
            { listener ->
                CodeConverterInputEngine(Yaml.default.decodeFromStream(mBoardIME.resources.openRawResource(R.raw.table_symbol_g)), listener)
            },
            autoUnlockShift = false,
            mBoardIME,
        )
    }

    fun of(key: String, ime: MBoardIME): InputEngine? {
        return when(key) {
            "layout_latin_qwerty" -> LatinQWERTY(ime)
            "layout_latin_dvorak" -> LatinDvorak(ime)
            "layout_latin_colemak" -> LatinColemak(ime)
            "layout_2set_ks5002" -> Hangul2SetKS5002(ime)
            "layout_2set_old_hangul" -> Hangul2SetOldHangul(ime)
            "layout_3set_390" -> Hangul3Set390(ime)
            "layout_3set_391" -> Hangul3Set391(ime)
            "layout_3set_391_strict" -> Hangul3Set391Strict(ime)
            else -> null
        }
    }
}