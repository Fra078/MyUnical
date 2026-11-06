package it.fra.myunical.network.models.schedule

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TimetableEntryDto(
    @SerialName("insegnamento") val courseName: String,
    // Missing for activities outside the study plan (e.g. OFA courses)
    @SerialName("codice_insegnamento") val courseCode: String? = null,
    @SerialName("dataInizio") val startDate: String,
    @SerialName("orarioInizio") val startTime: String,
    @SerialName("orarioFine") val endTime: String,
    @SerialName("annoCorso") val cdsYear: Int? = null,
    @SerialName("docenti") val teachers: List<String> = emptyList(),
    @SerialName("aule") val rooms: List<TimetableRoomDto> = emptyList()
)

@Serializable
data class TimetableRoomDto(
    @SerialName("nome") val name: String,
    @SerialName("edificio") val building: String? = null
)
