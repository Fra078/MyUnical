package it.fra.myunical.datastore.models

import kotlinx.serialization.Serializable

@Serializable
data class ScheduleProviderDataModel(
    val id: String,
    val displayName: String,
    val cdsCode: String,
    val regulationId: Int,
    val degreeName: String,
    val studyPathId: Int,
    val studyPathName: String,
    val courses: List<TrackedCourseDataModel>
)

@Serializable
data class TrackedCourseDataModel(
    val code: String,
    val name: String,
    val yearNumber: Int,
    val moduleCodes: List<String> = emptyList()
)
