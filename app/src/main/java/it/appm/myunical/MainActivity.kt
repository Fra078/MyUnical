package it.appm.myunical

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.appm.myunical.data.MyUnical
import it.appm.myunical.data2.MyUnical2
import it.appm.myunical.data.cineca.LoginState
import it.appm.myunical.ui.screen.homescreen.HomeScreen
import it.appm.myunical.ui.screen.login.LoginScreen
import it.appm.myunical.ui.screen.login2.LoginScreen2
import it.appm.myunical.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MyUnicalApp() }
    }

    @Composable
    private fun MyUnicalApp(){
        /*val state by MyUnical.state.collectAsStateWithLifecycle()
        AppTheme {
            if (state == LoginState.READY)
                HomeScreen()
            else
                LoginScreen(state)
        }*/

        val state by MyUnical2.state.collectAsStateWithLifecycle()
        AppTheme {
            if (state is MyUnical2.State.Ready){
                //TODO
            } else {
                LoginScreen2()
            }

        }
    }

}