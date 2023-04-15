package io.github.lee0701.mboard.module.serialization

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import io.github.lee0701.mboard.module.softkeyboard.Keyboard
import kotlinx.serialization.encodeToString
import java.io.File

fun main() {
    val filename = "soft_qwerty_mobile.yaml"
    val data = Yaml.default.decodeFromStream<Keyboard>(File("app/src/main/assets/$filename").inputStream())
    val result = Yaml.default.encodeToString(data)
    println(result)
}