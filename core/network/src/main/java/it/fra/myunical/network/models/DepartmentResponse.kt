package it.fra.myunical.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DepartmentPageResponse(
    val next: String? = null,
    val results: List<DepartmentDto> = emptyList()
)

@Serializable
data class DepartmentDto(
    @SerialName("DepartmentCod") val code: String,
    @SerialName("DepartmentName") val name: String,
    @SerialName("DepartmentURL") val url: String? = null
)
