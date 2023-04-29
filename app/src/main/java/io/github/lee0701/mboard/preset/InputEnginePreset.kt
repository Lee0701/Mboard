package io.github.lee0701.mboard.preset

import android.content.Context
import android.widget.Toast
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.decodeFromStream
import io.github.lee0701.converter.library.engine.HanjaConverter
import io.github.lee0701.converter.library.engine.Predictor
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.dictionary.DiskTrieDictionary
import io.github.lee0701.mboard.module.component.InputViewComponent
import io.github.lee0701.mboard.module.component.KeyboardComponent
import io.github.lee0701.mboard.module.inputengine.CodeConverterInputEngine
import io.github.lee0701.mboard.module.inputengine.HangulInputEngine
import io.github.lee0701.mboard.module.inputengine.HanjaConverterInputEngine
import io.github.lee0701.mboard.module.inputengine.InputEngine
import io.github.lee0701.mboard.module.kokr.HanjaConverterBuilder
import io.github.lee0701.mboard.preset.softkeyboard.Include
import io.github.lee0701.mboard.preset.softkeyboard.Keyboard
import io.github.lee0701.mboard.preset.softkeyboard.Row
import io.github.lee0701.mboard.preset.softkeyboard.RowItem
import io.github.lee0701.mboard.preset.table.CharOverrideTable
import io.github.lee0701.mboard.preset.table.CodeConvertTable
import io.github.lee0701.mboard.preset.table.JamoCombinationTable
import io.github.lee0701.mboard.preset.table.MoreKeysTable
import io.github.lee0701.mboard.preset.table.SimpleCodeConvertTable
import io.github.lee0701.mboard.service.MBoardIME
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.EmptySerializersModule

