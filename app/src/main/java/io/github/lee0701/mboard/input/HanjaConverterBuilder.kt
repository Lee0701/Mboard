package io.github.lee0701.mboard.input

import android.content.Context
import android.os.Build
import androidx.preference.PreferenceManager
import engine.DictionaryPredictor
import io.github.lee0701.converter.library.engine.CachingTFLitePredictor
import io.github.lee0701.converter.library.engine.CompoundHanjaConverter
import io.github.lee0701.converter.library.engine.ContextSortingHanjaConverter
import io.github.lee0701.converter.library.engine.DictionaryHanjaConverter
import io.github.lee0701.converter.library.engine.DictionaryManager
import io.github.lee0701.converter.library.engine.HanjaConverter
import io.github.lee0701.converter.library.engine.Predictor
import io.github.lee0701.converter.library.engine.ResortingPredictor
import io.github.lee0701.converter.library.engine.TFLitePredictor

object HanjaConverterBuilder {
    private const val commonPackageName = "io.github.lee0701.converter"
    private const val donationPackageName = "io.github.lee0701.converter.donation"

    fun build(context: Context): Pair<HanjaConverter?, Predictor?> {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        val list = listOf(commonPackageName, donationPackageName)
        val converterContext = list.map { name -> kotlin.runCatching {
            context.createPackageContext(name, 0)
        } }.filter { it.isSuccess }.firstOrNull()?.getOrNull() ?: return null to null
        println(converterContext)

        val converters: MutableList<HanjaConverter> = mutableListOf()
        val isDonation = converterContext.packageName == donationPackageName
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
        val dictionaries =
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

        val predictor = if(tfLitePredictor != null) {
            ResortingPredictor(
                DictionaryPredictor(dictionaries),
                CachingTFLitePredictor(tfLitePredictor),
            )
        } else null

        return hanjaConverter to predictor
    }

}