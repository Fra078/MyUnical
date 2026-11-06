package it.fra.myunical.today.models

sealed interface CareerSnapshotState {
    data object Loading : CareerSnapshotState
    data object Unavailable : CareerSnapshotState

    data class Available(
        val earnedCredits: Int,
        val requiredCredits: Int,
        val progress: Float,
        /** Formatted weighted average, null when no graded exam yet. */
        val weightedAverage: String?,
        val openSessions: Int
    ) : CareerSnapshotState
}
