package io.github.lee0701.mboard.service

import io.github.lee0701.mboard.input.InputEngine

class InputEngineSwitcher(
    private val engines: List<InputEngine>,
    private val table: Array<IntArray>,
) {
    private var languageIndex = 0
    private var extraIndex = 0

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