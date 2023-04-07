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

    fun SymbolsG(mBoardIME: MBoardIME): InputEngine {
        return BasicSoftInputEngine(
            mapper.readValue(mBoardIME.resources.openRawResource(R.raw.soft_qwerty_mobile), Keyboard::class.java).inflate(),
            CodeConverterInputEngine(mapper.readValue(mBoardIME.resources.openRawResource(R.raw.table_symbol_g), CodeConvertTable::class.java), mBoardIME),
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
                mapper.readValue(mBoardIME.resources.openRawResource(R.raw.table_hangul_3set_391), CodeConvertTable::class.java).inflate(),
                mapper.readValue(mBoardIME.resources.openRawResource(R.raw.comb_hangul_3set_391_strict), JamoCombinationTable::class.java).inflate(),
                mBoardIME,
            ),
            mBoardIME,
        )
    }

//    private val LAITN_QWERTY = { listener: InputEngine.Listener ->
//        BasicSoftInputEngine(
//            { SoftKeyboardLayout.LAYOUT_QWERTY_MOBILE },
//            { DirectInputEngine(it) },
//            listener,
//        )
//    }
//
//    private val SYMBOLS_G = { listener: InputEngine.Listener ->
//        BasicSoftInputEngine(
//            { SoftKeyboardLayout.LAYOUT_QWERTY_MOBILE_WITH_SEMICOLON },
//            { CodeConverterInputEngine(SymbolLayout.LAYOUT_SYMBOLS_G, it) },
//            listener,
//        )
//    }
//
//    private val map = mapOf<String, (InputEngine.Listener) -> InputEngine>(
//        "layout_latin_qwerty" to LAITN_QWERTY,
//
//        "layout_2set_ks5002" to HANGUL_2SET_KS5002,
//        "layout_3set_390" to HANGUL_3SET_390,
//        "layout_3set_391" to HANGUL_3SET_391,
//        "layout_3set_391_strict" to HANGUL_3SET_391_STRICT,
//
//        "layout_symbols_g" to SYMBOLS_G,
//    )
//
//    fun of(key: String, listener: InputEngine.Listener): InputEngine? {
//        return map[key]?.let { it(listener) }
//    }
}