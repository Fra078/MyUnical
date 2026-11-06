package it.fra.myunical.career.models

data class ExamRecord(
    val id: Long,
    val title: String,
    val credits: Int,
    /** "Scritto", "Orale"... */
    val examType: String?,
    /** "28", "30L", "Idoneo"; null when not passed yet. */
    val grade: String?,
    val hasHonors: Boolean,
    val isPassed: Boolean,
    val passedOn: String?,
    /** Upcoming sessions open for booking. */
    val openSessions: Int
)
