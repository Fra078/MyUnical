package it.fra.myunical.today.models


data class UpcomingCommitment(
    val title: String,
    val type: CommitmentType,
    val timeSlot: String,
    val room: String,
    val startsInMinutes: Int? = null
)
enum class CommitmentType {
    LESSON,
    EXAM
}