package it.fra.myunical.today.models

import androidx.compose.runtime.Immutable

@Immutable
data class UpcomingCommitment(
    val title: String,
    val type: CommitmentType,
    val timeSlot: String,
    val room: String?,
    /** Human readable distance from now, e.g. "Tra 35 min" or "Domani". */
    val relativeTime: String,
    val teachers: String? = null,
    val scheduleName: String? = null,
    /** Elapsed fraction of an ongoing commitment, `null` if it has not started yet. */
    val progress: Float? = null,
    /** Short description of what comes right after, e.g. "Poi alle 11:30 · Analisi". */
    val followedBy: String? = null
) {
    val isOngoing: Boolean get() = progress != null
}

enum class CommitmentType {
    LESSON,
    EXAM
}

sealed interface UpcomingCommitmentState {
    data object Loading : UpcomingCommitmentState
    data object NotConfigured : UpcomingCommitmentState
    data object NothingScheduled : UpcomingCommitmentState
    data object Unavailable : UpcomingCommitmentState
    data class Available(val commitment: UpcomingCommitment) : UpcomingCommitmentState
}
