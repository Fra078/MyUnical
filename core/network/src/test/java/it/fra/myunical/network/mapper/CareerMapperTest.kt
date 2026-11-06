package it.fra.myunical.network.mapper

import it.fra.myunical.network.models.cineca.ExamSessionDto
import it.fra.myunical.network.models.cineca.LibrettoAverageDto
import it.fra.myunical.network.models.cineca.LibrettoStatsDto
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

/** Based on real (anonymized) libretto-service-v2 responses. */
class CareerMapperTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private fun resource(name: String) =
        checkNotNull(javaClass.classLoader?.getResource("career/$name")).readText()

    @Test
    fun `maps stats and official averages`() {
        val stats = json.decodeFromString<LibrettoStatsDto>(resource("stats.json"))
        val averages = json.decodeFromString<List<LibrettoAverageDto>>(resource("medie.json"))
        val overview = stats.toCareerOverview(averages)

        assertEquals(180, overview.requiredCredits)
        assertEquals(180, overview.earnedCredits)
        assertEquals(23, overview.passedCount)
        assertEquals(27.68, overview.weightedAverage!!, 0.001)
        assertEquals(27.76, overview.arithmeticAverage!!, 0.001)
        assertEquals(101.51, overview.graduationBase!!, 0.001)
        assertEquals(1f, overview.progress)
    }

    @Test
    fun `maps exam sessions with time, booking window and state`() {
        val sessions = json.decodeFromString<List<ExamSessionDto>>(resource("appelli.json"))
            .mapNotNull(ExamSessionDto::toDomainModel)

        assertEquals(4, sessions.size)
        val wireless = sessions.first()
        assertEquals("Wireless Devices And Networks", wireless.courseName)
        assertEquals(LocalDate.of(2026, 11, 13), wireless.date)
        assertEquals(LocalTime.of(10, 0), wireless.time)
        assertEquals(LocalDate.of(2026, 11, 11), wireless.enrollEnd)
        assertTrue(wireless.isBookingOpen)
        assertEquals("Luca Neri", wireless.president)
        assertFalse(sessions.last().isBookingOpen)
    }

    @Test
    fun `title cases upper case names keeping mixed case words`() {
        assertEquals("Wireless Devices And Networks", "WIRELESS DEVICES AND NETWORKS".toTitleCase())
        assertEquals("IoT Device Programming", "IoT DEVICE PROGRAMMING".toTitleCase())
        assertEquals("Fisica - Meccanica", "Fisica - Meccanica".toTitleCase())
    }
}
