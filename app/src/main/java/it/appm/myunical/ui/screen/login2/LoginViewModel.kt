package it.appm.myunical.ui.screen.login2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.appm.myunical.data2.MyUnical2
import it.appm.myunical.data2.login.LoginManager
import it.unical.esse3.auth.model.ChiaviTrattoCarriera
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _state : MutableStateFlow<State> = MutableStateFlow(State.Form)
    val state = _state.asStateFlow()


    fun sendCredentials(username: String, password: String){
        _state.value = State.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val session = LoginManager.login(username, password)
            val careers = session?.user?.trattiCarriera
            if (careers.isNullOrEmpty())
                _state.value = State.Error
            else
                _state.value = State.SelectCareer(username, password, careers)
        }
    }

    fun selectCareer(matId: Long){
        val state = _state.value
        if (state !is State.SelectCareer)
            _state.value = State.Error
        else
            MyUnical2.selectSession(state.cf, state.password, matId)
    }

    fun openForm(){
        _state.value = State.Form
    }

    sealed interface State {
        data object Form : State
        data object Loading : State
        data class SelectCareer(val cf: String, val password: String, val list: List<ChiaviTrattoCarriera>): State
        data object Error : State
    }

}