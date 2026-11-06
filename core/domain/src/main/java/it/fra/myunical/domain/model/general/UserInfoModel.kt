package it.fra.myunical.domain.model.general

data class UserInfoModel(
    val username: String,
    val firstName: String,
    val lastName: String,
    val persId: Int,
    val careers: List<CareerTrackModel>
)