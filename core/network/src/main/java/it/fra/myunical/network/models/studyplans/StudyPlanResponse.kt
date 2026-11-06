package it.fra.myunical.network.models.studyplans

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StudyPlanResponse(
    @SerialName("page_number") val pageNumber: Int,
    @SerialName("total_pages") val totalPages: Int,
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<StudyPlanResult>
)

@Serializable
data class StudyPlanResult(
    @SerialName("RegPlanId") val regPlanId: Int,
    @SerialName("RegDidId") val regDidId: Int,
    @SerialName("YearCoorteId") val yearCoorteId: Int,
    @SerialName("CdSDuration") val cdsDuration: Int,
    @SerialName("PlanTabs") val planTabs: List<PlanTab>
)

@Serializable
data class PlanTab(
    @SerialName("StudyPlanID") val studyPlanId: Int,
    @SerialName("StudyPlanCOD") val studyPlanCod: String,
    @SerialName("StudyPlanName") val studyPlanName: String,
    // Maps numeric string keys (e.g., "1", "2", "3" for academic years) to their respective activities
    @SerialName("StudyActivities") val studyActivities: Map<String, List<StudyActivity>>
)

@Serializable
data class StudyActivity(
    @SerialName("StudyActivityID") val studyActivityId: Int? = null,
    @SerialName("StudyActivityCod") val studyActivityCod: String,
    @SerialName("StudyActivityName") val studyActivityName: String,
    @SerialName("StudyActivityECTS") val studyActivityEcts: Double? = null,
    @SerialName("StudyActivityCompulsory") val studyActivityCompulsory: Boolean,
    @SerialName("StudyActivitySSD") val studyActivitySsd: List<String> = emptyList(),
    @SerialName("StudyActivitySemester") val studyActivitySemester: List<String> = emptyList(),
    @SerialName("StudyActivityTeachingUnitType") val studyActivityTeachingUnitType: String? = null,
    @SerialName("AfSubModules") val afSubModules: List<SubModule> = emptyList()
)

@Serializable
data class SubModule(
    @SerialName("StudyActivityID") val studyActivityId: Int? = null,
    @SerialName("StudyActivityCod") val studyActivityCod: String,
    @SerialName("StudyActivityName") val studyActivityName: String,
    @SerialName("studyActivityPartitionCod") val studyActivityPartitionCod: String? = null,
    @SerialName("studyActivityPartitionDes") val studyActivityPartitionDes: String? = null,
    @SerialName("StudyActivitySemester") val studyActivitySemester: String? = null,
    @SerialName("StudyActivitySettCod") val studyActivitySettCod: String? = null,
    @SerialName("StudyActivityCreditValue") val studyActivityCreditValue: Double? = null,
    @SerialName("StudyActivityScope") val studyActivityScope: String? = null,
    @SerialName("StudyActivityType") val studyActivityType: String? = null
)