@Serializable
data class InputEnginePreset(
    val type: Type = Type.Latin,
    val size: Size = Size(),
    val layout: Layout = Layout(),
    val hanja: Hanja = Hanja(),
    val components: List<InputViewComponentType> = listOf(),
    val autoUnlockShift: Boolean = true,
    val candidatesView: Boolean = false,
) {

    fun inflate(context: Context, rootListener: InputEngine.Listener, disableTouch: Boolean = false): InputEngine {
        // Soft keyboards will be resolved later by components.
        val moreKeysTable = loadMoreKeysTable(context, names = layout.moreKeysTable)
        val convertTable = loadConvertTable(context, names = layout.codeConvertTable)
        val overrideTable = loadOverrideTable(context, names = layout.overrideTable)
        val combinationTable = loadCombinationTable(context, names = layout.combinationTable)

        fun inflateComponents(preset: InputEnginePreset): List<InputViewComponent> {
            return components.map { it.inflate(context, preset, disableTouch) }
        }
        val prefixDict = DiskTrieDictionary(context.assets.open("dict/dict-prefix.bin"))
        val ngramDict = DiskTrieDictionary(context.assets.open("dict/dict-ngram.bin"))
        val vocab = context.assets.open("dict/vocab.tsv").bufferedReader().readLines()
            .map { line -> line.split('\t') }
            .filter { it.size == 2 }
            .map { (k, v) -> k to v.toInt() }

        fun getHangulInputEngine(listener: InputEngine.Listener): InputEngine {
            return if(hanja.conversion) {
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
                        overrideTable = overrideTable,
                        moreKeysTable = moreKeysTable,
                        jamoCombinationTable = combinationTable,
                        listener = l,
                    ) },
                    vocab, prefixDict, ngramDict, listener
                )
            } else {
                HangulInputEngine(
                    convertTable = convertTable,
                    moreKeysTable = moreKeysTable,
                    overrideTable = overrideTable,
                    jamoCombinationTable = combinationTable,
                    listener = listener,
                )

            }
        }

        fun getTableInputEngine(listener: InputEngine.Listener): InputEngine {
            return CodeConverterInputEngine(
                convertTable = convertTable,
                moreKeysTable = moreKeysTable,
                overrideTable = overrideTable,
                listener = listener,
            )
        }

        fun getInputEngine(listener: InputEngine.Listener): InputEngine {
            return when(type) {
                Type.Latin -> getTableInputEngine(listener)
                Type.Hangul -> getHangulInputEngine(listener)
                Type.Symbol -> getTableInputEngine(listener)
            }.apply {
                components = inflateComponents(this@InputEnginePreset)
                components.filterIsInstance<KeyboardComponent>().forEach {
                    it.connectedInputEngine = this
                    it.updateView()
                }
            }
        }

        return getInputEngine(rootListener)
    }

    fun mutable(): Mutable {
        return Mutable(
            type = this.type,
            size = size.mutable(),
            layout = this.layout.mutable(),
            hanja = this.hanja.mutable(),
            components = this.components.toMutableList(),
            autoUnlockShift = this.autoUnlockShift,
        )
    }

    data class Mutable (
        var type: Type = Type.Latin,
        var size: Size.Mutable = Size.Mutable(),
        var layout: Layout.Mutable = Layout.Mutable(),
        var hanja: Hanja.Mutable = Hanja.Mutable(),
        var components: MutableList<InputViewComponentType> = mutableListOf(),
        var autoUnlockShift: Boolean = true
    ) {
        fun commit(): InputEnginePreset {
            return InputEnginePreset(
                type = type,
                size = size.commit(),
                layout = layout.commit(),
                hanja = hanja.commit(),
                components = components.toList(),
                autoUnlockShift = autoUnlockShift,
            )
        }
    }

    @Serializable
    enum class Type {
        Latin, Hangul, Symbol
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
        val overrideTable: List<String> = listOf(),
        val combinationTable: List<String> = listOf(),
    ) {
        fun mutable(): Mutable {
            return Mutable(
                softKeyboard = softKeyboard,
                moreKeysTable = moreKeysTable,
                codeConvertTable = codeConvertTable,
                overrideTable = overrideTable,
                combinationTable = combinationTable,
            )
        }

        data class Mutable(
            var softKeyboard: List<String> = listOf(),
            var moreKeysTable: List<String> = listOf(),
            var codeConvertTable: List<String> = listOf(),
            var overrideTable: List<String> = listOf(),
            var combinationTable: List<String> = listOf(),
        ) {
            fun commit(): Layout {
                return Layout(
                    softKeyboard = softKeyboard,
                    moreKeysTable = moreKeysTable,
                    codeConvertTable = codeConvertTable,
                    overrideTable = overrideTable,
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

        fun createHanjaConverter(ime: MBoardIME, prediction: Boolean, sortByContext: Boolean): Pair<HanjaConverter?, Predictor?> {
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

        fun resolveSoftKeyIncludes(context: Context, row: Row): List<RowItem> {
            return row.keys.flatMap { rowItem ->
                if(rowItem is Include) resolveSoftKeyIncludes(context,
                    yaml.decodeFromStream(context.assets.open(rowItem.name)))
                else listOf(rowItem)
            }
        }

        fun loadSoftKeyboards(context: Context, names: List<String>): Keyboard {
            val resolved = names.mapNotNull { filename ->
                val keyboard = kotlin
                    .runCatching { yaml.decodeFromStream<Keyboard>(context.assets.open(filename)) }
                    .getOrNull()
                    ?: return@mapNotNull null
                keyboard.copy(
                    rows = keyboard.rows.map { it.copy(resolveSoftKeyIncludes(context, it)) }
                )
            }
            return resolved.fold(Keyboard()) { acc, input -> acc + input }
        }

        fun loadConvertTable(context: Context, names: List<String>): CodeConvertTable {
            val resolved = names.map { filename ->
                yaml.decodeFromStream<CodeConvertTable>(context.assets.open(filename)) }
            return resolved.fold(SimpleCodeConvertTable() as CodeConvertTable) { acc, input -> acc + input }
        }

        fun loadOverrideTable(context: Context, names: List<String>): CharOverrideTable {
            val resolved = names.map { filename ->
                yaml.decodeFromStream<CharOverrideTable>(context.assets.open(filename)) }
            return resolved.fold(CharOverrideTable()) { acc, input -> acc + input }
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
    }
}