package io.github.lee0701.mboard.module

import android.content.Context
import android.widget.Toast
import androidx.preference.PreferenceManager
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
import io.github.lee0701.mboard.module.table.SimpleCodeConvertTable
import io.github.lee0701.mboard.service.MBoardIME
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.EmptySerializersModule

@Serializable
data class InputEnginePreset(
    val type: Type = Type.Latin,
    val size: Size = Size(),
    val layout: Layout = Layout(),
    val autoUnlockShift: Boolean = true,
    val candidatesView: Boolean = false,
    val hanja: Hanja = Hanja(),
) {

    fun inflate(context: Context, rootListener: InputEngine.Listener): InputEngine {
        val softKeyboard = loadSoftKeyboards(context, names = layout.softKeyboard)
        val moreKeysTable = loadMoreKeysTable(context, names = layout.moreKeysTable)
        val convertTable = loadConvertTable(context, names = layout.codeConvertTable)
        val combinationTable = loadCombinationTable(context, names = layout.combinationTable)

        val getHangulInputEngine = { listener: InputEngine.Listener ->
            if(hanja.conversion) {
                // TODO: Temporary workaround
                val (converter, predictor) = if(context is MBoardIME) {
                    createHanjaConverter(
                        context,
                        prediction = hanja.prediction,
                        sortByContext = hanja.sortByContext,
                    )
                } else {
                    (null to null)
                }
                HanjaConverterInputEngine(
                    { l -> HangulInputEngine(
                        convertTable = convertTable,
                        moreKeysTable = moreKeysTable,
                        jamoCombinationTable = combinationTable,
                        listener = l,
                    ) },
                    converter,
                    predictor,
                    listener,
                )
            } else {
                HangulInputEngine(
                    convertTable = convertTable,
                    moreKeysTable = moreKeysTable,
                    jamoCombinationTable = combinationTable,
                    listener,
                )
            }
        }

        val getInputEngine = { listener: InputEngine.Listener ->
            when(type) {
                Type.Latin -> {
                    CodeConverterInputEngine(
                        convertTable = convertTable,
                        moreKeysTable = moreKeysTable,
                        autoUnlockShift = autoUnlockShift,
                        listener = listener,
                    )
                }
                Type.Hangul -> {
                    getHangulInputEngine(listener)
                }
                Type.Symbols -> {
                    CodeConverterInputEngine(
                        convertTable = convertTable,
                        moreKeysTable = moreKeysTable,
                        autoUnlockShift = autoUnlockShift,
                        listener = listener,
                    )
                }
            }
        }

        return BasicSoftInputEngine(
            getInputEngine = getInputEngine,
            keyboard = softKeyboard,
            unifyHeight = size.unifyHeight,
            rowHeight = size.rowHeight,
            autoUnlockShift = autoUnlockShift,
            showCandidatesView = candidatesView,
            listener = rootListener,
        )
    }

    fun mutable(): Mutable {
        return Mutable(
            type = this.type,
            size = size.mutable(),
            autoUnlockShift = this.autoUnlockShift,
            showCandidatesView = this.candidatesView,
            layout = this.layout.mutable(),
            hanja = this.hanja.mutable(),
        )
    }

    data class Mutable (
        var type: Type = Type.Latin,
        var size: Size.Mutable = Size.Mutable(),
        var layout: Layout.Mutable = Layout.Mutable(),
        var autoUnlockShift: Boolean = true,
        var showCandidatesView: Boolean = false,
        var hanja: Hanja.Mutable = Hanja.Mutable()
    ) {
        fun commit(): InputEnginePreset {
            return InputEnginePreset(
                type = type,
                size = size.commit(),
                layout = layout.commit(),
                candidatesView = showCandidatesView,
                autoUnlockShift = autoUnlockShift,
                hanja = hanja.commit(),
            )
        }
    }

    @Serializable
    enum class Type {
        Latin, Hangul, Symbols
    }

    @Serializable
    data class Size(
        val unifyHeight: Boolean = false,
        val defaultHeight: Boolean = true,
        val rowHeight: Int = 55,
    ) {
        fun mutable(): Mutable {
            return Mutable(
                unifyHeight = unifyHeight,
                defaultHeight = defaultHeight,
                rowHeight = rowHeight,
            )
        }

        data class Mutable(
            var unifyHeight: Boolean = false,
            var defaultHeight: Boolean = true,
            var rowHeight: Int = 55,
        ) {
            fun commit(): Size {
                return Size(
                    unifyHeight = unifyHeight,
                    defaultHeight = defaultHeight,
                    rowHeight = rowHeight,
                )
            }
        }
    }

    @Serializable
    data class Layout(
        val softKeyboard: List<String> = listOf(),
        val moreKeysTable: List<String> = listOf(),
        val codeConvertTable: List<String> = listOf(),
        val combinationTable: List<String> = listOf(),
    ) {
        fun mutable(): Mutable {
            return Mutable(
                softKeyboard = softKeyboard,
                moreKeysTable = moreKeysTable,
                codeConvertTable = codeConvertTable,
                combinationTable = combinationTable,
            )
        }

        data class Mutable(
            var softKeyboard: List<String> = listOf(),
            var moreKeysTable: List<String> = listOf(),
            var codeConvertTable: List<String> = listOf(),
            var combinationTable: List<String> = listOf(),
        ) {
            fun commit(): Layout {
                return Layout(
                    softKeyboard = softKeyboard,
                    moreKeysTable = moreKeysTable,
                    codeConvertTable = codeConvertTable,
                    combinationTable = combinationTable,
                )
            }
        }
    }

    @Serializable
    data class Hanja(
        val conversion: Boolean = false,
        val prediction: Boolean = false,
        val sortByContext: Boolean = false,
        val additionalDictionaries: Set<String> = mutableSetOf(),
    ) {
        fun mutable(): Mutable {
            return Mutable(
                conversion = conversion,
                prediction = prediction,
                sortByContext = sortByContext,
                additionalDictionaries = additionalDictionaries.toMutableSet(),
            )
        }

        data class Mutable(
            var conversion: Boolean = false,
            var prediction: Boolean = false,
            var sortByContext: Boolean = false,
            var additionalDictionaries: MutableSet<String> = mutableSetOf(),
        ) {
            fun commit(): Hanja {
                return Hanja(
                    conversion = conversion,
                    prediction = prediction,
                    sortByContext = sortByContext,
                    additionalDictionaries = additionalDictionaries,
                )
            }
        }
    }

    companion object {
        private val yamlConfig = YamlConfiguration(encodeDefaults = false)
        val yaml = Yaml(EmptySerializersModule(), yamlConfig)

        private fun createHanjaConverter(ime: MBoardIME, prediction: Boolean, sortByContext: Boolean): Pair<HanjaConverter?, Predictor?> {
            if(prediction) {
                val (converter, predictor) = HanjaConverterBuilder.build(ime, true, sortByContext)
                if(converter != null && predictor != null) return converter to predictor
                else Toast.makeText(ime, R.string.msg_hanja_converter_donation_not_found, Toast.LENGTH_LONG).show()
            }

            val (converter, _) = HanjaConverterBuilder.build(ime, false, sortByContext)
            if(converter != null) return converter to null
            else Toast.makeText(ime, R.string.msg_hanja_converter_not_found, Toast.LENGTH_LONG).show()

            return null to null
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
            return resolved.fold(SimpleCodeConvertTable() as CodeConvertTable) { acc, input -> acc + input }
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

    }
}