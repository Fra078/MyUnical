package it.appm.myunical

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.appm.myunical.ui.screen.main.MainScreen
import it.appm.myunical.ui.theme.AppTheme
import it.fra.myunical.domain.model.auth.AuthState
import it.fra.myunical.domain.usecase.GetAuthStateUseCase
import it.fra.myunical.login.presentation.credentials.LoginScreen
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val getAuthState: GetAuthStateUseCase by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val authStateFlow = getAuthState()
        enableEdgeToEdge()
        setContent {
            val authState by authStateFlow.collectAsStateWithLifecycle(initialValue = AuthState.Unknown)
            MyUnicalApp(authState)
        }
    }

    @Composable
    private fun MyUnicalApp(authState: AuthState){
        AppTheme {
            when(authState){
                AuthState.Authenticated -> MainScreen({})
                AuthState.Unauthenticated -> LoginScreen()
                AuthState.Unknown -> Unit
            }
        }
    }

}