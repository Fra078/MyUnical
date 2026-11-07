package it.fra.myunical.domain.usecase

import it.fra.myunical.domain.model.degreeexplorer.DegreeRegulations
import it.fra.myunical.domain.model.degreeexplorer.Regulation
import it.fra.myunical.domain.model.schedule.AcademicYear
import it.fra.myunical.domain.model.syllabus.cohortYear
import it.fra.myunical.domain.sources.UnicalStorageDataSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

/** Regulations of the current career's degree course; null when the course or the matriculation year is unknown. */
class GetDegreeRegulationsUseCase(
    private val getCareerData: GetCareerDataUseCase,
    private val resolveCdsCode: ResolveCdsCodeUseCase,
    private val finder: DegreeRegulationFinder
) {
    suspend operator fun invoke(): DegreeRegulations? {
        val data = getCareerData().first().getOrNull() ?: return null
        val cdsCode = resolveCdsCode(data.career) ?: return null
        val cohort = data.courses.cohortYear() ?: return null
        return finder.find(cdsCode, cohort)
    }
}

/**
 * The catalogue has one record per academic year, and recent years may not have the PDF yet, so
 * older years are tried until one has it.
 */
class DegreeRegulationFinder(
    private val storageDataSource: UnicalStorageDataSource,
    private val currentYear: () -> Int = AcademicYear::current
) {
    /** The regulation of the matriculation year [cohort] and the latest one published. */
    suspend fun find(cdsCode: String, cohort: Int): DegreeRegulations {
        val current = maxOf(currentYear(), cohort)
        return DegreeRegulations(
            cohort = firstRegulation(cdsCode, cohort downTo cohort - LOOKBACK_YEARS),
            latest = firstRegulation(cdsCode, current downTo cohort)
        )
    }

    private suspend fun firstRegulation(cdsCode: String, years: IntProgression): Regulation? {
        for (year in years) {
            val url = try {
                storageDataSource.findDegreeProgram(cdsCode, year)?.regulationUrl
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
            if (url != null) return Regulation(year, url)
        }
        return null
    }

    private companion object {
        const val LOOKBACK_YEARS = 3
    }
}
