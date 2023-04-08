package io.github.lee0701.mboard.input

data class DefaultCandidate(
    override val text: String,
    override val score: Float = 0f,
): Candidate