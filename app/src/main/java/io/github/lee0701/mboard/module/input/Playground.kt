package io.github.lee0701.mboard.module.input

import io.github.lee0701.mboard.module.essentials.DropNull
import io.github.lee0701.mboard.module.essentials.Each
import io.github.lee0701.mboard.module.essentials.Pick
import io.github.lee0701.mboard.module.essentials.Transform

fun main() {
    val table = mapOf(
        0 to ' '.code,
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
    val listener = object: Output.Listener {
        override fun onCommit(text: CharSequence) {
            println("commit: $text")
        }
    }
    val output = Each(CodeConvert(table)) +
            DropNull() +
            Transform { CombineHangul.State() to it } +
            CombineHangul.Batch(CombineHangul()) +
            Transform { it.text } +
            Output.Commit(listener)

    val processor = ComposeWord() +
            Each(CodeConvert(table)) +
            Split(setOf(0x20)) +
            Pick(1) +
            DropNull() +
            Transform { CombineHangul.State() to it } +
            CombineHangul.Batch(CombineHangul()) +
            Transform { it.text }

    val inputs = listOf(1, 6, 2, 2, 8, 2, 3, 9, 0, 3, 8, 3, 9)

    var result: CharSequence
    for(code in inputs) {
        result = processor.process(code)
        println(result)
    }

}