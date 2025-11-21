package it.appm.myunical.ui.screen.login2

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import it.appm.myunical.R
import it.appm.myunical.data.MyUnical
import it.appm.myunical.data.cineca.LoginState
import it.appm.myunical.ui.theme.UnicalRed
import it.unical.esse3.auth.model.ChiaviTrattoCarriera

@Composable
fun LoginScreen2(){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UnicalRed),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.fillMaxWidth().fillMaxHeight(0.5f)){
            AppLogo(Modifier.align(Alignment.Center))
        }

        LoginBottomScreen()

    }
}

@Composable
private fun LoginBottomScreen(){
    val viewModel : LoginViewModel = viewModel()
    when(val state = viewModel.state.collectAsStateWithLifecycle().value){
        LoginViewModel.State.Form -> LoginForm(viewModel::sendCredentials)
        LoginViewModel.State.Loading -> LoadingBox()
        LoginViewModel.State.Error -> ErrorBox(R.string.error, viewModel::openForm)
        is LoginViewModel.State.SelectCareer -> CareerSelection(state.list, viewModel::selectCareer)
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
private fun LoginForm(send: (String, String) -> Unit){
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
                onClick = { send(codiceFiscale, password) },
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
private fun CareerSelection(list: List<ChiaviTrattoCarriera>, select: (Long)-> Unit){
    Surface(
        modifier = Modifier.padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 8.dp,
    ) {
        LazyColumn(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            item {
                Text(
                    text = stringResource(R.string.select_career),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                Spacer(Modifier.height(8.dp))
            }
            items(list){
                Column(Modifier.padding(vertical = 12.dp).fillMaxWidth().clickable {
                    select(it.matId)
                }) {
                    Text(it.matricola, fontWeight = FontWeight.Bold)
                    Text(it.cdsDes)
                    Text(it.staStuDes)
                }
            }
        }
    }
}


@Composable
private fun ErrorBox(@StringRes message: Int, onAccept: () -> Unit){
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
                onClick = onAccept,
                colors = ButtonDefaults.filledTonalButtonColors(),
                content =  { Text(stringResource(R.string.ok)) }
            )
        }
    }
}