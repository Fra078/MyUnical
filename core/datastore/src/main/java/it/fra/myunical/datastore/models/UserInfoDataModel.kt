package it.fra.myunical.datastore.models

import kotlinx.serialization.Serializable

@Serializable
data class UserInfoDataModel(
    val username: String,
    val firstName: String,
    val lastName: String,
    val persId: Int,
    val careers: List<CareerTrackDataModel>
)