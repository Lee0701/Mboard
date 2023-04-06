package io.github.lee0701.mboard.input

import io.github.lee0701.mboard.layout.HangulLayout
import io.github.lee0701.mboard.layout.SoftKeyboardLayout
import io.github.lee0701.mboard.layout.SymbolLayout

object InputEnginePresets {

    private val HANGUL_2SET_KS5002 = { listener: InputEngine.Listener ->
        BasicSoftInputEngine(
            { SoftKeyboardLayout.LAYOUT_QWERTY_MOBILE },
            { HangulInputEngine(HangulLayout.LAYOUT_HANGUL_DUBEOL_STANDARD, HangulLayout.COMB_DUBEOL_STANDARD, it) },
            listener,
        )
    }

    private val HANGUL_3SET_390 = { listener: InputEngine.Listener ->
        BasicSoftInputEngine(
            { SoftKeyboardLayout.LAYOUT_QWERTY_SEBEOLSIK_390_MOBILE },
            { HangulInputEngine(HangulLayout.LAYOUT_HANGUL_SEBEOL_390, HangulLayout.COMB_SEBEOL_390, it) },
            listener,
        )
    }

    private val LAITN_QWERTY = { listener: InputEngine.Listener ->
        BasicSoftInputEngine(
            { SoftKeyboardLayout.LAYOUT_QWERTY_MOBILE },
            { DirectInputEngine(it) },
            listener,
        )
    }

    private val SYMBOLS_G = { listener: InputEngine.Listener ->
        BasicSoftInputEngine(
            { SoftKeyboardLayout.LAYOUT_QWERTY_MOBILE_WITH_SEMICOLON },
            { CodeConverterInputEngine(SymbolLayout.LAYOUT_SYMBOLS_G, it) },
            listener,
        )
    }

    private val map = mapOf<String, (InputEngine.Listener) -> InputEngine>(
        "layout_latin_qwerty" to LAITN_QWERTY,

        "layout_2set_ks5002" to HANGUL_2SET_KS5002,
        "layout_3set_390" to HANGUL_3SET_390,

        "layout_symbols_g" to SYMBOLS_G,
    )

    fun of(key: String, listener: InputEngine.Listener): InputEngine? {
        return map[key]?.let { it(listener) }
    }
}