package io.github.lee0701.mboard.module.input

class NullConvert<T>(
    private val defValue: () -> T
): InputModule<T?, T> {
    override fun process(input: T?): T {
        return input ?: defValue()
    }
}