package ee.oyatl.ime.f.service

import android.content.Context
import android.view.View
import ee.oyatl.ime.f.module.candidates.Candidate
import ee.oyatl.ime.f.module.component.CandidatesComponent
import ee.oyatl.ime.f.module.inputengine.InputEngine

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
        getCurrentEngine().components.forEach { it.updateView() }
    }

    fun getCurrentEngine(): InputEngine {
        return engines[table[languageIndex][extraIndex]]
    }

    fun showCandidates(list: List<Candidate>) {
        getCurrentEngine().components.filterIsInstance<CandidatesComponent>()
            .forEach { it.showCandidates(list) }
    }

    fun nextLanguage() {
        getCurrentEngine().components.filterIsInstance<CandidatesComponent>()
            .forEach { it.showCandidates(listOf()) }
        languageIndex += 1
        if(languageIndex >= table.size) languageIndex = 0
        extraIndex = 0
    }

    fun nextExtra() {
        extraIndex += 1
        if(extraIndex >= table[languageIndex].size) extraIndex = 0
    }
}