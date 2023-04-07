package io.github.lee0701.mboard.input

import io.github.lee0701.mboard.layout.HangulLayout
import io.github.lee0701.mboard.layout.LatinLayout
import io.github.lee0701.mboard.layout.SoftKeyboardLayout
import io.github.lee0701.mboard.layout.SymbolLayout

object InputEnginePresets {

    private val HANGUL_2SET_KS5002 = { listener: InputEngine.Listener ->
        BasicSoftInputEngine(
            { SoftKeyboardLayout.LAYOUT_QWERTY_MOBILE },
            { HangulInputEngine(HangulLayout.LAYOUT_HANGUL_2SET_STANDARD, HangulLayout.COMB_2SET_STANDARD, it) },
            autoUnlockShift = true,
            listener,
        )
    }

    private val HANGUL_3SET_390 = { listener: InputEngine.Listener ->
        BasicSoftInputEngine(
            { SoftKeyboardLayout.LAYOUT_QWERTY_SEBEOLSIK_390_MOBILE },
            { HangulInputEngine(HangulLayout.LAYOUT_HANGUL_3SET_390, HangulLayout.COMB_SEBEOL_390, it) },
            autoUnlockShift = true,
            listener,
        )
    }

    private val HANGUL_3SET_391 = { listener: InputEngine.Listener ->
        BasicSoftInputEngine(
            { SoftKeyboardLayout.LAYOUT_QWERTY_SEBEOLSIK_391_MOBILE },
            { HangulInputEngine(HangulLayout.LAYOUT_HANGUL_3SET_391, HangulLayout.COMB_SEBEOL_391, it) },
            autoUnlockShift = true,
            listener,
        )
    }

    private val HANGUL_3SET_391_STRICT = { listener: InputEngine.Listener ->
        BasicSoftInputEngine(
            { SoftKeyboardLayout.LAYOUT_QWERTY_SEBEOLSIK_391_MOBILE },
            { HangulInputEngine(HangulLayout.LAYOUT_HANGUL_3SET_391_STRICT, HangulLayout.COMB_SEBEOL_391_STRICT, it) },
            autoUnlockShift = true,
            listener,
        )
    }

    private val LATIN_QWERTY = { listener: InputEngine.Listener ->
        BasicSoftInputEngine(
            { SoftKeyboardLayout.LAYOUT_QWERTY_MOBILE },
            { DirectInputEngine(it) },
            autoUnlockShift = true,
            listener,
        )
    }

    private val LATIN_DVORAK = { listener: InputEngine.Listener ->
        BasicSoftInputEngine(
            { SoftKeyboardLayout.LAYOUT_QWERTY_DVORAK_CUSTOM },
            { CodeConverterInputEngine(LatinLayout.LAYOUT_LATIN_DVORAK, it) },
            listener,
        )
    }

    private val LATIN_COLEMAK = { listener: InputEngine.Listener ->
        BasicSoftInputEngine(
            { SoftKeyboardLayout.LAYOUT_QWERTY_MOBILE_WITH_SEMICOLON },
            { CodeConverterInputEngine(LatinLayout.LAYOUT_LATIN_COLEMAK, it) },
            listener,
        )
    }

    private val SYMBOLS_G = { listener: InputEngine.Listener ->
        BasicSoftInputEngine(
            { SoftKeyboardLayout.LAYOUT_QWERTY_MOBILE_WITH_SEMICOLON },
            { CodeConverterInputEngine(SymbolLayout.LAYOUT_SYMBOLS_G, it) },
            autoUnlockShift = false,
            listener,
        )
    }

    private val map = mapOf<String, (InputEngine.Listener) -> InputEngine>(
        "layout_latin_qwerty" to LATIN_QWERTY,
        "layout_latin_dvorak" to LATIN_DVORAK,
        "layout_latin_colemak" to LATIN_COLEMAK,

        "layout_2set_ks5002" to HANGUL_2SET_KS5002,
        "layout_3set_390" to HANGUL_3SET_390,
        "layout_3set_391" to HANGUL_3SET_391,
        "layout_3set_391_strict" to HANGUL_3SET_391_STRICT,

        "layout_symbols_g" to SYMBOLS_G,
    )

    fun of(key: String, listener: InputEngine.Listener): InputEngine? {
        return map[key]?.let { it(listener) }
    }
}