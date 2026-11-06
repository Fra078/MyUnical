package it.fra.myunical.login.models


data class LoginUiState(
    val fiscalCode: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val availableCareers: List<AcademicCareer> = emptyList(),
    val selectedCareerId: Int? = null
)