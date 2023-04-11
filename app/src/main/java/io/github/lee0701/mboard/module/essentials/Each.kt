package io.github.lee0701.mboard.module.essentials

class Each<In, Out>(
    private val inputModule: InputModule<In, Out>,
): InputModule<List<In>, List<Out>> {
    override fun process(input: List<In>): List<Out> {
        return input.map { inputModule.process(it) }
    }
}