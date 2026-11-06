package it.fra.myunical.domain.sources

import it.fra.myunical.domain.model.degreeexplorer.DegreeProgram
import it.fra.myunical.domain.model.degreeexplorer.StudyPath
import it.fra.myunical.domain.model.schedule.ScheduleEvent

interface UnicalStorageDataSource {
    suspend fun getAllDegreeCourses(): List<DegreeProgram>
    suspend fun getStudyPlansForCds(regDidId: Int): List<StudyPath>

    /** Returns the lessons of [cdsYear] for the given calendar [month]; `providerId` is left empty. */
    suspend fun getTimetable(
        cdsCode: String,
        academicYear: Int,
        cdsYear: Int,
        year: Int,
        month: Int
    ): List<ScheduleEvent>
}
