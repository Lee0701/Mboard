package io.github.lee0701.mboard.service

import android.content.Context
import android.view.View
import io.github.lee0701.mboard.module.candidates.Candidate
import io.github.lee0701.mboard.module.component.CandidatesComponent
import io.github.lee0701.mboard.module.inputengine.InputEngine

class InputEngineSwitcher(
    private val engines: List<InputEngine>,
    private val table: Array<IntArray>,
) {
    private var languageIndex = 0
    private var extraIndex = 0

    fun initView(context: Context): View? {
        val currentEngine = getCurrentEngine()
        val view = currentEngine.initView(context)
        currentEngine.components.forEach { it.reset() }
        return view
    }

    fun updateView() {
        val currentEngine = getCurrentEngine()
        currentEngine.components.forEach { it.updateView() }
    }

    fun getCurrentEngine(): InputEngine {
        return engines[table[languageIndex][extraIndex]]
    }

    fun showCandidates(list: List<Candidate>) {
        val currentEngine = getCurrentEngine()
        currentEngine.components.filterIsInstance<CandidatesComponent>()
            .forEach { it.showCandidates(list) }
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