package it.fra.myunical.today.models

sealed interface TuitionState {
    data object Loading : TuitionState
    data object Unavailable : TuitionState
    data object UpToDate : TuitionState

    data class Due(
        val amount: String,
        /** e.g. "Scade il 30/11", "Scaduta il 30/09"; null when no due date is known. */
        val dueLabel: String?,
        val pendingCount: Int,
        val isOverdue: Boolean
    ) : TuitionState
}
