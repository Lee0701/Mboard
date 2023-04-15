package io.github.lee0701.mboard.service

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.decodeFromStream
import com.charleskorn.kaml.encodeToStream
import io.github.lee0701.mboard.module.InputEnginePreset
import io.github.lee0701.mboard.module.softkeyboard.Keyboard
import io.github.lee0701.mboard.module.table.CodeConvertTable
import kotlinx.serialization.modules.EmptySerializersModule
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class ImportExportActivity: AppCompatActivity() {

    private val config = YamlConfiguration(encodeDefaults = false)
    private val yaml = Yaml(EmptySerializersModule(), config)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tables = listOf(
            "hangul_2set/table_hangul_2set_ks5002.yaml",
            "table_hangul_2set_old_hangul.yaml",
            "table_hangul_3set_390.yaml",
            "table_hangul_3set_391.yaml",
            "table_hangul_3set_391_strict.yaml",
            "table_latin_colemak.yaml",
            "table_latin_dvorak.yaml",
            "table_symbol_g.yaml",
        )
//        upgradeTables(tables)

        val keyboards = listOf(
            "soft_qwerty_mobile.yaml",
            "soft_qwerty_mobile_3set_390.yaml",
            "soft_qwerty_mobile_3set_391.yaml",
            "soft_qwerty_mobile_dvorak_custom.yaml",
            "soft_qwerty_mobile_with_num.yaml",
            "soft_qwerty_mobile_with_semicolon.yaml",
        )
//        upgradeKeyboards(keyboards)

        generatePreset()
    }

    private fun upgradeTables(names: List<String>) {
        names.forEach { name ->
            upgradeTable(
                assets.open(name.split('.').first()),
                File(filesDir, name).outputStream(),
            )
        }
    }

    private fun upgradeTable(input: InputStream, output: OutputStream) {
        val table = yaml.decodeFromStream<CodeConvertTable>(input)
//        val newTable = CodeConvertTable(map2 = table.map.map { (k, v) -> KeyEvent.keyCodeToString(k) to v.convert() }.toMap())
        val newTable = table
        yaml.encodeToStream(newTable, output)
    }

    private fun upgradeKeyboards(names: List<String>) {
        names.forEach { name ->
            val input = assets.open(name)
            val output = File(filesDir, name).outputStream()
            upgradeKeyboard(input, output)
        }
    }

    private fun upgradeKeyboard(input: InputStream, output: OutputStream) {
        val keyboard = yaml.decodeFromStream<Keyboard>(input)
        yaml.encodeToStream(keyboard, output)
    }

    private fun generatePreset() {
        val h2 = InputEnginePreset.HanjaHangul(
            softKeyboard = listOf("common/soft_qwerty_tablet.yaml"),
            hangulTable = listOf("hangul_2set/table_ks5002.yaml"),
            combinationTable = listOf("hangul_2set/comb_ks5002.yaml"),
        )
        Yaml.default.encodeToStream(h2, File(filesDir, "test.yaml").outputStream())
    }

}