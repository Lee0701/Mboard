package io.github.lee0701.mboard

import io.github.lee0701.mboard.module.input.CombineHangul
import org.junit.Test

class HangulCombinerTest {

    @Test
    fun testJamoCombination() {
        val combiner = CombineHangul(mapOf(
            0x11af to 0x11a8 to 0x11b0,
            0x116e to 0x1166 to 0x1170,
        ))
        val input = listOf(0x3142, 0x315c, 0x3154, 0x3139, 0x3131)
        var result = CombineHangul.State()
        result = combiner.process(result to input[0]).last()
        result = combiner.process(result to input[1]).last()
        result = combiner.process(result to input[2]).last()
        result = combiner.process(result to input[3]).last()
        result = combiner.process(result to input[4]).last()
        assert(result.text == "뷁")
    }

    @Test
    fun testBatchProcessing() {
        val combiner = CombineHangul(mapOf())
        val inputs = listOf(0x110b, 0x1161, 0x1107, 0x1165, 0x110c, 0x1175, 0x1100, 0x1161, 0x1107, 0x1161, 0x11bc, 0x110b, 0x1166, 0x1103, 0x1173, 0x11af, 0x110b, 0x1165, 0x1100, 0x1161, 0x1109, 0x1175, 0x11ab, 0x1103, 0x1161)
        val result = inputs.fold(listOf( CombineHangul.State())) { acc, i -> combiner.process(acc.last() to i) }

        assert(result.last().text == "아버지가방에들어가신다")
    }

}