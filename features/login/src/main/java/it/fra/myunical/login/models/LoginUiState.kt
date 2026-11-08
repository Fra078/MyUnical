package it.fra.myunical.login.models

enum class LoginStep { CREDENTIALS, CAREER }

/** Why a sign-in failed; the screen turns it into text. */
enum class LoginError { INVALID_CREDENTIALS, NETWORK, SERVER }

data class LoginUiState(
    val step: LoginStep = LoginStep.CREDENTIALS,
    val fiscalCode: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: LoginError? = null,
    val careers: List<AcademicCareer> = emptyList(),
    val selectedCareerId: Int? = null
) {
    val canSubmit: Boolean get() = !isLoading && fiscalCode.isNotEmpty() && password.isNotEmpty()
}
