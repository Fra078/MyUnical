package it.fra.myunical.domain.model.degreeexplorer

/** The didactic regulation (PDF) of a degree course as published for [academicYear]. */
data class Regulation(val academicYear: Int, val url: String)

/** Regulations of the student's degree course; each is null when the university has not published it. */
data class DegreeRegulations(
    /** The one the student enrolled under. */
    val cohort: Regulation?,
    /** The most recent one. */
    val latest: Regulation?
) {
    /** True when there is a single document to show (same regulation for the cohort and the latest year). */
    val isSingle: Boolean get() = cohort != null && cohort.url == latest?.url
}
