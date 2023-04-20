package io.github.lee0701.mboard.service

import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.decodeFromStream
import com.charleskorn.kaml.encodeToStream
import io.github.lee0701.mboard.module.InputEnginePreset
import io.github.lee0701.mboard.module.serialization.KeyCodeSerializer
import io.github.lee0701.mboard.module.softkeyboard.Keyboard
import io.github.lee0701.mboard.module.table.CodeConvertTable
import io.github.lee0701.mboard.module.table.SimpleCodeConvertTable
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
            "hangul_2set/table_ks5002.yaml",
            "table_old_hangul.yaml",
            "hangul_3set/table_390.yaml",
            "hangul_3set/table_391.yaml",
            "hangul_3set/table_391_strict.yaml",
            "latin/table_latin_colemak.yaml",
            "latin/table_latin_dvorak.yaml",
            "symbol/table_symbol_g.yaml",
        )
//        upgradeTables(tables)

        val keyboards = listOf(
//            "common/soft_qwerty_mobile.yaml",
            "hangul_3set/soft_390_mobile.yaml",
            "hangul_3set/soft_391_mobile.yaml",
//            "latin/soft_qwerty_mobile_dvorak_custom.yaml",
//            "soft_qwerty_mobile_with_num.yaml",
//            "common/soft_qwerty_mobile_with_semicolon.yaml",
        )
//        upgradeKeyboards(keyboards)
//        generatePreset()
        val layouts = listOf(SYMBOL_A, SYMBOL_B)
        layouts.forEachIndexed { index, layout ->
            val table = importLayout(layout)
            val file = File(filesDir, "layout$index.yaml")
            yaml.encodeToStream(table, file.outputStream())
        }
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
            val file = File(filesDir, name)
            file.parentFile.mkdirs()
            val output = file.outputStream()
            upgradeKeyboard(input, output)
        }
    }

    private fun upgradeKeyboard(input: InputStream, output: OutputStream) {
        val keyboard = yaml.decodeFromStream<Keyboard>(input)
        yaml.encodeToStream(keyboard, output)
    }

    private fun generatePreset() {
        val h2 = InputEnginePreset.HangulHanja(
            softKeyboard = listOf("common/soft_qwerty_tablet.yaml"),
            hangulTable = listOf("hangul_2set/table_ks5002.yaml"),
            combinationTable = listOf("hangul_2set/comb_ks5002.yaml"),
        )
        Yaml.default.encodeToStream(h2, File(filesDir, "test.yaml").outputStream())
    }

    private fun importLayout(layout: Array<IntArray>): CodeConvertTable {
        val map = layout.associate { (code, base, shift) ->
            KeyEvent.keyCodeFromString(convertKeycode(code)) to SimpleCodeConvertTable.Entry(base, shift) }
        return SimpleCodeConvertTable(map = map)
    }

    private fun convertKeycode(code: Int): String {
        val converted = when(code) {
            in '0'.code .. '9'.code -> code - '0'.code + KeyEvent.KEYCODE_0
            in 'A'.code .. 'Z'.code -> code - 'A'.code + KeyEvent.KEYCODE_A
            in 'a'.code .. 'z'.code -> code - 'a'.code + KeyEvent.KEYCODE_A
            else -> null
        }
        return if(converted != null) KeyCodeSerializer.keyCodeToString(converted)
        else "0x" + code.toString(16)
    }

    companion object {
        val SYMBOL_A = arrayOf(
            intArrayOf(0x31, 0x21, 0x2460),
            intArrayOf(0x32, 0x40, 0x2461),
            intArrayOf(0x33, 0x23, 0x2462),
            intArrayOf(0x34, 0x24, 0x2463),
            intArrayOf(0x35, 0x25, 0x2464),
            intArrayOf(0x36, 0x5e, 0x2465),
            intArrayOf(0x37, 0x26, 0x2466),
            intArrayOf(0x38, 0x2a, 0x2467),
            intArrayOf(0x39, 0x28, 0x2468),
            intArrayOf(0x30, 0x29, 0x24ea),
            intArrayOf(113, 0x31, 0x21),
            intArrayOf(119, 0x32, 0x40),
            intArrayOf(101, 0x33, 0x23),
            intArrayOf(114, 0x34, 0x24),
            intArrayOf(116, 0x35, 0x25),
            intArrayOf(121, 0x36, 0x5e),
            intArrayOf(117, 0x37, 0x26),
            intArrayOf(105, 0x38, 0x2a),
            intArrayOf(111, 0x39, 0x28),
            intArrayOf(112, 0x30, 0x29),
            intArrayOf(97, 0x7e, 0x203b),
            intArrayOf(115, 0x27, 0x60),
            intArrayOf(100, 0x5b, 0x7b),
            intArrayOf(102, 0x5d, 0x7d),
            intArrayOf(103, 0x2f, 0x5c),
            intArrayOf(104, 0x3c, 0x2190),
            intArrayOf(106, 0x3e, 0x2193),
            intArrayOf(107, 0x3a, 0x2191),
            intArrayOf(108, 0x3b, 0x2192),
            intArrayOf(122, 0x5f, 0x7c),
            intArrayOf(120, 0xb7, 0x221a),
            intArrayOf(99, 0x3d, 0xf7),
            intArrayOf(118, 0x2b, 0xd7),
            intArrayOf(98, 0x3f, 0x03c0),
            intArrayOf(110, 0x2d, 0x300c),
            intArrayOf(109, 0x22, 0x300d),
        )

        val SYMBOL_B = arrayOf(
            intArrayOf(0x31, 0x31, 0x2460),
            intArrayOf(0x32, 0x32, 0x2461),
            intArrayOf(0x33, 0x33, 0x2462),
            intArrayOf(0x34, 0x34, 0x2463),
            intArrayOf(0x35, 0x35, 0x2464),
            intArrayOf(0x36, 0x36, 0x2465),
            intArrayOf(0x37, 0x37, 0x2466),
            intArrayOf(0x38, 0x38, 0x2467),
            intArrayOf(0x39, 0x39, 0x2468),
            intArrayOf(0x30, 0x30, 0x24ea),
            intArrayOf(113, 0x21, 0x25cb),
            intArrayOf(119, 0x40, 0x25cf),
            intArrayOf(101, 0x23, 0x25ce),
            intArrayOf(114, 0x24, 0x25a1),
            intArrayOf(116, 0x25, 0x25a0),
            intArrayOf(121, 0x5e, 0x2661),
            intArrayOf(117, 0x26, 0x2665),
            intArrayOf(105, 0x2a, 0x2606),
            intArrayOf(111, 0x28, 0x2605),
            intArrayOf(112, 0x29, 0x20a9),
            intArrayOf(97, 0x7e, 0x203b),
            intArrayOf(115, 0x27, 0x60),
            intArrayOf(100, 0x5b, 0x7b),
            intArrayOf(102, 0x5d, 0x7d),
            intArrayOf(103, 0x2f, 0x5c),
            intArrayOf(104, 0x3c, 0x2190),
            intArrayOf(106, 0x3e, 0x2193),
            intArrayOf(107, 0x3a, 0x2191),
            intArrayOf(108, 0x3b, 0x2192),
            intArrayOf(122, 0x5f, 0x7c),
            intArrayOf(120, 0xb7, 0x221a),
            intArrayOf(99, 0x3d, 0xf7),
            intArrayOf(118, 0x2b, 0xd7),
            intArrayOf(98, 0x3f, 0x03c0),
            intArrayOf(110, 0x2d, 0x300c),
            intArrayOf(109, 0x22, 0x300d),
        )
    }
}