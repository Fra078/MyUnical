package it.fra.myunical.career.mapper

import it.fra.myunical.career.models.CareerOption
import it.fra.myunical.career.models.ExamFilter
import it.fra.myunical.career.models.ExamGroup
import it.fra.myunical.career.models.ExamRecord
import it.fra.myunical.career.models.ExamSortOption
import it.fra.myunical.career.models.OverviewUiModel
import it.fra.myunical.career.models.SessionUiModel
import it.fra.myunical.domain.model.career.BookingState
import it.fra.myunical.domain.model.career.CareerData
import it.fra.myunical.domain.model.career.CareerOverview
import it.fra.myunical.domain.model.career.ExamSession
import it.fra.myunical.domain.model.general.CareerTrackModel
import it.fra.myunical.domain.model.general.statusLabel
import it.fra.myunical.domain.model.general.Course
import it.fra.myunical.domain.model.general.ExamResult
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val ItalianLocale: Locale = Locale.ITALIAN
private val DateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val ShortDateFormatter = DateTimeFormatter.ofPattern("dd/MM")
private val TimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun Double?.formatAverage(): String = this?.let { String.format(Locale.ITALY, "%.2f", it) } ?: "—"

fun CareerOverview.toUiModel() = OverviewUiModel(
    earnedCredits = earnedCredits,
    requiredCredits = requiredCredits,
    progress = progress,
    weightedAverage = weightedAverage.formatAverage(),
    arithmeticAverage = arithmeticAverage.formatAverage(),
    graduationBase = graduationBase.formatAverage(),
    passedLabel = "$passedCount di $totalCount esami superati"
)

fun CareerTrackModel.toOption(selectedMatId: Int?) = CareerOption(
    matId = matId,
    name = name.toTitleCase(),
    status = statusLabel,
    matricola = matricola,
    isSelected = matId == selectedMatId
)

fun CareerData.toExamGroups(filter: ExamFilter, sort: ExamSortOption, query: String): List<ExamGroup> {
    val openByRow = openSessions.groupingBy(ExamSession::courseRowId).eachCount()
    val visible = courses
        .filter {
            when (filter) {
                ExamFilter.ALL -> true
                ExamFilter.PASSED -> it.isPassed
                ExamFilter.PENDING -> !it.isPassed
            }
        }
        .filter { query.isBlank() || it.name.contains(query.trim(), ignoreCase = true) }

    fun List<Course>.toRecords() = map { it.toExamRecord(openByRow[it.id] ?: 0) }

    return when (sort) {
        ExamSortOption.COURSE_YEAR -> visible
            .sortedWith(compareBy(Course::year, Course::name))
            .groupBy(Course::year)
            .map { (year, courses) -> ExamGroup("$year° anno", courses.toRecords()) }
        ExamSortOption.DATE_DESC -> listOf(
            ExamGroup(null, visible.sortedByDescending { if (it.isPassed) it.outcome?.date ?: 0L else Long.MIN_VALUE }.toRecords())
        )
        ExamSortOption.GRADE_DESC -> listOf(ExamGroup(null, visible.sortedByDescending { it.gradeScore() }.toRecords()))
        ExamSortOption.ALPHABETICAL -> listOf(ExamGroup(null, visible.sortedBy(Course::name).toRecords()))
    }.filter { it.exams.isNotEmpty() }
}

private fun Course.gradeScore(): Int = if (!isPassed) -10 else when (val result = outcome?.result) {
    is ExamResult.Graded -> result.grade * 2 + if (result.hasHonors) 1 else 0
    is ExamResult.Judgment -> 0
    else -> -1
}

private fun Course.toExamRecord(openSessions: Int): ExamRecord {
    val result = outcome?.result.takeIf { isPassed }
    return ExamRecord(
        id = id,
        title = name,
        credits = credits,
        examType = examType.takeIf { it.isNotBlank() }?.capitalized(),
        grade = when (result) {
            is ExamResult.Graded -> result.grade.toString() + if (result.hasHonors) "L" else ""
            is ExamResult.Judgment -> result.description.takeIf { it.isNotBlank() }?.capitalized() ?: "Idoneo"
            else -> null
        },
        hasHonors = (result as? ExamResult.Graded)?.hasHonors == true,
        isPassed = isPassed,
        passedOn = outcome?.date?.takeIf { isPassed && it > 0 }?.let {
            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).format(DateFormatter)
        },
        openSessions = if (isPassed) 0 else openSessions
    )
}

fun ExamSession.toUiModel(today: LocalDate): SessionUiModel {
    val state = bookingState(today)
    return SessionUiModel(
        id = "$courseRowId-$id",
        courseName = courseName,
        description = description,
        day = date.dayOfMonth.toString(),
        month = date.month.getDisplayName(TextStyle.SHORT, ItalianLocale),
        weekday = date.dayOfWeek.getDisplayName(TextStyle.SHORT, ItalianLocale),
        time = time?.format(TimeFormatter),
        president = president,
        state = state,
        stateLabel = when (state) {
            BookingState.BOOKED -> "Prenotato"
            BookingState.OPEN -> enrollEnd?.let { "Prenotazioni aperte fino al ${it.format(ShortDateFormatter)}" }
                ?: "Prenotazioni aperte"
            BookingState.NOT_YET_OPEN -> enrollStart?.let { "Prenotazioni dal ${it.format(ShortDateFormatter)}" }
                ?: "Prenotazioni non ancora aperte"
            BookingState.CLOSED -> "Prenotazioni chiuse"
        },
        bookingDetail = booking?.let { b ->
            listOfNotNull(
                b.position?.let { "Posizione $it" },
                listOfNotNull(b.room, b.building).joinToString(", ").takeIf { it.isNotEmpty() }
            ).joinToString(" · ").takeIf { it.isNotEmpty() }
        }
    )
}

private fun String.capitalized(): String =
    lowercase(ItalianLocale).replaceFirstChar { it.titlecase(ItalianLocale) }

private fun String.toTitleCase(): String =
    if (any { it.isLowerCase() }) this
    else lowercase(ItalianLocale).split(' ').joinToString(" ") { w -> w.replaceFirstChar { it.titlecase(ItalianLocale) } }
