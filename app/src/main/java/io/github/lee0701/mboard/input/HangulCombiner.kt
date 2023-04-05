package io.github.lee0701.mboard.input

import io.github.lee0701.mboard.charset.Hangul

class HangulCombiner(
    val jamoCombinationMap: Map<Pair<Int, Int>, Int> = mapOf(),
) {
    fun combine(state: State, input: Int): Pair<CharSequence, State> {
        var newState = state
        var composed = ""
        if(Hangul.isCho(input)) {
            if(newState.cho != null) {
                val combination = jamoCombinationMap[newState.cho to input]
                if(combination != null) {
                    if(state.last != null && !Hangul.isCho(state.last)) {
                        composed += newState.composed
                        newState = State(cho = input)
                    } else {
                        newState = newState.copy(cho = combination)
                    }
                } else {
                    composed += newState.composed
                    newState = State(cho = input)
                }
            } else newState = newState.copy(cho = input)
        } else if(Hangul.isJung(input)) {
            if(newState.jung != null) {
                val combination = jamoCombinationMap[newState.jung to input]
                if(combination != null) newState = newState.copy(jung = combination)
                else {
                    composed += newState.composed
                    newState = State(jung = input)
                }
            } else newState = newState.copy(jung = input)
        } else if(Hangul.isJong(input)) {
            if(newState.jong != null) {
                val combination = jamoCombinationMap[newState.jong to input]
                if(combination != null) newState = newState.copy(jong = combination)
                else {
                    composed += newState.composed
                    newState = State(jong = input)
                }
            } else newState = newState.copy(jong = input)
        } else {
            composed += newState.composed
            composed += input.toChar()
            newState = State()
        }
        return composed to newState.copy(last = input)
    }

    data class State(
        val cho: Int? = null,
        val jung: Int? = null,
        val jong: Int? = null,
        val last: Int? = null,
    ) {
        val choChar: Char? = cho?.and(0xffff)?.toChar()
        val jungChar: Char? = jung?.and(0xffff)?.toChar()
        val jongChar: Char? = jong?.and(0xffff)?.toChar()

        val ordinalCho: Int? = cho?.and(0xffff)?.minus(0x1100)
        val ordinalJung: Int? = jung?.and(0xffff)?.minus(0x1161)
        val ordinalJong: Int? = jong?.and(0xffff)?.minus(0x11a7)

        val nfc: Char? =
            if(ordinalCho != null && ordinalJung != null) Hangul.combineNFC(ordinalCho, ordinalJung, ordinalJong)
            else null
        val nfd: CharSequence =
            Hangul.combineNFD(choChar, jungChar, jongChar)

        val composed: CharSequence =
            if(cho == null && jung == null && jong == null) ""
            else if(listOfNotNull(cho, jung, jong).size == 1)
                (choChar?.let { Hangul.choToCompatConsonant(it) } ?:
                jungChar?.let { Hangul.jungToCompatVowel(it) } ?:
                jongChar?.let { Hangul.jongToCompatConsonant(it) })?.toString().orEmpty()
            else
                nfc?.toString() ?: nfd
    }
}