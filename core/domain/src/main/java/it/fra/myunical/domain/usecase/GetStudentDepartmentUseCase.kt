package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.model.degreeexplorer.Department
import it.fra.myunical.domain.model.schedule.AcademicYear
import it.fra.myunical.domain.model.syllabus.cohortYear
import it.fra.myunical.domain.sources.UnicalStorageDataSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Department of the current career's degree course, which is also the source of its news.
 * Null when the course or its department cannot be determined.
 */
class GetStudentDepartmentUseCase(
    private val getCareerData: GetCareerDataUseCase,
    private val resolveCdsCode: ResolveCdsCodeUseCase,
    private val finder: DepartmentFinder
) {
    suspend operator fun invoke(): Department? {
        val data = getCareerData().first().getOrNull() ?: return null
        val cdsCode = resolveCdsCode(data.career) ?: return null
        return finder.find(cdsCode, data.courses.cohortYear())
    }
}

/** Looks up the department of a degree course; the department list never changes, so it is kept. */
class DepartmentFinder(
    private val storageDataSource: UnicalStorageDataSource,
    private val currentYear: () -> Int = AcademicYear::current
) {
    private val departmentsMutex = Mutex()
    private var departments: List<Department>? = null

    /** The cohort's record is the right one; if it is missing the course is looked up in the current year. */
    suspend fun find(cdsCode: String, cohort: Int?): Department? {
        val code = departmentCodeOf(cdsCode, cohort) ?: return null
        return allDepartments().firstOrNull { it.code == code }
    }

    private suspend fun departmentCodeOf(cdsCode: String, cohort: Int?): String? {
        for (year in listOfNotNull(cohort, currentYear()).distinct()) {
            val code = try {
                storageDataSource.findDegreeProgram(cdsCode, year)?.departmentCode
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
            if (!code.isNullOrEmpty()) return code
        }
        return null
    }

    private suspend fun allDepartments(): List<Department> = departmentsMutex.withLock {
        departments ?: storageDataSource.getDepartments().also { departments = it }
    }
}
