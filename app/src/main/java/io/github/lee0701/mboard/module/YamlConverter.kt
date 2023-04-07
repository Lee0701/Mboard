package io.github.lee0701.mboard.module

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.github.lee0701.mboard.keyboard.Keyboard
import io.github.lee0701.mboard.module.hangul.JamoCombinationTable
import java.io.File

fun main() {
    val mapper = ObjectMapper(YAMLFactory())
    mapper.registerModule(KotlinModule.Builder().build())

//    convertSoftLayout(mapper)
    convertJamoCombinationTable(mapper)
//    convertCodeConvertTable(mapper)
}

fun convertCodeConvertTable(mapper: ObjectMapper) {
    val map = mapOf<String, CodeConvertTable>(
//        "conv_hangul_2set_ks5002" to CodeConvertTable(HangulLayout.LAYOUT_HANGUL_2SET_STANDARD),
//        "conv_hangul_3set_390" to CodeConvertTable(HangulLayout.LAYOUT_HANGUL_3SET_390),
//        "conv_hangul_3set_391" to CodeConvertTable(HangulLayout.LAYOUT_HANGUL_3SET_391),
//        "conv_symbol_g" to CodeConvertTable(SymbolLayout.LAYOUT_SYMBOLS_G)
    )
    map.entries.forEach { (k, v) ->
        mapper.writeValue(File("$k.yaml"), v)
    }
}

fun convertJamoCombinationTable(mapper: ObjectMapper) {
    val map = mapOf<String, JamoCombinationTable>(
//        "comb_hangul_3set_cho" to JamoCombinationTable(HangulLayout.COMB_3SET_CHO.map { listOf(it.key.first, it.key.second, it.value) }),
//        "comb_hangul_3set_jung" to JamoCombinationTable(HangulLayout.COMB_3SET_JUNG.map { listOf(it.key.first, it.key.second, it.value) }),
//        "comb_hangul_3set_jong" to JamoCombinationTable(HangulLayout.COMB_3SET_JONG.map { listOf(it.key.first, it.key.second, it.value) }),
//        "comb_hangul_2set_ks5002" to JamoCombinationTable(HangulLayout.COMB_2SET_STANDARD.map { listOf(it.key.first, it.key.second, it.value) }),
//        "comb_hangul_3set_390" to JamoCombinationTable(HangulLayout.COMB_SEBEOL_390.map { listOf(it.key.first, it.key.second, it.value) }),
//        "comb_hangul_3set_390" to JamoCombinationTable(HangulLayout.COMB_SEBEOL_390.map { listOf(it.key.first, it.key.second, it.value) }),
//        "comb_hangul_3set_391" to JamoCombinationTable(HangulLayout.COMB_SEBEOL_391.map { listOf(it.key.first, it.key.second, it.value) }),
//        "comb_hangul_3set_391_strict" to JamoCombinationTable(HangulLayout.COMB_SEBEOL_391_STRICT.map { listOf(it.key.first, it.key.second, it.value) }),
    )

    map.entries.forEach { (k, v) ->
        mapper.writeValue(File("$k.yaml"), v)
    }
}

fun convertSoftLayout(mapper: ObjectMapper) {
    val map = mapOf<String, Keyboard>(
//        "qwerty_mobile" to SoftKeyboardLayout.LAYOUT_QWERTY_MOBILE,
//        "qwerty_mobile_with_num" to SoftKeyboardLayout.LAYOUT_QWERTY_MOBILE_WITH_NUM,
//        "qwerty_mobile_with_semicolon" to SoftKeyboardLayout.LAYOUT_QWERTY_MOBILE_WITH_SEMICOLON,
//        "qwerty_mobile_3set_390" to SoftKeyboardLayout.LAYOUT_QWERTY_SEBEOLSIK_390_MOBILE,
//        "qwerty_mobile_3set_391" to SoftKeyboardLayout.LAYOUT_QWERTY_SEBEOLSIK_391_MOBILE,
    )

    map.entries.forEach { (k, v) ->
        mapper.writeValue(File("$k.yaml"), v)
    }
}