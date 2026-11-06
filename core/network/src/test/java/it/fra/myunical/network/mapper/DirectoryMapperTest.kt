package it.fra.myunical.network.mapper

import it.fra.myunical.network.models.directory.AddressBookDetailDto
import it.fra.myunical.network.models.directory.AddressBookPageDto
import it.fra.myunical.network.models.directory.PublicationPageDto
import it.fra.myunical.network.models.directory.ResearchLinePageDto
import it.fra.myunical.network.models.directory.StudyActivityPageDto
import it.fra.myunical.network.models.directory.TeacherDetailDto
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Based on real responses of the public address book (storage.portale.unical.it). */
class DirectoryMapperTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    private fun resource(name: String) =
        checkNotNull(javaClass.classLoader?.getResource("directory/$name")).readText()

    @Test
    fun `maps a search page`() {
        val page = json.decodeFromString<AddressBookPageDto>(resource("search.json")).toDomainModel()

        assertEquals(3, page.items.size)
        assertTrue(page.hasNext)
        assertEquals(1, page.page)
        val first = page.items.first()
        assertEquals("andrea.rossi", first.id)
        assertEquals("Andrea Rossi", first.name)
        assertEquals("Personale Tecnico Amministrativo", first.roles.first().title)
        assertEquals(listOf("andrea.rossi@example.org"), first.emails)
        assertEquals(listOf("0984/000000"), first.phones)
    }

    @Test
    fun `maps the detail with duties`() {
        val contact = json.decodeFromString<AddressBookDetailDto>(resource("detail.json")).results!!.toDomainModel()!!

        assertEquals("Andrea Rossi", contact.name)
        assertFalse(contact.isTeacher)
        assertEquals("Responsabile Amministrativo", contact.functions.single().title)
    }

    @Test
    fun `formats names written surname first`() {
        assertEquals("Andrea Rossi", "ROSSI Andrea".toDisplayName())
        assertEquals("Maria Lo Giudice", "LO GIUDICE Maria".toDisplayName())
        assertEquals("Anna D'Amico", "Anna D'AMICO".toDisplayName())
        assertEquals("Rossi Giovanni", "ROSSI GIOVANNI".toDisplayName())
    }

    @Test
    fun `keeps roman numerals in course names`() {
        assertEquals("Quantum Field Theory II", "QUANTUM FIELD THEORY II".toTitleCase())
        assertEquals("Analisi Matematica I", "ANALISI MATEMATICA I".toTitleCase())
    }

    @Test
    fun `decodes the teacher flag written as a number`() {
        val contact = json.decodeFromString<AddressBookDetailDto>(resource("detail_teacher.json")).results!!.toDomainModel()!!

        assertTrue(contact.isTeacher)
        assertEquals("Marco Rossi", contact.name)
        assertEquals(listOf("Cubo 31C Piano 5 Stanza 6"), contact.offices)
    }

    @Test
    fun `maps a teacher page with photo, courses and publications`() {
        val teacher = json.decodeFromString<TeacherDetailDto>(resource("teacher_9002.json")).results!!
        val profile = teacher.toTeacherProfile("9002", 2026, null, null, null)

        assertEquals("https://storage.portale.unical.it/media/portale/docenti_pta/foto/docente.png", profile.photoUrl)
        assertTrue(profile.cvUrl!!.startsWith("https://storage.portale.unical.it/"))
        assertEquals("0000-0000-0000-0000", profile.orcid)
        assertTrue(profile.courses.isEmpty())
    }

    @Test
    fun `maps teaching and research of a teacher without photo`() {
        val teacher = json.decodeFromString<TeacherDetailDto>(resource("teacher_9001.json")).results!!
        val profile = teacher.toTeacherProfile(
            id = "9001",
            academicYear = 2026,
            activities = json.decodeFromString<StudyActivityPageDto>(resource("activities_9001.json")),
            researchLines = json.decodeFromString<ResearchLinePageDto>(resource("researchlines_9001.json")),
            publications = json.decodeFromString<PublicationPageDto>(resource("publications_9001.json"))
        )

        assertNull(profile.photoUrl)
        val course = profile.courses.first { it.name == "Metodi Matematici Della Fisica" }
        assertEquals("Fisica", course.degreeCourse)
        assertEquals(2, course.year)
        assertEquals(profile.courses.size, profile.courses.distinctBy { it.name to it.degreeCourse }.size)
        assertEquals(listOf("Fisica teorica delle interazioni fondamentali"), profile.researchLines)
        assertEquals(5, profile.publications.size)
        assertEquals(56, profile.publicationCount)
        assertEquals("https://www.unical.it/storage/teachers/9001/", profile.pageUrl)
    }
}
