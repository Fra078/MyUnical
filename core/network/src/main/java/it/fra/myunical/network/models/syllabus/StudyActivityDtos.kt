package it.fra.myunical.network.models.syllabus

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `ricerca/activities/{id}/`: public page of a teaching activity. */
@Serializable
data class StudyActivityDetailResponse(
    val results: StudyActivityDetailDto
)

@Serializable
data class StudyActivityDetailDto(
    @SerialName("StudyActivityID") val id: Int,
    @SerialName("StudyActivityCod") val code: String,
    @SerialName("StudyActivityName") val name: String,
    @SerialName("StudyActivityPartitionDes") val partition: String? = null,
    @SerialName("StudyActivityCdSName") val degreeCourse: String? = null,
    @SerialName("StudyActivityECTS") val credits: Double? = null,
    @SerialName("StudyActivityLanguage") val languages: List<String> = emptyList(),
    @SerialName("StudyActivitySSDCod") val ssdCode: String? = null,
    @SerialName("StudyActivitySSD") val ssd: String? = null,
    // "Si" / "No"
    @SerialName("StudyActivityCompulsory") val compulsory: String? = null,
    @SerialName("StudyActivityYear") val year: Int? = null,
    @SerialName("StudyActivitySemester") val semester: String? = null,
    @SerialName("StudyActivityTeachingUnitType") val teachingUnitType: String? = null,
    @SerialName("StudyActivityHours") val hours: List<StudyActivityHoursDto> = emptyList(),
    @SerialName("StudyActivityContents") val contents: List<StudyActivityContentDto> = emptyList(),
    @SerialName("StudyActivityBorrowedFrom") val borrowedFrom: BorrowedActivityDto? = null
)

/** Hours of one activity type taught by one teacher. */
@Serializable
data class StudyActivityHoursDto(
    @SerialName("ActivityType") val type: String? = null,
    @SerialName("Hours") val hours: Int? = null,
    @SerialName("StudyActivityTeacherID") val teacherId: String? = null,
    @SerialName("StudyActivityTeacherName") val teacherName: String? = null
)

@Serializable
data class StudyActivityContentDto(
    @SerialName("StudyActivityContentCod") val code: String,
    @SerialName("StudyActivityContentDes") val text: String? = null
)

@Serializable
data class BorrowedActivityDto(
    @SerialName("StudyActivityID") val id: Int
)

/** `ricerca/activities/`: activities offered in an academic year. */
@Serializable
data class StudyActivityListResponse(
    val results: List<StudyActivityListItemDto> = emptyList()
)

@Serializable
data class StudyActivityListItemDto(
    @SerialName("StudyActivityID") val id: Int,
    @SerialName("StudyActivityCod") val code: String,
    @SerialName("StudyActivityName") val name: String,
    @SerialName("StudyActivityCdSCod") val cdsCode: String? = null,
    @SerialName("StudyActivityPartitionDes") val partition: String? = null,
    @SerialName("StudyActivitySemester") val semester: String? = null
)
