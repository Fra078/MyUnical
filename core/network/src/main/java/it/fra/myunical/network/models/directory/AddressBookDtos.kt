package it.fra.myunical.network.models.directory

import it.fra.myunical.network.models.LenientBooleanSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddressBookPageDto(
    val next: String? = null,
    val count: Int = 0,
    @SerialName("page_number") val pageNumber: Int = 1,
    val results: List<AddressBookPersonDto> = emptyList()
)

@Serializable
data class AddressBookDetailDto(
    val results: AddressBookPersonDto? = null
)

@Serializable
data class AddressBookPersonDto(
    @SerialName("Name") val name: String? = null,
    @SerialName("ID") val id: String? = null,
    @SerialName("Roles") val roles: List<AddressBookRoleDto> = emptyList(),
    @SerialName("Email") val emails: List<String> = emptyList(),
    @SerialName("PEC") val pecs: List<String> = emptyList(),
    @SerialName("TelOffice") val officePhones: List<String> = emptyList(),
    @SerialName("TelCelOffice") val mobilePhones: List<String> = emptyList(),
    @SerialName("WebSite") val websites: List<String> = emptyList(),
    @SerialName("OfficeReference") val offices: List<String> = emptyList(),
    @Serializable(with = LenientBooleanSerializer::class)
    @SerialName("Teacher") val teacher: Boolean = false,
    @SerialName("PersonFunctions") val functions: List<AddressBookFunctionDto> = emptyList()
)

@Serializable
data class AddressBookRoleDto(
    @SerialName("RoleDescription") val description: String? = null,
    @SerialName("Structure") val structure: String? = null,
    @SerialName("Priority") val priority: Int? = null
)

@Serializable
data class AddressBookFunctionDto(
    @SerialName("TeacherRole") val role: String? = null,
    @SerialName("StructureName") val structure: String? = null
)

@Serializable
data class TeacherDetailDto(
    val results: TeacherDto? = null
)

@Serializable
data class TeacherDto(
    @SerialName("TeacherID") val id: String? = null,
    @SerialName("PhotoPath") val photoPath: String? = null,
    @SerialName("TeacherSSDDescription") val sectorDescription: String? = null,
    @SerialName("ReceptionHours") val receptionHours: String? = null,
    @SerialName("ShortBio") val shortBio: String? = null,
    @SerialName("CVPathIta") val cvPathIt: String? = null,
    @SerialName("CVPathEn") val cvPathEn: String? = null,
    @SerialName("ORCID") val orcid: String? = null
)

@Serializable
data class StudyActivityPageDto(
    val results: List<StudyActivityDto> = emptyList()
)

@Serializable
data class StudyActivityDto(
    @SerialName("StudyActivityName") val name: String? = null,
    @SerialName("StudyActivityCdSName") val degreeCourse: String? = null,
    @SerialName("StudyActivityYear") val year: Double? = null,
    @SerialName("StudyActivitySemester") val semester: String? = null,
    @SerialName("StudyActivityLanguage") val language: String? = null
)

@Serializable
data class ResearchLinePageDto(
    val results: List<ResearchLineDto> = emptyList()
)

@Serializable
data class ResearchLineDto(
    @SerialName("RLineDescription") val description: String? = null
)

@Serializable
data class PublicationPageDto(
    val count: Int = 0,
    val results: List<PublicationDto> = emptyList()
)

@Serializable
data class PublicationDto(
    @SerialName("PublicationTitle") val title: String? = null,
    @SerialName("PublicationYear") val year: Int? = null,
    @SerialName("PublicationLabel") val label: String? = null,
    @SerialName("PublicationUrl") val url: String? = null
)
