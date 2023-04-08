package io.github.lee0701.mboard.module.input

import io.github.lee0701.mboard.charset.Hangul

class CombineHangul(
    val jamoCombinationTable: Map<Pair<Int, Int>, Int> = mapOf(),
): InputModule<Pair<CombineHangul.State, Int>, List<CombineHangul.State>> {

    override fun process(stateAndInput: Pair<State, Int>): List<State> {
        val (state, input) = stateAndInput
        val newStates = mutableListOf<State>()
        var composed = ""
        if(Hangul.isCho(input and 0x1fffff)) {
            if(state.cho != null) {
                val combination = jamoCombinationTable[state.cho to input]
                if(combination != null) {
                    if(state.last != null && !Hangul.isCho(state.last)) {
                        composed += state.composed
                        newStates += State(cho = input)
                    } else {
                        newStates += state.copy(cho = combination)
                    }
                } else {
                    composed += state.composed
                    newStates += State(cho = input)
                }
            } else newStates += state.copy(cho = input)
        } else if(Hangul.isJung(input and 0x1fffff)) {
            if(state.jung != null) {
                val combination = jamoCombinationTable[state.jung to input]
                if(combination != null) newStates += state.copy(jung = combination)
                else {
                    composed += state.composed
                    newStates += State(jung = input)
                }
            } else newStates += state.copy(jung = input)
        } else if(Hangul.isJong(input and 0x1fffff)) {
            val newStateJong = state.jong
            if(newStateJong != null) {
                val combination = jamoCombinationTable[newStateJong to input]
                if(combination != null) newStates += state.copy(jong = combination, lastCombination = newStateJong to input)
                else {
                    composed += state.composed
                    newStates += State(jong = input)
                }
            } else newStates += state.copy(jong = input)
        } else if(Hangul.isConsonant(input and 0x1fffff)) {
            val cho = Hangul.consonantToCho(input and 0xffff)
            val jong = Hangul.consonantToJong(input and 0xffff)
            if(state.cho != null && state.jung != null) {
                if(state.jong != null) {
                    val combination = jamoCombinationTable[state.jong to jong]
                    if(combination != null) newStates += state.copy(jong = combination, lastCombination = state.jong to jong)
                    else {
                        composed += state.composed
                        newStates += State(cho = cho)
                    }
                } else {
                    newStates += state.copy(jong = jong)
                }
            } else if(state.cho != null) {
                if(state.last != null && !Hangul.isConsonant(state.last)) {
                    composed += state.composed
                    newStates += State(cho = cho)
                } else {
                    val combination = jamoCombinationTable[state.cho to cho]
                    if(combination != null) newStates += state.copy(cho = combination)
                    else {
                        composed += state.composed
                        newStates += State(cho = cho)
                    }
                }
            } else {
                newStates += state.copy(cho = cho)
            }
        } else if(Hangul.isVowel(input and 0x1fffff)) {
            val jung = Hangul.vowelToJung(input and 0xffff)
            val newStateJong = state.jong
            val jongCombination = state.lastCombination
            if(newStateJong != null) {
                if(jongCombination != null) {
                    val promotedCho = Hangul.ghostLight(jongCombination.second)
                    composed += state.copy(jong = jongCombination.first).composed
                    newStates += State(cho = promotedCho)
                    newStates += State(cho = promotedCho, jung = jung)
                } else {
                    val promotedCho = Hangul.ghostLight(newStateJong)
                    composed += state.copy(jong = null).composed
                    newStates += State(cho = promotedCho)
                    newStates += State(cho = promotedCho, jung = jung)
                }
            } else if(state.jung != null) {
                val combination = jamoCombinationTable[state.jung to jung]
                if(combination != null) newStates += state.copy(jung = combination)
                else {
                    composed += state.composed
                    newStates += State(jung = jung)
                }
            } else {
                newStates += state.copy(jung = jung)
            }
        } else {
            composed += state.composed
            composed += input.toChar()
        }
        return newStates.map { it.copy(last = input, committed = "${state.committed}$composed") }
    }

    data class State(
        val cho: Int? = null,
        val jung: Int? = null,
        val jong: Int? = null,
        val last: Int? = null,
        val lastCombination: Pair<Int, Int>? = null,
        val committed: CharSequence = "",
    ) {
        val choChar: Char? = cho?.and(0xffff)?.toChar()
        val jungChar: Char? = jung?.and(0xffff)?.toChar()
        val jongChar: Char? = jong?.and(0xffff)?.toChar()

        private val ordinalCho: Int? = cho?.and(0xffff)?.minus(0x1100)
        private val ordinalJung: Int? = jung?.and(0xffff)?.minus(0x1161)
        private val ordinalJong: Int? = jong?.and(0xffff)?.minus(0x11a7)

        val nfc: Char? =
            if(ordinalCho != null && ordinalJung != null && listOfNotNull(cho, jung, jong).all { Hangul.isModernJamo(it) })
                Hangul.combineNFC(ordinalCho, ordinalJung, ordinalJong)
            else null
        val nfd: CharSequence =
            Hangul.combineNFD(choChar, jungChar, jongChar)

        val composed: CharSequence =
            if(cho == null && jung == null && jong == null) ""
            else if(listOfNotNull(cho, jung, jong).let { it.size == 1 && it.all { c -> Hangul.isModernJamo(c) } })
                (choChar?.let { Hangul.choToCompatConsonant(it) } ?:
                jungChar?.let { Hangul.jungToCompatVowel(it) } ?:
                jongChar?.let { Hangul.jongToCompatConsonant(it) })?.toString().orEmpty()
            else
                nfc?.toString() ?: nfd

        val text: CharSequence = "$committed$composed"
    }
}