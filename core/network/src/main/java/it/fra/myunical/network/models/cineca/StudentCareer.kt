package it.fra.myunical.network.models.cineca

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StudentCareer(
    // Student identity
    @SerialName("stuId") val studentId: Long,
    @SerialName("matId") val careerId: Long,
    @SerialName("matricola") val studentNumber: String,
    @SerialName("userId") val userId: String,
    @SerialName("nome") val firstName: String,
    @SerialName("cognome") val lastName: String,
    @SerialName("codFis") val fiscalCode: String,
    @SerialName("sesso") val gender: String? = null,
    @SerialName("dataNascita") val birthDate: String? = null,
    @SerialName("email") val personalEmail: String? = null,
    @SerialName("emailAte") val institutionalEmail: String? = null,

    // Academic program
    @SerialName("aaDes") val academicYear: String,
    @SerialName("annoCorso") val courseYear: Int,
    @SerialName("p06CdsCod") val courseCode: String,
    @SerialName("p06CdsDes") val courseDescription: String,
    @SerialName("tipoCorsoCod") val degreeTypeCode: String,
    @SerialName("tipoCorsoDes") val degreeTypeDescription: String,
    @SerialName("facDes") val departmentName: String? = null,
    @SerialName("sediDes") val campus: String? = null,

    // Status & flags
    @SerialName("staStuCod") val statusCode: String,
    @SerialName("statiStuDes") val statusDescription: String,
    @SerialName("motStastuCod") val statusReasonCode: String? = null,
    @SerialName("motStastuDes") val statusReasonDescription: String? = null,
    @SerialName("sospFlg") val isSuspended: Int = 0,
    @SerialName("ptFlg") val isPartTime: Int = 0,
    @SerialName("attlauFlg") val graduationApplicationFlag: Int = 0,

    // Career lifecycle dates
    @SerialName("dataIscr") val enrollmentDate: String? = null,
    @SerialName("dataChiusura") val closureDate: String? = null,
    @SerialName("dataFineCarriera") val careerEndDate: String? = null
)