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
import io.github.lee0701.mboard.module.table.JamoCombinationTable
import io.github.lee0701.mboard.module.table.MoreKeysTable
import io.github.lee0701.mboard.service.MBoardIME
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.EmptySerializersModule

@Serializable
data class InputEnginePreset(
    val type: Type = Type.Latin,
    val softKeyboard: List<String> = listOf(),
    val moreKeysTable: List<String> = listOf(),
    val codeConvertTable: List<String> = listOf(),
    val combinationTable: List<String> = listOf(),
    val unifyHeight: Boolean = false,
    val defaultHeight: Boolean = true,
    val rowHeight: Int = 55,
    val autoUnlockShift: Boolean = true,
    val candidatesView: Boolean = false,
    val hanjaConversion: Boolean = false,
    val hanjaPrediction: Boolean = false,
) {

    fun inflate(context: Context, rootListener: InputEngine.Listener): InputEngine {
        val keyboard = loadSoftKeyboards(context, names = softKeyboard)
        val moreKeysTable = loadMoreKeysTable(context, names = moreKeysTable)
        val convertTable = loadConvertTable(context, names = codeConvertTable)
        val combinationTable = loadCombinationTable(context, names = combinationTable)

        val getHangulInputEngine = { listener: InputEngine.Listener ->
            if(hanjaConversion) {
                // TODO: Temporary workaround
                val (converter, predictor) = if(context is MBoardIME) {
                    createHanjaConverter(context, prediction = hanjaPrediction)
                } else {
                    (null to null)
                }
                HanjaConverterInputEngine(
                    { l -> HangulInputEngine(convertTable, moreKeysTable, combinationTable, l) },
                    converter,
                    predictor,
                    listener
                )
            } else {
                HangulInputEngine(
                    convertTable,
                    moreKeysTable,
                    combinationTable,
                    listener
                )
            }
        }

        val getInputEngine = { listener: InputEngine.Listener ->
            when(type) {
                Type.Latin -> {
                    CodeConverterInputEngine(convertTable, moreKeysTable, listener)
                }
                Type.Hangul -> {
                    getHangulInputEngine(listener)
                }
            }
        }

        return BasicSoftInputEngine(
            getInputEngine = getInputEngine,
            keyboard = keyboard,
            unifyHeight = unifyHeight,
            rowHeight = rowHeight,
            autoUnlockShift = autoUnlockShift,
            showCandidatesView = candidatesView,
            listener = rootListener,
        )
    }

    private fun loadSoftKeyboards(context: Context, names: List<String>): Keyboard {
        val resolved = names.map { filename ->
            yaml.decodeFromStream<Keyboard>(context.assets.open(filename))
        }
        return resolved.fold(Keyboard()) { acc, input -> acc + input }
    }

    private fun loadConvertTable(context: Context, names: List<String>): CodeConvertTable {
        val resolved = names.map { filename ->
            yaml.decodeFromStream<CodeConvertTable>(context.assets.open(filename)) }
        return resolved.reduce { acc, input -> acc + input }
    }

    private fun loadCombinationTable(context: Context, names: List<String>): JamoCombinationTable {
        val resolved = names.map { filename ->
            yaml.decodeFromStream<JamoCombinationTable>(context.assets.open(filename)) }
        return resolved.fold(JamoCombinationTable()) { acc, input -> acc + input }
    }

    private fun loadMoreKeysTable(context: Context, names: List<String>): MoreKeysTable {
        val resolved = names.map { filename ->
            val refMap = yaml.decodeFromStream<MoreKeysTable.RefMap>(context.assets.open(filename))
            refMap.resolve(context.assets, yaml)
        }
        return resolved.fold(MoreKeysTable()) { acc, input -> acc + input }
    }

    fun mutable(): Mutable {
        return Mutable(
            type = Type.Latin,
            unifyHeight = this.unifyHeight,
            defaultHeight = this.defaultHeight,
            rowHeight = this.rowHeight,
            autoUnlockShift = this.autoUnlockShift,
            showCandidatesView = this.candidatesView,
            enableHanjaConversion = this.hanjaConversion,
            enableHanjaPrediction = this.hanjaPrediction,
            softKeyboard = this.softKeyboard,
            moreKeysTable = this.moreKeysTable,
            codeConvertTable = this.codeConvertTable,
            combinationTable = this.combinationTable,
        )
    }

    data class Mutable (
        var type: Type = Type.Latin,
        var unifyHeight: Boolean = false,
        var defaultHeight: Boolean = true,
        var rowHeight: Int = 55,
        var autoUnlockShift: Boolean = true,
        var showCandidatesView: Boolean = false,
        var enableHanjaConversion: Boolean = false,
        var enableHanjaPrediction: Boolean = false,
        var softKeyboard: List<String> = listOf(),
        var moreKeysTable: List<String> = listOf(),
        var codeConvertTable: List<String> = listOf(),
        var combinationTable: List<String> = listOf(),
    ) {
        fun commit(): InputEnginePreset {
            return InputEnginePreset(
                type = type,
                softKeyboard = softKeyboard,
                moreKeysTable = moreKeysTable,
                codeConvertTable = codeConvertTable,
                combinationTable = combinationTable,
                unifyHeight = unifyHeight,
                defaultHeight = defaultHeight,
                rowHeight = rowHeight,
                candidatesView = showCandidatesView,
                autoUnlockShift = autoUnlockShift,
            )
        }
    }

    @Serializable
    enum class Type {
        Latin, Hangul
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