package it.fra.myunical.login.presentation.credentials

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.fra.myunical.domain.model.auth.AuthResponseModel
import it.fra.myunical.domain.model.auth.UserCredentials
import it.fra.myunical.domain.usecase.LoginUseCase
import it.fra.myunical.domain.usecase.SaveCredentialsUseCase
import it.fra.myunical.login.models.AcademicCareer
import it.fra.myunical.login.models.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val login : LoginUseCase,
    private val saveCredentials: SaveCredentialsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state = _state.asStateFlow()


    fun updateFiscalCode(value: String){
        _state.update { it.copy(fiscalCode = value) }
    }

    fun updatePassword(value: String){
        _state.update { it.copy(password = value) }
    }

    fun togglePasswordVisibility(){
        _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun submitLogin(){
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val value = _state.value
            val response = login(UserCredentials(value.fiscalCode, value.password))

            if (response is AuthResponseModel.Success){
                _state.update { old -> old.copy(
                    isLoading = false,
                    availableCareers = response.content.careers.map {
                        AcademicCareer(
                            matId = it.matId,
                            degreeTitle = it.name,
                            matricola = it.matricola,
                            status = if (it.active) "Attiva" else "Chiusa - ${it.closedReason}")
                    }
                ) }
            } else if (response is AuthResponseModel.Failure){
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Credenziali non valide. Verifica il codice fiscale e la password inseriti. $response"
                    )
                }
            }

        }
    }

    fun selectCareer(matId: Int){
        _state.update { it.copy(selectedCareerId = matId) }
    }

    fun confirmCareerSelection(){
        viewModelScope.launch {
            val currentState = _state.value
            saveCredentials(UserCredentials(currentState.fiscalCode, currentState.password), currentState.selectedCareerId!!)
        }
    }

}