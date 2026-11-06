package it.fra.myunical.domain.model.general

data class Course(
    val id: Long,
    val code: String,
    val name: String,
    val year: Int,
    val credits: Int,
    val attendanceAcademicYear: Int,
    val isPassed: Boolean,
    val examType: String,
    val courseType: String,
    val outcome: ExamOutcome?
)

data class ExamOutcome(
    val date: Long,
    val academicYear: Int?,
    val result: ExamResult
)

sealed class ExamResult {
    data class Graded(
        val grade: Int, 
        val hasHonors: Boolean
    ) : ExamResult()

    data class Judgment(
        val code: String, 
        val description: String
    ) : ExamResult()
    
    data object NotEvaluated : ExamResult()
}