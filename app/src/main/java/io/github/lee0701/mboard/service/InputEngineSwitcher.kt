package io.github.lee0701.mboard.service

import android.content.Context
import android.view.View
import io.github.lee0701.mboard.input.InputEngine
import io.github.lee0701.mboard.input.SoftInputEngine

class InputEngineSwitcher(
    private val engines: List<InputEngine>,
    private val table: Array<IntArray>,
) {
    private var languageIndex = 0
    private var extraIndex = 0

    fun initView(context: Context): View? {
        val currentEngine = getCurrentEngine()
        return if(currentEngine is SoftInputEngine) currentEngine.initView(context)
        else null
    }

    fun updateView() {
        val currentEngine = getCurrentEngine()
        if(currentEngine is SoftInputEngine) currentEngine.updateView()
    }

    fun getCurrentEngine(): InputEngine {
        return engines[table[languageIndex][extraIndex]]
    }

    fun nextLanguage() {
        languageIndex += 1
        if(languageIndex >= table.size) languageIndex = 0
        extraIndex = 0
    }

    fun nextExtra() {
        extraIndex += 1
        if(extraIndex >= table[languageIndex].size) extraIndex = 0
    }
}