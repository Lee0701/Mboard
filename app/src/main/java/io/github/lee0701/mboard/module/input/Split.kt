package io.github.lee0701.mboard.module.input

import io.github.lee0701.mboard.module.essentials.InputModule

class Split<T>(
    private val delimiters: Set<T> = setOf(),
): InputModule<List<T>, List<List<T>>> {
    override fun process(input: List<T>): List<List<T>> {
        val indices = input.mapIndexed { i, v -> if(v in delimiters) i else null }.filterNotNull()
        return indices.mapIndexed { i, index ->
            if(i == 0) input.subList(0, index)
            else input.subList(indices[i-1], index)
        }
    }
}