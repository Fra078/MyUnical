package it.appm.myunical.data2

import it.appm.myunical.data2.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object MyUnical2 {
    private val _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    init {
        _state.value = State.Login
    }

    fun selectSession(cf: String, password: String, matId: Long){
        _state.value = State.Ready(Session(cf, password, matId))
    }

    sealed interface State {
        data object Loading : State
        data object Login : State
        data class Ready(val session: Session) : State
    }

}