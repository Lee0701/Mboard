package io.github.lee0701.mboard.input

import android.content.Context
import android.os.Build
import androidx.preference.PreferenceManager
import io.github.lee0701.converter.library.dictionary.HanjaDictionary
import io.github.lee0701.converter.library.dictionary.ListDictionary
import io.github.lee0701.converter.library.engine.CachingTFLitePredictor
import io.github.lee0701.converter.library.engine.CompoundHanjaConverter
import io.github.lee0701.converter.library.engine.ContextSortingHanjaConverter
import io.github.lee0701.converter.library.engine.DictionaryHanjaConverter
import io.github.lee0701.converter.library.engine.DictionaryManager
import io.github.lee0701.converter.library.engine.HanjaConverter
import io.github.lee0701.converter.library.engine.TFLitePredictor

object HanjaConverterBuilder {
    fun build(context: Context): HanjaConverter {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val converterContext = context.createPackageContext("io.github.lee0701.converter.donation", 0)

        val converters: MutableList<HanjaConverter> = mutableListOf()
        val isDonation = true
        val usePrediction = true
        val sortByContext = true

        val tfliteAvailable = isDonation && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && (usePrediction || sortByContext)
        val tfLitePredictor = if(tfliteAvailable) {
            TFLitePredictor(
                converterContext.assets.open("ml/wordlist.txt"),
                converterContext.assets.openFd("ml/model.tflite"),
            )
        } else null

        // Add Converter with Main Compound Dictionary
        val additional = preferences.getStringSet("input_hanja_additional_dictionaries", setOf()).orEmpty()
        val dictionaries: ListDictionary<HanjaDictionary.Entry> =
            DictionaryManager.loadCompoundDictionary(converterContext.assets, listOf("base") + additional)
        val dictionaryHanjaConverter: HanjaConverter =
            DictionaryHanjaConverter(dictionaries)

        if(tfLitePredictor != null && sortByContext) {
            converters += ContextSortingHanjaConverter(
                dictionaryHanjaConverter,
                CachingTFLitePredictor(tfLitePredictor)
            )
        } else {
            converters += dictionaryHanjaConverter
        }

        // Add Converters with Specialized Dictionaries
        // TODO: adapt and reimplement this
//        if(isDonation && preferences.getBoolean("search_by_translation", false)) {
//            val dictionary = DictionaryManager.loadDictionary(assets, "translation")
//            val color = ResourcesCompat.getColor(resources, R.color.searched_by_translation, theme)
//            if(dictionary != null) converters += SpecializedHanjaConverter(
//                dictionary,
//                color
//            )
//        }
//        if(BuildConfig.IS_DONATION && preferences.getBoolean("search_by_composition", false)) {
//            val dictionary = DictionaryManager.loadDictionary(assets, "composition")
//            val color = ResourcesCompat.getColor(resources, R.color.searched_by_composition, theme)
//            if(dictionary != null) converters += SpecializedHanjaConverter(
//                dictionary,
//                color
//            )
//        }

        val hanjaConverter: HanjaConverter =
            CompoundHanjaConverter(converters.toList())

        return hanjaConverter
    }

}