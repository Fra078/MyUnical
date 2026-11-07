package it.fra.myunical.network.mapper

import it.fra.myunical.domain.model.syllabus.SyllabusSectionKind
import it.fra.myunical.domain.model.syllabus.TeachingHours
import it.fra.myunical.network.models.syllabus.StudyActivityDetailResponse
import it.fra.myunical.network.models.syllabus.StudyActivityListResponse
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Based on real responses of the course catalogue (storage.portale.unical.it). */
class SyllabusMapperTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    private fun resource(name: String) =
        checkNotNull(javaClass.classLoader?.getResource("syllabus/$name")).readText()

    @Test
    fun `maps the page of a module`() {
        val syllabus = json.decodeFromString<StudyActivityDetailResponse>(resource("module.json")).results.toDomainModel()

        assertEquals(203557, syllabus.activityId)
        assertEquals("Modulo 1: MECCANICA", syllabus.name)
        assertEquals(6.0, syllabus.credits)
        assertEquals("FIS/01", syllabus.ssdCode)
        assertEquals("Primo Semestre", syllabus.semester)
        assertEquals(true, syllabus.isCompulsory)
        assertNull(syllabus.borrowedFromId)

        assertEquals(listOf("9003", "9004"), syllabus.teachers.map { it.id })
        assertEquals("Lombardi Chiara", syllabus.teachers.first().name)
        assertEquals(listOf(TeachingHours("LEZ", 38), TeachingHours("ESE", 18)), syllabus.hoursByType)
        assertEquals(56, syllabus.totalHours)

        // Empty contents (PROPEDE) and the ones not shown as sections are skipped
        assertFalse(syllabus.sections.any { it.kind == SyllabusSectionKind.PROPAEDEUTICS })
        assertEquals(SyllabusSectionKind.OBJECTIVES, syllabus.sections.first().kind)
        val program = checkNotNull(syllabus.section(SyllabusSectionKind.PROGRAM))
        assertFalse(program.contains(' '))
        assertTrue(program.startsWith("MODULO I-MECCANICA"))

        assertEquals("65cup0z", syllabus.teamsCode)
        // The team name is not a link
        assertNull(syllabus.teamsLink)
    }

    @Test
    fun `a borrowed page points to the original activity`() {
        val syllabus = json.decodeFromString<StudyActivityDetailResponse>(resource("borrowed.json")).results.toDomainModel()

        assertEquals(203549, syllabus.borrowedFromId)
        assertFalse(syllabus.hasContents)
    }

    @Test
    fun `maps search results`() {
        val activities = json.decodeFromString<StudyActivityListResponse>(resource("search.json")).results.map { it.toDomainModel() }

        val first = activities.first()
        assertEquals(203549, first.activityId)
        assertEquals("27000001", first.code)
        assertEquals("0876", first.cdsCode)
        assertNull(first.partition)
    }
}
