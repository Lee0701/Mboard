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
        run {
            val inputs = listOf(0x3142, 0x315c, 0x3154, 0x3139, 0x3131)
            var state = CombineHangul.State()
            state = combiner.process(state to inputs[0]).last()
            state = combiner.process(state to inputs[1]).last()
            state = combiner.process(state to inputs[2]).last()
            state = combiner.process(state to inputs[3]).last()
            state = combiner.process(state to inputs[4]).last()
            assert(state.text == "뷁")
        }
        run {
            val inputs = listOf(0x3147, 0x314f, 0x3143, 0x314f)
            val intermediates = listOf("ㅇ", "아", "아ㅃ", "아빠")
            var state = CombineHangul.State()
            for(i in inputs.indices) {
                state = combiner.process(state to inputs[i]).last()
                assert(state.text == intermediates[i])
            }
            assert(state.text == "아빠")
        }
    }

    @Test
    fun testBatchProcessing() {
        val combiner = CombineHangul(mapOf())
        val batchCombiner = CombineHangul.Batch(combiner)
        val inputs = listOf(0x110b, 0x1161, 0x1107, 0x1165, 0x110c, 0x1175, 0x1100, 0x1161, 0x1107, 0x1161, 0x11bc, 0x110b, 0x1166, 0x1103, 0x1173, 0x11af, 0x110b, 0x1165, 0x1100, 0x1161, 0x1109, 0x1175, 0x11ab, 0x1103, 0x1161)
        val result = batchCombiner.process(CombineHangul.State() to inputs)

        assert(result.text == "아버지가방에들어가신다")
    }

}