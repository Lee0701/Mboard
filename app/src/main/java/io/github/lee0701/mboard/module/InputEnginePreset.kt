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
import io.github.lee0701.mboard.module.softkeyboard.Include
import io.github.lee0701.mboard.module.softkeyboard.KeyLike
import io.github.lee0701.mboard.module.softkeyboard.Keyboard
import io.github.lee0701.mboard.module.softkeyboard.Row
import io.github.lee0701.mboard.module.table.CodeConvertTable
import io.github.lee0701.mboard.module.table.JamoCombinationTable
import io.github.lee0701.mboard.module.table.MoreKeysTable
import io.github.lee0701.mboard.service.MBoardIME
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.EmptySerializersModule

@Serializable
sealed interface InputEnginePreset {

    fun inflate(ime: MBoardIME): InputEngine

    fun resolveSoftKeyIncludes(context: Context, row: Row): List<KeyLike> {
        return row.keys.flatMap { keyLike ->
            if(keyLike is Include) resolveSoftKeyIncludes(context,
                yaml.decodeFromStream(context.assets.open(keyLike.name)))
            else listOf(keyLike)
        }
    }

    fun loadSoftKeyboards(context: Context, names: List<String>): Keyboard {
        val resolved = names.map { filename ->
            val keyboard = yaml.decodeFromStream<Keyboard>(context.assets.open(filename))
            return@map keyboard.copy(rows = keyboard.rows.map { it.copy(keys = resolveSoftKeyIncludes(context, it)) })
        }
        return resolved.fold(Keyboard()) { acc, input -> acc + input }
    }

    fun loadConvertTable(context: Context, names: List<String>): CodeConvertTable {
        val resolved = names.map { filename ->
            yaml.decodeFromStream<CodeConvertTable>(context.assets.open(filename)) }
        return resolved.reduce { acc, input -> acc + input }
    }

    fun loadCombinationTable(context: Context, names: List<String>): JamoCombinationTable {
        val resolved = names.map { filename ->
            yaml.decodeFromStream<JamoCombinationTable>(context.assets.open(filename)) }
        return resolved.fold(JamoCombinationTable()) { acc, input -> acc + input }
    }

    fun loadMoreKeysTable(context: Context, names: List<String>): MoreKeysTable {
        val resolved = names.map { filename ->
            val refMap = yaml.decodeFromStream<MoreKeysTable.RefMap>(context.assets.open(filename))
            refMap.resolve(context.assets, yaml)
        }
        return resolved.fold(MoreKeysTable()) { acc, input -> acc + input }
    }

    @Serializable
    @SerialName("latin")
    data class Latin(
        val softKeyboard: List<String> = listOf(),
        val moreKeysTable: List<String> = listOf(),
        val codeConvertTable: List<String> = listOf(),
        val autoUnlockShift: Boolean = true,
    ): InputEnginePreset {
        override fun inflate(ime: MBoardIME): InputEngine {
            val keyboard = loadSoftKeyboards(ime, names = softKeyboard)
            val moreKeysTable = loadMoreKeysTable(ime, names = moreKeysTable)
            val convertTable = loadConvertTable(ime, names = codeConvertTable)
            return BasicSoftInputEngine(
                keyboard = keyboard,
                getInputEngine = { listener -> CodeConverterInputEngine(convertTable, moreKeysTable, listener) },
                listener = ime,
                autoUnlockShift = autoUnlockShift,
            )
        }
    }

    @Serializable
    @SerialName("hangul")
    data class Hangul(
        val softKeyboard: List<String> = listOf(),
        val moreKeysTable: List<String> = listOf(),
        val hangulTable: List<String> = listOf(),
        val combinationTable: List<String> = listOf(),
    ): InputEnginePreset {
        override fun inflate(ime: MBoardIME): InputEngine {
            val keyboard = loadSoftKeyboards(ime, names = softKeyboard)
            val moreKeysTable = loadMoreKeysTable(ime, names = moreKeysTable)
            val convertTable = loadConvertTable(ime, names = hangulTable)
            val combinationTable = loadCombinationTable(ime, names = combinationTable)
            return BasicSoftInputEngine(
                keyboard = keyboard,
                getInputEngine = { listener -> HangulInputEngine(convertTable, moreKeysTable, combinationTable, listener) },
                autoUnlockShift = true,
                listener = ime,
            )
        }
    }

    @Serializable
    @SerialName("hangul-hanja")
    data class HangulHanja(
        val softKeyboard: List<String> = listOf(),
        val moreKeysTable: List<String> = listOf(),
        val hangulTable: List<String> = listOf(),
        val combinationTable: List<String> = listOf(),
    ): InputEnginePreset {
        override fun inflate(ime: MBoardIME): InputEngine {
            val keyboard = loadSoftKeyboards(ime, names = softKeyboard)
            val moreKeysTable = loadMoreKeysTable(ime, names = moreKeysTable)
            val hangulTable = loadConvertTable(ime, names = hangulTable)
            val combinationTable = loadCombinationTable(ime, names = combinationTable)
            val (converter, _) = createHanjaConverter(ime, prediction = false)
            return BasicSoftInputEngine(
                keyboard = keyboard,
                getInputEngine = { listener ->
                    HanjaConverterInputEngine({ l ->
                        HangulInputEngine(hangulTable, moreKeysTable, combinationTable, l)
                    }, converter, null, listener)
                },
                autoUnlockShift = true,
                listener = ime
            )
        }
    }

    @Serializable
    @SerialName("predicting-hangul-hanja")
    data class PredictingHangulHanja(
        val softKeyboard: List<String> = listOf(),
        val moreKeysTable: List<String> = listOf(),
        val hangulTable: List<String> = listOf(),
        val combinationTable: List<String> = listOf(),
    ): InputEnginePreset {
        override fun inflate(ime: MBoardIME): InputEngine {
            val keyboard = loadSoftKeyboards(ime, names = softKeyboard)
            val moreKeysTable = loadMoreKeysTable(ime, names = moreKeysTable)
            val convertTable = loadConvertTable(ime, names = hangulTable)
            val combinationTable = loadCombinationTable(ime, names = combinationTable)
            val (converter, predictor) = createHanjaConverter(ime, prediction = true)
            return BasicSoftInputEngine(
                keyboard = keyboard,
                getInputEngine = { listener ->
                    HanjaConverterInputEngine({ l ->
                        HangulInputEngine(convertTable, moreKeysTable, combinationTable, l)
                    }, converter, predictor, listener)
                },
                autoUnlockShift = true,
                listener = ime
            )
        }
    }

    companion object {
        private val yamlConfig = YamlConfiguration(encodeDefaults = false)
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