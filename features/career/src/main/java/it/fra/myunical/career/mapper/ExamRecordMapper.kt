package it.fra.myunical.career.mapper

import it.fra.myunical.career.models.ExamRecord
import it.fra.myunical.domain.model.general.Course
import it.fra.myunical.domain.model.general.ExamResult
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


fun Course.mapToUiModel() : ExamRecord {

    val grade = when(val result = outcome?.result){
        is ExamResult.Graded -> result.grade.toString() + if (result.hasHonors) "L" else ""
        is ExamResult.Judgment -> "A"
        else -> null
    }

    return ExamRecord(
        id = id.toString(),
        title = name,
        scientificSector = "",
        credits = credits,
        grade = grade,
        isPassed = isPassed,
        date = outcome?.date?.let {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            Instant.ofEpochMilli(it)
                .atZone(ZoneId.systemDefault())
                .format(formatter)
        },
        academicYear = attendanceAcademicYear.toString()
    )
}