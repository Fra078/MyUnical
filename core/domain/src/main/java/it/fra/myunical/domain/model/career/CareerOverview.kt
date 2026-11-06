package it.fra.myunical.domain.model.career

/** Progress and official averages of a career, as computed by Esse3 (libretto stats and averages). */
data class CareerOverview(
    val requiredCredits: Int,
    val earnedCredits: Int,
    val passedCount: Int,
    val totalCount: Int,
    val weightedAverage: Double?,
    val arithmeticAverage: Double?,
    /** Weighted average expressed on the graduation base (110). */
    val graduationBase: Double?
) {
    val progress: Float
        get() = if (requiredCredits > 0) (earnedCredits.toFloat() / requiredCredits).coerceIn(0f, 1f) else 0f
}
