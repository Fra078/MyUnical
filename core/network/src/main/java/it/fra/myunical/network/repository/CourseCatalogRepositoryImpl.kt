package it.fra.myunical.network.repository

import it.fra.myunical.domain.model.degreeexplorer.StudyPath
import it.fra.myunical.domain.model.schedule.AcademicYear
import it.fra.myunical.domain.model.syllabus.CourseLookup
import it.fra.myunical.domain.model.syllabus.CourseSyllabus
import it.fra.myunical.domain.model.syllabus.CourseUnit
import it.fra.myunical.domain.model.syllabus.unitsFor
import it.fra.myunical.domain.repository.CourseCatalogRepository
import it.fra.myunical.domain.sources.UnicalStorageDataSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Finds the pages of a libretto course in the study plan of the student's cohort, where the
 * ids of modules and groups are listed; courses outside the plan (free choice, older
 * cohorts) are searched by name in the year they were attended. Plans and pages are cached
 * for the session.
 */
internal class CourseCatalogRepositoryImpl(
    private val dataSource: UnicalStorageDataSource
) : CourseCatalogRepository {

    private val studyPlans = mutableMapOf<Pair<String, Int>, List<StudyPath>>()
    private val syllabi = mutableMapOf<Int, CourseSyllabus>()
    private val mutex = Mutex()

    override suspend fun findUnits(lookup: CourseLookup): List<CourseUnit> {
        val cdsCode = lookup.cdsCode
        val cohort = lookup.cohort
        val fromPlan = if (cdsCode != null && cohort != null) {
            // A missing plan is not fatal: the search below can still find the course
            optional { studyPlan(cdsCode, cohort) }?.unitsFor(lookup.code).orEmpty()
        } else emptyList()
        if (fromPlan.isNotEmpty()) return fromPlan

        val years = listOf(lookup.attendanceYear, AcademicYear.current()).filter { it > 0 }.distinct()
        for (year in years) {
            val units = dataSource.searchActivities(lookup.name, year).unitsFor(lookup.code, lookup.cdsCode)
            if (units.isNotEmpty()) return units
        }
        return emptyList()
    }

    override suspend fun getSyllabus(activityId: Int): CourseSyllabus {
        mutex.withLock { syllabi[activityId] }?.let { return it }
        val own = dataSource.getSyllabus(activityId)
        // Borrowed pages are empty: the contents are on the original activity
        val source = own.borrowedFromId?.takeIf { !own.hasContents }?.let { optional { dataSource.getSyllabus(it) } }
        val syllabus = source?.copy(activityId = own.activityId, partition = own.partition ?: source.partition) ?: own
        mutex.withLock { syllabi[activityId] = syllabus }
        return syllabus
    }

    private suspend fun studyPlan(cdsCode: String, cohort: Int): List<StudyPath> {
        val key = cdsCode to cohort
        mutex.withLock { studyPlans[key] }?.let { return it }
        val program = dataSource.findDegreeProgram(cdsCode, cohort) ?: return emptyList()
        val plan = dataSource.getStudyPlansForCds(program.regulationId)
        mutex.withLock { studyPlans[key] = plan }
        return plan
    }

    private inline fun <T> optional(block: () -> T): T? = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }
}
