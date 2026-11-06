package it.fra.myunical.network.mapper

import it.fra.myunical.domain.model.career.CareerOverview
import it.fra.myunical.domain.model.career.ExamBooking
import it.fra.myunical.domain.model.career.ExamSession
import it.fra.myunical.network.models.cineca.ExamBookingDto
import it.fra.myunical.network.models.cineca.ExamSessionDto
import it.fra.myunical.network.models.cineca.LibrettoAverageDto
import it.fra.myunical.network.models.cineca.LibrettoStatsDto
import java.util.Locale

fun LibrettoStatsDto.toCareerOverview(averages: List<LibrettoAverageDto>): CareerOverview {
    fun average(base: Int, type: String) =
        averages.firstOrNull { it.base == base && it.tipoMediaCod?.value == type }?.media
    return CareerOverview(
        requiredCredits = umPesoMin?.toInt() ?: 0,
        earnedCredits = umPesoSuperato?.toInt() ?: 0,
        passedCount = numAdSuperate ?: 0,
        totalCount = numAdLibretto ?: 0,
        weightedAverage = average(30, "P"),
        arithmeticAverage = average(30, "A"),
        graduationBase = average(110, "P")
    )
}

fun ExamSessionDto.toDomainModel(): ExamSession? {
    val date = dataInizioApp.toEsse3Date() ?: return null
    return ExamSession(
        id = appId ?: return null,
        courseRowId = adsceId ?: return null,
        courseName = adDes.clean()?.toTitleCase() ?: "Esame",
        description = desApp.clean().orEmpty(),
        date = date,
        time = oraEsa.toEsse3Time(),
        enrollStart = dataInizioIscr.toEsse3Date(),
        enrollEnd = dataFineIscr.toEsse3Date(),
        isBookingOpen = stato == "P",
        president = listOfNotNull(presidenteNome.clean(), presidenteCognome.clean())
            .takeIf { it.isNotEmpty() }
            ?.joinToString(" ") { it.toTitleCase() }
    )
}

fun ExamBookingDto.toDomainModel(): ExamBooking? = ExamBooking(
    courseRowId = adsceId ?: return null,
    sessionId = appId ?: return null,
    bookingId = applistaId,
    courseName = adStuDes.clean()?.toTitleCase(),
    date = dataOraTurno.toEsse3Date(),
    position = posiz,
    room = aulaDes.clean(),
    building = edificioDes.clean()
)

/**
 * Esse3 names are upper case: "WIRELESS DEVICES AND NETWORKS" -> "Wireless Devices And Networks".
 * In mixed names only the upper case words change, so "IoT DEVICE PROGRAMMING" -> "IoT Device Programming".
 */
// "II", "IV", "VIII": numbered courses keep their numeral ("Quantum Field Theory II")
private val RomanNumeral = Regex("^(?=[IVX])X{0,3}(IX|IV|V?I{0,3})$")

internal fun String.toTitleCase(): String {
    val allUpper = none { it.isLowerCase() }
    return split(' ').joinToString(" ") { word ->
        val upperWord = word.any { it.isLetter() } && word.none { it.isLowerCase() }
        when {
            word.matches(RomanNumeral) -> word
            allUpper || (upperWord && word.length > 3) ->
                word.lowercase(Locale.ITALIAN).replaceFirstChar { it.titlecase(Locale.ITALIAN) }
            else -> word
        }
    }
}
