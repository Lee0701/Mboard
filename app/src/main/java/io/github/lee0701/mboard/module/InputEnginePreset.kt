package io.github.lee0701.mboard.module

import android.content.Context
import android.widget.Toast
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.decodeFromStream
import io.github.lee0701.converter.library.engine.HanjaConverter
import io.github.lee0701.converter.library.engine.Predictor
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.input.BasicSoftInputEngine
import io.github.lee0701.mboard.input.CodeConverterInputEngine
import io.github.lee0701.mboard.input.HangulInputEngine
import io.github.lee0701.mboard.input.HanjaConverterBuilder
import io.github.lee0701.mboard.input.HanjaConverterInputEngine
import io.github.lee0701.mboard.input.InputEngine
import io.github.lee0701.mboard.module.softkeyboard.Keyboard
import io.github.lee0701.mboard.module.table.CodeConvertTable
import io.github.lee0701.mboard.module.table.CompoundCodeConvertTable
import io.github.lee0701.mboard.module.table.JamoCombinationTable
import io.github.lee0701.mboard.service.MBoardIME
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.modules.EmptySerializersModule

@Serializable
sealed interface InputEnginePreset {

    fun inflate(ime: MBoardIME): InputEngine

    fun loadSoftKeyboards(context: Context, names: List<String>): Keyboard {
        val resolved = names.map { filename ->
            Yaml.default.decodeFromStream<Keyboard>(context.assets.open(filename))
        }
        return resolved.reduce { acc, input -> acc + input }
    }

    fun loadConvertTables(context: Context, names: List<String>): CodeConvertTable {
        val resolved = names.map { filename ->
            Yaml.default.decodeFromStream<CodeConvertTable>(context.assets.open(filename)) }
        return CompoundCodeConvertTable(resolved)
    }

    fun loadCombinationTable(context: Context, names: List<String>): JamoCombinationTable {
        val resolved = names.map { filename ->
            Yaml.default.decodeFromStream<JamoCombinationTable>(context.assets.open(filename)) }
        return resolved.reduce { acc, input -> acc + input }
    }

    @Serializable
    @SerialName("latin")
    data class Latin(
        val softKeyboard: List<String>,
        val codeConvertTable: List<String>,
    ): InputEnginePreset {
        override fun inflate(ime: MBoardIME): InputEngine {
            val keyboard = loadSoftKeyboards(ime, names = softKeyboard)
            val convertTable = loadConvertTables(ime, names = codeConvertTable)
            return BasicSoftInputEngine(
                keyboard = keyboard,
                getInputEngine = { listener -> CodeConverterInputEngine(convertTable, listener) },
                autoUnlockShift = true,
                listener = ime,
            )
        }
    }

    @Serializable
    @SerialName("hangul")
    data class Hangul(
        val softKeyboard: List<String>,
        val hangulTable: List<String>,
        val combinationTable: List<String>,
    ): InputEnginePreset {
        override fun inflate(ime: MBoardIME): InputEngine {
            val keyboard = loadSoftKeyboards(ime, names = softKeyboard)
            val convertTable = loadConvertTables(ime, names = hangulTable)
            val combinationTable = loadCombinationTable(ime, names = combinationTable)
            return BasicSoftInputEngine(
                keyboard = keyboard,
                getInputEngine = { listener -> HangulInputEngine(convertTable, combinationTable, listener) },
                autoUnlockShift = true,
                listener = ime,
            )
        }
    }

    @Serializable
    @SerialName("hangul-hanja")
    data class HangulHanja(
        val softKeyboard: List<String>,
        val hangulTable: List<String>,
        val combinationTable: List<String>,
    ): InputEnginePreset {
        override fun inflate(ime: MBoardIME): InputEngine {
            val keyboard = loadSoftKeyboards(ime, names = softKeyboard)
            val hangulTable = loadConvertTables(ime, names = hangulTable)
            val combinationTable = loadCombinationTable(ime, names = combinationTable)
            val (converter, _) = createHanjaConverter(ime, prediction = false)
            return BasicSoftInputEngine(keyboard, { listener ->
                HanjaConverterInputEngine({ l ->
                    HangulInputEngine(hangulTable, combinationTable, l)
                }, converter, null, listener)
            }, true, ime)
        }
    }

    @Serializable
    @SerialName("predicting-hangul-hanja")
    data class PredictingHangulHanja(
        val softKeyboard: List<String>,
        val hangulTable: List<String>,
        val combinationTable: List<String>,
    ): InputEnginePreset {
        override fun inflate(ime: MBoardIME): InputEngine {
            val keyboard = loadSoftKeyboards(ime, names = softKeyboard)
            val convertTable = loadConvertTables(ime, names = hangulTable)
            val combinationTable = loadCombinationTable(ime, names = combinationTable)
            val (converter, predictor) = createHanjaConverter(ime, prediction = true)
            return BasicSoftInputEngine(keyboard, { listener ->
                HanjaConverterInputEngine({ l ->
                    HangulInputEngine(convertTable, combinationTable, l)
                }, converter, predictor, listener)
            }, true, ime)
        }
    }

    companion object {
        val yamlConfig = YamlConfiguration(encodeDefaults = false)
        val yaml = Yaml(EmptySerializersModule(), yamlConfig)

        private fun createHanjaConverter(ime: MBoardIME, prediction: Boolean): Pair<HanjaConverter?, Predictor?> {
            if(prediction) {
                val (converter, predictor) = HanjaConverterBuilder.build(ime)
                if(converter != null && predictor != null) return converter to predictor
                else Toast.makeText(ime, R.string.msg_hanja_converter_donation_not_found, Toast.LENGTH_LONG).show()
            }

            val (converter, _) = HanjaConverterBuilder.build(ime)
            if(converter != null) return converter to null
            else Toast.makeText(ime, R.string.msg_hanja_converter_not_found, Toast.LENGTH_LONG).show()

            return null to null
        }

    }
}