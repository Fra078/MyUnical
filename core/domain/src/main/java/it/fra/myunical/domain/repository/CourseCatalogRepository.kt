package it.fra.myunical.domain.repository

import it.fra.myunical.domain.model.syllabus.CourseLookup
import it.fra.myunical.domain.model.syllabus.CourseSyllabus
import it.fra.myunical.domain.model.syllabus.CourseUnit

/** Public pages of the courses in the libretto. */
interface CourseCatalogRepository {
    /** Units with a page of a libretto course; empty when the catalogue does not list it. */
    suspend fun findUnits(lookup: CourseLookup): List<CourseUnit>

    /** Page of [activityId]; borrowed pages are resolved to the activity holding the contents. */
    suspend fun getSyllabus(activityId: Int): CourseSyllabus
}
