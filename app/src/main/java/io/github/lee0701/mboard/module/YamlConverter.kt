package io.github.lee0701.mboard.module

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.github.lee0701.mboard.keyboard.Keyboard
import java.io.File

fun main() {
    val mapper = ObjectMapper(YAMLFactory())
    mapper.registerModule(KotlinModule.Builder().build())

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