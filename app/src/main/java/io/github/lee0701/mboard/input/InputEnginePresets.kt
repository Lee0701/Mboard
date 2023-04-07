package io.github.lee0701.mboard.input

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.module.CodeConvertTable
import io.github.lee0701.mboard.module.Keyboard
import io.github.lee0701.mboard.module.hangul.JamoCombinationTable
import io.github.lee0701.mboard.service.MBoardIME

object InputEnginePresets {

    val mapper = ObjectMapper(YAMLFactory()).apply {
        registerModule(KotlinModule.Builder().build())
    }

    fun LatinQWERTY(mBoardIME: MBoardIME): InputEngine {
        return BasicSoftInputEngine(
            mapper.readValue(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile), Keyboard::class.java).inflate(),
            DirectInputEngine(mBoardIME),
            mBoardIME,
        )
    }

    fun LatinDvorak(mBoardIME: MBoardIME): InputEngine {
        return BasicSoftInputEngine(
            mapper.readValue(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile_dvorak_custom), Keyboard::class.java).inflate(),
            CodeConverterInputEngine(
                mapper.readValue(mBoardIME.resources.openRawResource(R.raw.table_latin_dvorak), CodeConvertTable::class.java).inflate(),
                mBoardIME,
            ),
            mBoardIME,
        )
    }

    fun LatinColemak(mBoardIME: MBoardIME): InputEngine {
        return BasicSoftInputEngine(
            mapper.readValue(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile_with_semicolon), Keyboard::class.java).inflate(),
            CodeConverterInputEngine(
                mapper.readValue(mBoardIME.resources.openRawResource(R.raw.table_latin_colemak), CodeConvertTable::class.java).inflate(),
                mBoardIME,
            ),
            mBoardIME,
        )
    }

    fun Hangul2SetKS5002(mBoardIME: MBoardIME): InputEngine {
        return BasicSoftInputEngine(
            mapper.readValue(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile), Keyboard::class.java).inflate(),
            HangulInputEngine(
                mapper.readValue(mBoardIME.resources.openRawResource(R.raw.table_hangul_2set_ks5002), CodeConvertTable::class.java).inflate(),
                mapper.readValue(mBoardIME.resources.openRawResource(R.raw.comb_hangul_2set_ks5002), JamoCombinationTable::class.java).inflate(),
                mBoardIME,
            ),
            mBoardIME,
        )
    }

    fun Hangul2SetOldHangul(mBoardIME: MBoardIME): InputEngine {
        return BasicSoftInputEngine(
            mapper.readValue(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile), Keyboard::class.java).inflate(),
            HangulInputEngine(
                mapper.readValue(mBoardIME.resources.openRawResource(R.raw.table_hangul_2set_old_hangul), CodeConvertTable::class.java).inflate(),
                mapper.readValue(mBoardIME.resources.openRawResource(R.raw.comb_hangul_full), JamoCombinationTable::class.java).inflate(),
                mBoardIME,
            ),
            mBoardIME,
        )
    }

    private fun Hangul3Set390(mBoardIME: MBoardIME): InputEngine {
        return BasicSoftInputEngine(
            mapper.readValue(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile_3set_390), Keyboard::class.java).inflate(),
            HangulInputEngine(
                mapper.readValue(mBoardIME.resources.openRawResource(R.raw.table_hangul_3set_390), CodeConvertTable::class.java).inflate(),
                mapper.readValue(mBoardIME.resources.openRawResource(R.raw.comb_hangul_3set_390), JamoCombinationTable::class.java).inflate(),
                mBoardIME,
            ),
            mBoardIME,
        )
    }

    private fun Hangul3Set391(mBoardIME: MBoardIME): InputEngine {
        return BasicSoftInputEngine(
            mapper.readValue(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile_3set_391), Keyboard::class.java).inflate(),
            HangulInputEngine(
                mapper.readValue(mBoardIME.resources.openRawResource(R.raw.table_hangul_3set_391), CodeConvertTable::class.java).inflate(),
                mapper.readValue(mBoardIME.resources.openRawResource(R.raw.comb_hangul_3set_391), JamoCombinationTable::class.java).inflate(),
                mBoardIME,
            ),
            mBoardIME,
        )
    }

    private fun Hangul3Set391Strict(mBoardIME: MBoardIME): InputEngine {
        return BasicSoftInputEngine(
            mapper.readValue(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile_3set_391), Keyboard::class.java).inflate(),
            HangulInputEngine(
                mapper.readValue(mBoardIME.resources.openRawResource(R.raw.table_hangul_3set_391_strict), CodeConvertTable::class.java).inflate(),
                mapper.readValue(mBoardIME.resources.openRawResource(R.raw.comb_hangul_3set_391_strict), JamoCombinationTable::class.java).inflate(),
                mBoardIME,
            ),
            mBoardIME,
        )
    }

    fun SymbolsG(mBoardIME: MBoardIME): InputEngine {
        return BasicSoftInputEngine(
            mapper.readValue(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile), Keyboard::class.java).inflate(),
            CodeConverterInputEngine(mapper.readValue(mBoardIME.resources.openRawResource(R.raw.table_symbol_g), CodeConvertTable::class.java), mBoardIME),
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