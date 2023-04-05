package io.github.lee0701.mboard.charset

object Hangul {
    const val CONVERT_CHO = "ᄀᄁ ᄂ  ᄃᄄᄅ       ᄆᄇᄈ ᄉᄊᄋᄌᄍᄎᄏᄐᄑᄒ"
    const val CONVERT_JONG = "ᆨᆩᆪᆫᆬᆭᆮ ᆯᆰᆱᆲᆳᆴᆵᆶᆷᆸ ᆹᆺᆻᆼᆽ ᆾᆿᇀᇁᇂ"

    fun isCho(code: Int) = code in 0x1100..0x115f || code in 0xa960..0xa97c
    fun isJung(code: Int) = code in 0x1160..0x11a7 || code in 0xd7b0..0xd7ca
    fun isJong(code: Int) = code in 0x11a8..0x11ff || code in 0xd7cb..0xd7ff
    fun isModernJamo(code: Int) = code in 0x1100 ..0x1112 || code in 0x1161..0x1175 || code in 0x11a8..0x11c2

    fun isConsonant(code: Int) = code in 0x3131..0x314e || code in 0x3165..0x3186
    fun isVowel(code: Int) = code in 0x314f..0x3163 || code in 0x3187..0x318e
    fun isModernCompatJamo(code: Int) = code in 0x3131..0x314e || code in 0x314f..0x3163

    fun choToCompatConsonant(char: Char): Char = (CONVERT_CHO.indexOf(char) + 0x3131).toChar()
    fun jungToCompatVowel(char: Char): Char = (char - 0x1161 + 0x314f)
    fun jongToCompatConsonant(char: Char): Char = (CONVERT_JONG.indexOf(char) + 0x3131).toChar()

    fun consonantToCho(char: Char): Char = CONVERT_CHO[(char - 0x3131).code]
    fun vowelToJung(char: Char): Char = (char - 0x3131 + 0x1161)
    fun consonantToJong(char: Char): Char = CONVERT_JONG[(char - 0x3131).code]

    fun combineNFC(cho: Int, jung: Int, jong: Int?): Char {
        return (0xac00 + 21*28*cho + 28*jung + (jong?:0)).toChar()
    }
    fun combineNFD(cho: Char?, jung: Char?, jong: Char?): CharSequence {
        return "${(cho ?: 0x115f.toChar())}${(jung ?: 0x1160.toChar())}${jong?.toString().orEmpty()}"
    }
}