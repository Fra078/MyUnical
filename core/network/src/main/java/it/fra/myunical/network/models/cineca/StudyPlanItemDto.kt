package it.fra.myunical.network.models.cineca

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StudyPlanItemDto(
    @SerialName("adCod")
    val courseCode: String,

    @SerialName("adDes")
    val courseName: String,

    @SerialName("adsceId")
    val activityId: Long,

    @SerialName("annoCorso")
    val courseYear: Int,

    @SerialName("peso")
    val credits: Double,

    @SerialName("aaFreqId")
    val attendanceAcademicYear: Int,

    @SerialName("stato")
    val status: ValueObjectDto,

    @SerialName("statoDes")
    val statusDescription: String,

    @SerialName("tipoEsaDes")
    val examTypeDescription: String,

    @SerialName("tipoInsDes")
    val courseTypeDescription: String,

    @SerialName("esito")
    val outcome: ExamOutcomeDto? = null
)

@Serializable
data class ExamOutcomeDto(
    @SerialName("voto")
    val grade: Double? = null,

    @SerialName("lodeFlg")
    val honorsFlag: Int? = 0,

    @SerialName("dataEsa")
    val examDate: String? = null,

    @SerialName("aaSupId")
    val passedAcademicYear: Int? = null,

    @SerialName("modValCod")
    val evaluationMode: ValueObjectDto? = null,

    @SerialName("tipoGiudCod")
    val judgmentCode: String? = null,

    @SerialName("tipoGiudDes")
    val judgmentDescription: String? = null
)

@Serializable
data class ValueObjectDto(
    @SerialName("value")
    val value: String
)