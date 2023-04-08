package io.github.lee0701.mboard.module.input

import io.github.lee0701.mboard.module.input.essentials.InputModule

object Output {
    class Commit(
        private val listener: Listener,
    ): InputModule<CharSequence, CharSequence> {
        override fun process(input: CharSequence): CharSequence {
            listener.onCommit(input)
            return input
        }
    }

    interface Listener {
        fun onCommit(text: CharSequence)
    }
}