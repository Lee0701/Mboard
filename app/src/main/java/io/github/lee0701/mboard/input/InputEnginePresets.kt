package io.github.lee0701.mboard.input

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.module.CodeConvertTable
import io.github.lee0701.mboard.module.Keyboard
import io.github.lee0701.mboard.module.hangul.JamoCombinationTable
import io.github.lee0701.mboard.service.MBoardIME
import java.io.InputStream

object InputEnginePresets {

    fun LatinQWERTY(mBoardIME: MBoardIME): InputEngine {
        return BasicSoftInputEngine(
            Yaml.default.decodeFromStream<Keyboard>(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile)).inflate(),
            DirectInputEngine(mBoardIME),
            mBoardIME,
        )
    }

    fun LatinDvorak(mBoardIME: MBoardIME): InputEngine {
        return BasicSoftInputEngine(
            Yaml.default.decodeFromStream<Keyboard>(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile_dvorak_custom)).inflate(),
            CodeConverterInputEngine(
                Yaml.default.decodeFromStream<CodeConvertTable>(mBoardIME.resources.openRawResource(R.raw.table_latin_dvorak)).inflate(),
                mBoardIME,
            ),
            mBoardIME,
        )
    }

    fun LatinColemak(mBoardIME: MBoardIME): InputEngine {
        return BasicSoftInputEngine(
            Yaml.default.decodeFromStream<Keyboard>(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile_with_semicolon)).inflate(),
            CodeConverterInputEngine(
                Yaml.default.decodeFromStream<CodeConvertTable>(mBoardIME.resources.openRawResource(R.raw.table_latin_colemak)).inflate(),
                mBoardIME,
            ),
            mBoardIME,
        )
    }

    fun Hangul2SetKS5002(mBoardIME: MBoardIME): InputEngine {
        return BasicSoftInputEngine(
            Yaml.default.decodeFromStream<Keyboard>(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile)).inflate(),
            HangulInputEngine(
                Yaml.default.decodeFromStream<CodeConvertTable>(mBoardIME.resources.openRawResource(R.raw.table_hangul_2set_ks5002)).inflate(),
                Yaml.default.decodeFromStream<JamoCombinationTable>(mBoardIME.resources.openRawResource(R.raw.comb_hangul_2set_ks5002)).inflate(),
                mBoardIME,
            ),
            mBoardIME,
        )
    }

    fun Hangul2SetOldHangul(mBoardIME: MBoardIME): InputEngine {
        return BasicSoftInputEngine(
            Yaml.default.decodeFromStream<Keyboard>(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile)).inflate(),
            HangulInputEngine(
                Yaml.default.decodeFromStream<CodeConvertTable>(mBoardIME.resources.openRawResource(R.raw.table_hangul_2set_old_hangul)).inflate(),
                Yaml.default.decodeFromStream<JamoCombinationTable>(mBoardIME.resources.openRawResource(R.raw.comb_hangul_full)).inflate(),
                mBoardIME,
            ),
            mBoardIME,
        )
    }

    private fun Hangul3Set390(mBoardIME: MBoardIME): InputEngine {
        return BasicSoftInputEngine(
            Yaml.default.decodeFromStream<Keyboard>(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile_3set_390)).inflate(),
            HangulInputEngine(
                Yaml.default.decodeFromStream<CodeConvertTable>(mBoardIME.resources.openRawResource(R.raw.table_hangul_3set_390)).inflate(),
                Yaml.default.decodeFromStream<JamoCombinationTable>(mBoardIME.resources.openRawResource(R.raw.comb_hangul_3set_390)).inflate(),
                mBoardIME,
            ),
            mBoardIME,
        )
    }

    private fun Hangul3Set391(mBoardIME: MBoardIME): InputEngine {
        return BasicSoftInputEngine(
            Yaml.default.decodeFromStream<Keyboard>(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile_3set_391)).inflate(),
            HangulInputEngine(
                Yaml.default.decodeFromStream<CodeConvertTable>(mBoardIME.resources.openRawResource(R.raw.table_hangul_3set_391)).inflate(),
                Yaml.default.decodeFromStream<JamoCombinationTable>(mBoardIME.resources.openRawResource(R.raw.comb_hangul_3set_391)).inflate(),
                mBoardIME,
            ),
            mBoardIME,
        )
    }

    private fun Hangul3Set391Strict(mBoardIME: MBoardIME): InputEngine {
        return BasicSoftInputEngine(
            Yaml.default.decodeFromStream<Keyboard>(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile_3set_391)).inflate(),
            HangulInputEngine(
                Yaml.default.decodeFromStream<CodeConvertTable>(mBoardIME.resources.openRawResource(R.raw.table_hangul_3set_391_strict)).inflate(),
                Yaml.default.decodeFromStream<JamoCombinationTable>(mBoardIME.resources.openRawResource(R.raw.comb_hangul_3set_391_strict)).inflate(),
                mBoardIME,
            ),
            mBoardIME,
        )
    }

    fun SymbolsG(mBoardIME: MBoardIME): InputEngine {
        return BasicSoftInputEngine(
            Yaml.default.decodeFromStream<Keyboard>(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile)).inflate(),
            CodeConverterInputEngine(Yaml.default.decodeFromStream<CodeConvertTable>(mBoardIME.resources.openRawResource(R.raw.table_symbol_g)), mBoardIME),
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