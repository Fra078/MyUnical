package it.fra.myunical.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    @SerialName("authToken") val authToken: String,
    @SerialName("credentials") val credentials: UserCredentials,
    @SerialName("expPwd") val isPasswordExpired: Boolean,
    @SerialName("internalAuthToken") val internalAuthToken: String,
    @SerialName("profili") val profiles: List<String> = emptyList(),
    @SerialName("user") val user: UserProfile
)

@Serializable
data class UserCredentials(
    @SerialName("jwtIsExpired") val isJwtExpired: Boolean,
    @SerialName("jwtKeyId") val jwtKeyId: String? = null,
    @SerialName("kind") val kind: CredentialKind,
    @SerialName("profile") val profile: String? = null,
    @SerialName("user") val username: String
)

@Serializable
data class CredentialKind(
    @SerialName("value") val value: String
)

@Serializable
data class UserProfile(
    @SerialName("aliasName") val aliasName: String,
    @SerialName("codFis") val fiscalCode: String,
    @SerialName("docenteId") val teacherId: Long? = null,
    @SerialName("firstName") val firstName: String,
    @SerialName("grpDes") val groupDescription: String,
    @SerialName("grpId") val groupId: Long,
    @SerialName("grpPta") val groupPta: Int,
    @SerialName("grpUserTecnicoFlg") val technicalUserFlag: Int,
    @SerialName("id") val id: Long,
    @SerialName("idAb") val addressBookId: Long,
    @SerialName("lastName") val lastName: String,
    @SerialName("persId") val personId: Long,
    @SerialName("sessionTimeout") val sessionTimeoutMinutes: Int,
    @SerialName("sex") val gender: String,
    @SerialName("soggEstId") val externalSubjectId: Long? = null,
    @SerialName("tipoFirmaFaId") val signatureTypeFaId: Int,
    @SerialName("tipoFirmaId") val signatureTypeId: Int,
    @SerialName("trattiCarriera") val careerTracks: List<CareerTrack> = emptyList(),
    @SerialName("userId") val userId: String
)

@Serializable
data class CareerTrack(
    @SerialName("cdsDes") val degreeCourseDescription: String,
    @SerialName("cdsId") val degreeCourseId: Long,
    @SerialName("dettaglioTratto") val details: CareerTrackDetails,
    @SerialName("matId") val matriculationId: Long,
    @SerialName("matricola") val studentNumber: String,
    @SerialName("motStastuCod") val studentStatusReasonCode: String,
    @SerialName("motStastuDes") val studentStatusReasonDescription: String,
    @SerialName("staMatCod") val matriculationStatusCode: String,
    @SerialName("staMatDes") val matriculationStatusDescription: String,
    @SerialName("staStuCod") val studentStatusCode: String,
    @SerialName("staStuDes") val studentStatusDescription: String,
    @SerialName("stuId") val studentId: Long
)

@Serializable
data class CareerTrackDetails(
    @SerialName("aaIscrId") val enrollmentAcademicYear: Int,
    @SerialName("aaOrdId") val regulationAcademicYear: Int,
    @SerialName("aaRegId") val orderAcademicYear: Int,
    @SerialName("anniFC") val outOfCourseYears: Int,
    @SerialName("annoCorso") val currentCourseYear: Int,
    @SerialName("cdsCod") val degreeCourseCode: String,
    @SerialName("cdsId") val degreeCourseId: Long,
    @SerialName("condFlg") val conditionalFlag: Int,
    @SerialName("domiscrFlg") val enrollmentApplicationFlag: Int,
    @SerialName("durataAnni") val durationYears: Int,
    @SerialName("facCod") val facultyCode: String,
    @SerialName("facId") val facultyId: Long,
    @SerialName("iscrId") val enrollmentId: Long,
    @SerialName("matId") val matriculationId: Long,
    @SerialName("mobilFlg") val mobilityFlag: Int,
    @SerialName("motStaiscrCod") val enrollmentStatusReasonCode: String,
    @SerialName("motStamatCod") val matriculationStatusReasonCode: String,
    @SerialName("motStastuCod") val studentStatusReasonCode: String,
    @SerialName("normId") val regulationId: Long,
    @SerialName("notaBloccanteFlg") val blockingNoteFlag: Int,
    @SerialName("passaggioFlg") val transferFlag: Int,
    @SerialName("pdsCod") val studyPlanCode: String,
    @SerialName("pdsId") val studyPlanId: Long,
    @SerialName("profCod") val profileCode: String,
    @SerialName("ptFlg") val partTimeFlag: Int,
    @SerialName("staIscrCod") val enrollmentStatusCode: String,
    @SerialName("staMatCod") val matriculationStatusCode: String,
    @SerialName("staStuCod") val studentStatusCode: String,
    @SerialName("stuId") val studentId: Long,
    @SerialName("tipoCatAmmId") val admissionCategoryId: Long? = null,
    @SerialName("tipoCorsoCod") val courseTypeCode: String,
    @SerialName("tipoIscrCod") val enrollmentTypeCode: String,
    @SerialName("tipoSpecCod") val specializationTypeCode: String,
    @SerialName("ultimoAnnoFlg") val isLastYearFlag: Int
)