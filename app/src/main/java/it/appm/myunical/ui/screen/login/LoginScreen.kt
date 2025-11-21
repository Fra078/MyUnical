package it.appm.myunical.ui.screen.login

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import it.appm.myunical.R
import it.appm.myunical.data.MyUnical
import it.appm.myunical.data.cineca.LoginState
import it.appm.myunical.ui.theme.UnicalRed

@Composable
fun LoginScreen(loginState: LoginState){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UnicalRed),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.fillMaxWidth().fillMaxHeight(0.5f)){
            AppLogo(Modifier.align(Alignment.Center))
        }
        when(loginState){
            LoginState.LOADING -> LoadingBox()
            LoginState.WAIT_CREDENTIALS -> LoginForm()
            LoginState.LOGGING_IN -> LoadingBox()
            LoginState.LOGIN_FAILED_CAUSE_NO_CONNECTION -> ErrorBox(R.string.error_no_connection)
            LoginState.LOGIN_FAILED_CAUSE_WRONG_CREDENTIALS -> ErrorBox(R.string.error_wrong_credentials)
            LoginState.LOGIN_FAILED_CAUSE_CHANGE_PASSWORD -> ErrorBox(R.string.error_change_password)
            else -> {/*Impossibile*/}
        }
    }
}

@Composable
fun AppLogo(modifier: Modifier = Modifier){
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(R.drawable.unical_logo),
            contentDescription = null,
            modifier = Modifier.size(128.dp)
        )
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displayLarge,
            maxLines = 1,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            color = Color.White
        )

    }
}

@Composable
private fun LoginForm(){
    var codiceFiscale by remember { mutableStateOf("RSSMRA00A01H501U") }
    var password by remember { mutableStateOf("password") }

    Surface(
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp,
    ) {
        Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.put_credentials),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.size(8.dp))
            OutlinedTextField(
                value = codiceFiscale,
                onValueChange = { codiceFiscale = it},
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge,
                label = { Text(stringResource(R.string.codice_fiscale)) }
            )
            Spacer(Modifier.size(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge,
                label = { Text(stringResource(R.string.password)) }
            )
            Spacer(Modifier.size(16.dp))
            Button (
                onClick = { MyUnical.login(codiceFiscale, password) },
                enabled = codiceFiscale.isNotBlank() && password.isNotBlank(),
                colors = ButtonDefaults.filledTonalButtonColors(),
                content =  { Text(stringResource(R.string.login)) }
            )
        }
    }
}

@Composable
private fun LoadingBox(){
    Box(Modifier.fillMaxSize()){
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center),
            color = Color.White
        )
    }
}


@Composable
private fun ErrorBox(@StringRes message: Int){
    Surface(
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp,
    ) {
        Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.error),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.size(16.dp))
            Text(stringResource(message))
            Spacer(Modifier.size(16.dp))
            Button (
                onClick = { MyUnical.backToLogin() },
                colors = ButtonDefaults.filledTonalButtonColors(),
                content =  { Text(stringResource(R.string.ok)) }
            )
        }
    }
}