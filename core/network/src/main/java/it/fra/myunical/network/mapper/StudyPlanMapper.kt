package it.fra.myunical.network.mapper

import it.fra.myunical.domain.model.general.Course
import it.fra.myunical.domain.model.general.ExamOutcome
import it.fra.myunical.domain.model.general.ExamResult
import it.fra.myunical.network.models.cineca.ExamOutcomeDto
import it.fra.myunical.network.models.cineca.StudyPlanItemDto
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun StudyPlanItemDto.toDomainModel(): Course {
    return Course(
        id = this.activityId,
        code = this.courseCode,
        name = this.courseName,
        year = this.courseYear,
        credits = this.credits.toInt(),
        attendanceAcademicYear = this.attendanceAcademicYear,
        isPassed = this.status.value == "S", // Mappatura diretta a booleano
        examType = this.examTypeDescription,
        courseType = this.courseTypeDescription,
        outcome = this.outcome?.toDomainModel()
    )
}

fun ExamOutcomeDto.toDomainModel(): ExamOutcome {
    val result = when (this.evaluationMode?.value) {
        "V" -> {
            if (this.grade != null) {
                ExamResult.Graded(
                    grade = this.grade.toInt(),
                    hasHonors = this.honorsFlag == 1
                )
            } else {
                ExamResult.NotEvaluated
            }
        }
        "G" -> {
            if (this.judgmentCode != null && this.judgmentDescription != null) {
                ExamResult.Judgment(
                    code = this.judgmentCode,
                    description = this.judgmentDescription
                )
            } else {
                ExamResult.NotEvaluated
            }
        }
        else -> ExamResult.NotEvaluated
    }

    return ExamOutcome(
        date = this.examDate?.takeUnless(String::isBlank)?.toEpochMillis() ?: 0,
        academicYear = this.passedAcademicYear,
        result = result
    )
}

private fun String.toEpochMillis(zoneId: ZoneId = ZoneId.systemDefault()): Long {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
    return LocalDateTime.parse(this, formatter)
        .atZone(zoneId)
        .toInstant()
        .toEpochMilli()
}