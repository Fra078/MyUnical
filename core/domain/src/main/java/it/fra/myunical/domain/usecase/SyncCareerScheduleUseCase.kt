package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.model.degreeexplorer.DegreeProgram
import it.fra.myunical.domain.model.degreeexplorer.StudyPath
import it.fra.myunical.domain.model.general.Course
import it.fra.myunical.domain.model.schedule.AcademicYear
import it.fra.myunical.domain.model.schedule.ScheduleProvider
import it.fra.myunical.domain.model.schedule.TrackedCourse
import it.fra.myunical.domain.model.syllabus.cohortYear
import it.fra.myunical.domain.repository.ScheduleProviderRepository
import it.fra.myunical.domain.sources.UnicalStorageDataSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import java.util.Locale

/**
 * Keeps one schedule in sync with the current career: the student's degree course, curriculum and
 * the exams still to pass in the libretto. Schedules the user edited by hand or removed are left alone.
 */
class SyncCareerScheduleUseCase(
    private val getCareerData: GetCareerDataUseCase,
    private val resolveCdsCode: ResolveCdsCodeUseCase,
    private val builder: CareerScheduleBuilder,
    private val providerRepository: ScheduleProviderRepository
) {
    /** Suspends for as long as the career is observed; failures are ignored and retried on the next emission. */
    suspend operator fun invoke() {
        getCareerData().collectLatest { result ->
            val data = result.getOrNull() ?: return@collectLatest
            val matId = data.career.matId
            val id = scheduleId(matId)
            val existing = providerRepository.getProvider(id)
            if (existing != null && existing.careerMatId == null) return@collectLatest
            // The student already follows this degree course by hand: a second schedule would repeat every lesson
            val manual = providerRepository.getProviders().first()
                .any { it.careerMatId == null && it.id != id && it.cdsCode == data.career.cdsCode }
            if (manual) {
                if (existing != null) providerRepository.removeProvider(id)
                return@collectLatest
            }
            if (existing == null && providerRepository.isCareerScheduleDismissed(matId)) return@collectLatest
            val cdsCode = resolveCdsCode(data.career) ?: return@collectLatest
            val cohort = data.courses.cohortYear() ?: return@collectLatest
            val provider = try {
                builder.build(id, matId, cdsCode, cohort, data.courses)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            } ?: return@collectLatest
            if (provider != existing) providerRepository.upsertProvider(provider)
        }
    }

    companion object {
        fun scheduleId(matId: Int) = "career-$matId"
    }
}

class CareerScheduleBuilder(
    private val storageDataSource: UnicalStorageDataSource,
    private val currentYear: () -> Int = AcademicYear::current
) {
    /** Null when the course, the curriculum or any course still to pass cannot be found. */
    suspend fun build(id: String, matId: Int, cdsCode: String, cohort: Int, libretto: List<Course>): ScheduleProvider? {
        val program = findProgram(cdsCode, cohort) ?: return null
        val paths = storageDataSource.getStudyPlansForCds(program.regulationId)
        val toPass = libretto.filterNot(Course::isPassed).map(Course::code).toSet()
        val path = paths.maxByOrNull { it.matches(toPass) }?.takeIf { it.matches(toPass) > 0 } ?: return null
        val courses = path.academicYears.flatMap { year ->
            year.courses
                .filter { it.code in toPass || it.moduleCodes.any(toPass::contains) }
                .map { TrackedCourse(it.code, it.name, year.yearNumber, it.moduleCodes) }
        }.distinctBy(TrackedCourse::code)
        return ScheduleProvider(
            id = id,
            displayName = program.name.toDisplayName(),
            cdsCode = cdsCode,
            regulationId = program.regulationId,
            degreeName = program.name,
            studyPathId = path.id,
            studyPathName = path.name,
            courses = courses,
            careerMatId = matId
        )
    }

    /** Courses of the libretto still to pass that the curriculum offers. */
    private fun StudyPath.matches(toPass: Set<String>): Int = academicYears.sumOf { year ->
        year.courses.count { it.code in toPass || it.moduleCodes.any(toPass::contains) }
    }

    private suspend fun findProgram(cdsCode: String, cohort: Int): DegreeProgram? {
        for (year in cohort downTo cohort - LOOKBACK_YEARS) {
            val program = try {
                storageDataSource.findDegreeProgram(cdsCode, year)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
            if (program != null) return program
        }
        return null
    }

    private companion object {
        const val LOOKBACK_YEARS = 3
    }
}

/** The catalogue writes names in upper case ("INGEGNERIA INFORMATICA"); text with lower case is left alone. */
private fun String.toDisplayName(): String {
    if (any(Char::isLowerCase)) return this
    return lowercase(Locale.ITALIAN).split(' ').joinToString(" ") { word -> word.replaceFirstChar { it.titlecase(Locale.ITALIAN) } }
}
