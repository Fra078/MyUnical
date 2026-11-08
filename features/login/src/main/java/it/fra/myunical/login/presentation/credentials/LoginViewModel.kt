package it.fra.myunical.login.presentation.credentials

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.fra.myunical.domain.model.auth.AuthError
import it.fra.myunical.domain.model.auth.AuthResponseModel
import it.fra.myunical.domain.model.auth.UserCredentials
import it.fra.myunical.domain.model.general.CareerTrackModel
import it.fra.myunical.domain.model.general.statusLabel
import it.fra.myunical.domain.usecase.LoginUseCase
import it.fra.myunical.domain.usecase.SaveCredentialsUseCase
import it.fra.myunical.login.models.AcademicCareer
import it.fra.myunical.login.models.LoginError
import it.fra.myunical.login.models.LoginStep
import it.fra.myunical.login.models.LoginUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val login: LoginUseCase,
    private val saveCredentials: SaveCredentialsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun updateFiscalCode(value: String) = _state.update { it.copy(fiscalCode = value.uppercase(), error = null) }

    fun updatePassword(value: String) = _state.update { it.copy(password = value, error = null) }

    fun togglePasswordVisibility() = _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun selectCareer(matId: Int) = _state.update { it.copy(selectedCareerId = matId) }

    /** From the career choice back to the credentials. */
    fun backToCredentials() = _state.update {
        it.copy(step = LoginStep.CREDENTIALS, careers = emptyList(), selectedCareerId = null, error = null)
    }

    fun submitLogin() {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val response = try {
                login(UserCredentials(current.fiscalCode, current.password))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AuthResponseModel.Failure(AuthError.Unknown(e))
            }
            when (response) {
                is AuthResponseModel.Failure -> _state.update { it.copy(isLoading = false, error = response.error.toLoginError()) }
                is AuthResponseModel.Success -> onSignedIn(response.content.careers)
            }
        }
    }

    fun confirmCareerSelection() {
        val matId = _state.value.selectedCareerId ?: return
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch { save(matId) }
    }

    private suspend fun onSignedIn(careers: List<CareerTrackModel>) {
        // Nothing to choose: go straight in with the only career (or the only active one)
        val only = careers.singleOrNull() ?: careers.singleOrNull { it.active }
        if (only != null) {
            save(only.matId)
            return
        }
        val items = careers.sortedByDescending { it.active }.map {
            AcademicCareer(it.matId, it.name, it.matricola, it.statusLabel, it.active)
        }
        _state.update {
            it.copy(isLoading = false, step = LoginStep.CAREER, careers = items, selectedCareerId = null)
        }
    }

    private suspend fun save(matId: Int) {
        val state = _state.value
        saveCredentials(UserCredentials(state.fiscalCode.trim(), state.password.trim()), matId)
        // Saving the credentials moves the app to the signed-in screens: nothing else to update
    }

    private fun AuthError.toLoginError() = when (this) {
        AuthError.InvalidCredentials -> LoginError.INVALID_CREDENTIALS
        AuthError.NetworkUnavailable -> LoginError.NETWORK
        is AuthError.ServerError, is AuthError.Unknown -> LoginError.SERVER
    }
}
