package io.github.lee0701.mboard.module

import android.content.Context
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import io.github.lee0701.mboard.dictionary.EmptyDictionary
import io.github.lee0701.mboard.input.BasicSoftInputEngine
import io.github.lee0701.mboard.input.CodeConverterInputEngine
import io.github.lee0701.mboard.input.DictionaryHanjaConverter
import io.github.lee0701.mboard.input.HangulInputEngine
import io.github.lee0701.mboard.input.HanjaConverterInputEngine
import io.github.lee0701.mboard.input.InputEngine
import io.github.lee0701.mboard.module.external.HanjaConverter
import io.github.lee0701.mboard.module.softkeyboard.Keyboard
import io.github.lee0701.mboard.module.table.CodeConvertTable
import io.github.lee0701.mboard.module.table.JamoCombinationTable
import io.github.lee0701.mboard.service.MBoardIME
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
sealed interface InputEnginePreset {

    fun inflate(ime: MBoardIME): InputEngine

    fun loadSoftKeyboards(context: Context, names: List<String>): Keyboard {
        val resolved = names.map { filename ->
            println(filename)
            Yaml.default.decodeFromStream<Keyboard>(context.assets.open(filename))
        }
        return resolved.reduce { acc, input -> acc + input }
    }

    fun loadConvertTables(context: Context, names: List<String>): CodeConvertTable {
        val resolved = names.map { filename ->
            Yaml.default.decodeFromStream<CodeConvertTable>(context.assets.open(filename)) }
        return resolved.reduce { acc, input -> acc + input }
    }

    fun loadCombinationTable(context: Context, names: List<String>): JamoCombinationTable {
        val resolved = names.map { filename ->
            Yaml.default.decodeFromStream<JamoCombinationTable>(context.assets.open(filename)) }
        return resolved.reduce { acc, input -> acc + input }
    }

    @SerialName("latin")
    @Serializable
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

    @SerialName("hangul")
    @Serializable
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

    @SerialName("hanja-hangul")
    @Serializable
    data class HanjaHangul(
        val softKeyboard: List<String>,
        val hangulTable: List<String>,
        val combinationTable: List<String>,
    ): InputEnginePreset {
        override fun inflate(ime: MBoardIME): InputEngine {
            val dictionary = HanjaConverter.loadDictionary(ime) ?: EmptyDictionary()
            val keyboard = loadSoftKeyboards(ime, names = softKeyboard)
            val convertTable = loadConvertTables(ime, names = hangulTable)
            val combinationTable = loadCombinationTable(ime, names = combinationTable)
            return BasicSoftInputEngine(
                keyboard = keyboard,
                getInputEngine = { listener ->
                    HanjaConverterInputEngine(
                        { l -> HangulInputEngine(convertTable, combinationTable, l) },
                        DictionaryHanjaConverter(dictionary),
                        listener
                    ) },
                autoUnlockShift = true,
                listener = ime,
            )
        }
    }

}