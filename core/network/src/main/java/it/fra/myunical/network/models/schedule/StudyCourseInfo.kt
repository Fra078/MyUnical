package it.fra.myunical.network.models.schedule

import it.fra.myunical.network.models.news.NewsEntry
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StudyCoursePaginationResponse(
    val count: Int,
    val next: String? = null,
    val previous: String? = null,
    @SerialName("page_number")val page: Int,
    @SerialName("total_pages") val totalPages: Int,
    val results: List<StudyCourseInfo>
)

@Serializable
data class StudyCourseInfo(
    @SerialName("RegDidId")
    val regulationId: Long,

    @SerialName("CdSId")
    val courseId: Long,

    @SerialName("CdSCod")
    val courseCode: String,

    @SerialName("AcademicYear")
    val academicYear: Int,

    @SerialName("AreaCds")
    val academicArea: String,

    @SerialName("CdSName")
    val courseName: String,

    @SerialName("DepartmentId")
    val departmentId: Long,

    @SerialName("DepartmentCod")
    val departmentCode: String,

    @SerialName("DepartmentName")
    val departmentName: String,

    @SerialName("CourseType")
    val courseType: String,

    @SerialName("CourseTypeDescription")
    val courseTypeDescription: String,

    @SerialName("CourseClassCod")
    val courseClassCode: String,

    @SerialName("CourseClassName")
    val courseClassName: String,

    @SerialName("CourseInterClassCod")
    val interClassCode: String? = null,

    @SerialName("CourseInterClassDes")
    val interClassDescription: String? = null,

    @SerialName("ErogationMode")
    val deliveryMode: String? = null,

    @SerialName("CdSLanguage")
    val languages: List<String> = emptyList(),

    @SerialName("CdSDuration")
    val durationYears: Int,

    @SerialName("CdSECTS")
    val ectsCredits: Int,

    @SerialName("CdSAttendance")
    val attendance: String? = null,

    @SerialName("RegDidState")
    val regulationState: String,

    @SerialName("JointDegree")
    val jointDegree: String,

    @SerialName("StudyManifesto")
    val studyManifestoUrl: String? = null,

    @SerialName("DidacticRegulation")
    val didacticRegulationUrl: String? = null,

    @SerialName("TeachingSystem")
    val teachingSystemUrl: String? = null,

    @SerialName("TeachingSystemYear")
    val teachingSystemYear: Int? = null
)