package io.github.lee0701.mboard.module.essentials

class DropNull<T>: InputModule<List<T?>, List<T>> {
    override fun process(input: List<T?>): List<T> {
        return input.filterNotNull()
    }
}