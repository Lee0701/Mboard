package io.github.lee0701.mboard.module.input

fun main() {
    val table = mapOf(
        1 to 'ㅁ'.code,
        2 to 'ㄴ'.code,
        3 to 'ㅇ'.code,
        4 to 'ㄹ'.code,
        5 to 'ㅎ'.code,
        6 to 'ㅗ'.code,
        7 to 'ㅓ'.code,
        8 to 'ㅏ'.code,
        9 to 'ㅣ'.code,
    )
    val processor = ComposeWord() +
            Each(CodeConvert(table)) +
            DropNull() +
            Transform { CombineHangul.State() to it } +
            CombineHangul.Batch(CombineHangul()) +
            Transform { it.text }

    var result = processor.process(1)
    println(result)
    result = processor.process(6)
    println(result)
    result = processor.process(2)
    println(result)
    result = processor.process(2)
    println(result)
    result = processor.process(8)
    println(result)
    result = processor.process(2)
    println(result)
    result = processor.process(3)
    println(result)
    result = processor.process(9)
    println(result)
}