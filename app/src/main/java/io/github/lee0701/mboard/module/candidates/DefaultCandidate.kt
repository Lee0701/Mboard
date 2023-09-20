package io.github.lee0701.mboard.module.candidates

data class DefaultCandidate(
    override val text: String,
    override val score: Float = 0f,
): Candidate