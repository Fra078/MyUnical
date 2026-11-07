package it.fra.myunical.domain.sources

import it.fra.myunical.domain.model.degreeexplorer.DegreeProgram
import it.fra.myunical.domain.model.degreeexplorer.StudyPath
import it.fra.myunical.domain.model.schedule.ScheduleEvent
import it.fra.myunical.domain.model.syllabus.CourseSyllabus
import it.fra.myunical.domain.model.syllabus.OfferedActivity

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

    /** The degree course [cdsCode] as offered to the students enrolled in [academicYear]. */
    suspend fun findDegreeProgram(cdsCode: String, academicYear: Int): DegreeProgram?

    /** Teaching activities offered in [academicYear] whose name matches [query]. */
    suspend fun searchActivities(query: String, academicYear: Int): List<OfferedActivity>

    /** Public page ("scheda insegnamento") of a teaching activity. */
    suspend fun getSyllabus(activityId: Int): CourseSyllabus
}
