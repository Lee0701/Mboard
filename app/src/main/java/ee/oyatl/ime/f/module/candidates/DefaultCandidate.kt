package ee.oyatl.ime.f.module.candidates

data class DefaultCandidate(
    override val text: String,
    override val score: Float = 0f,
): Candidate