package it.fra.myunical.today.models

data class ProfileUiState(
    val fullName: String,
    val matricola: String,
    val courseName: String,
    val avatarUrl: String?,
    val careers: List<ProfileCareer>
)

data class ProfileCareer(
    val matId: Int,
    val name: String,
    val status: String,
    val matricola: String,
    val isSelected: Boolean
)
