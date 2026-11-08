package it.fra.myunical.login.presentation.credentials

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.fra.myunical.login.R
import it.fra.myunical.login.models.AcademicCareer
import it.fra.myunical.login.models.LoginError
import it.fra.myunical.login.models.LoginStep
import it.fra.myunical.login.models.LoginUiState
import it.fra.myunical.ui.components.ShapeBadge
import it.fra.myunical.ui.components.rememberGroupedItemShape
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(viewModel: LoginViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler(enabled = state.step == LoginStep.CAREER && !state.isLoading, onBack = viewModel::backToCredentials)

    LoginScreen(
        state = state,
        onFiscalCodeChanged = viewModel::updateFiscalCode,
        onPasswordChanged = viewModel::updatePassword,
        onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
        onSubmit = viewModel::submitLogin,
        onCareerSelected = viewModel::selectCareer,
        onConfirmCareer = viewModel::confirmCareerSelection,
        onBackToCredentials = viewModel::backToCredentials
    )
}

@Composable
fun LoginScreen(
    state: LoginUiState,
    onFiscalCodeChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    onCareerSelected: (Int) -> Unit,
    onConfirmCareer: () -> Unit,
    onBackToCredentials: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BrandHeader()
            Spacer(Modifier.height(32.dp))
            AnimatedContent(
                targetState = state.step,
                transitionSpec = {
                    val forward = targetState == LoginStep.CAREER
                    (fadeIn() + slideInHorizontally { if (forward) it / 4 else -it / 4 }) togetherWith
                        (fadeOut() + slideOutHorizontally { if (forward) -it / 4 else it / 4 })
                },
                label = "loginStep"
            ) { step ->
                when (step) {
                    LoginStep.CREDENTIALS -> CredentialsStep(
                        state, onFiscalCodeChanged, onPasswordChanged, onTogglePasswordVisibility, onSubmit
                    )
                    LoginStep.CAREER -> CareerStep(state, onCareerSelected, onConfirmCareer, onBackToCredentials)
                }
            }
        }
    }
}

@Composable
private fun BrandHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ShapeBadge(
            size = 88.dp,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(painterResource(R.drawable.school), contentDescription = null, modifier = Modifier.size(44.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "MyUnical",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            "Università della Calabria",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CredentialsStep(
    state: LoginUiState,
    onFiscalCodeChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val submit = {
        focusManager.clearFocus()
        onSubmit()
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp),) {
        Text(
            "Accedi con le credenziali Esse3",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            "Le credenziali restano sul telefono e servono solo a leggere i tuoi dati dal portale.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))

        OutlinedTextField(
            value = state.fiscalCode,
            onValueChange = onFiscalCodeChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Codice fiscale") },
            leadingIcon = { Icon(painterResource(R.drawable.badge), contentDescription = null) },
            singleLine = true,
            isError = state.error == LoginError.INVALID_CREDENTIALS,
            shape = MaterialTheme.shapes.large,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Next
            )
        )
        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            leadingIcon = { Icon(painterResource(R.drawable.lock), contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(
                        painterResource(if (state.isPasswordVisible) R.drawable.visibility_off else R.drawable.visibility),
                        contentDescription = if (state.isPasswordVisible) "Nascondi password" else "Mostra password"
                    )
                }
            },
            visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            isError = state.error == LoginError.INVALID_CREDENTIALS,
            shape = MaterialTheme.shapes.large,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (state.canSubmit) submit() })
        )

        AnimatedVisibility(state.error != null) {
            state.error?.let { ErrorBanner(it.message()) }
        }

        Spacer(Modifier.height(4.dp))
        PrimaryAction("Accedi", state.canSubmit, state.isLoading, submit)
    }
}

@Composable
private fun CareerStep(
    state: LoginUiState,
    onCareerSelected: (Int) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Scegli la carriera", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(
            "Il tuo account ha più carriere. Potrai cambiarla in qualsiasi momento dal profilo.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            state.careers.forEachIndexed { index, career ->
                CareerRow(career, index, state.careers.size, career.matId == state.selectedCareerId) {
                    onCareerSelected(career.matId)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        PrimaryAction("Continua", state.selectedCareerId != null && !state.isLoading, state.isLoading, onConfirm)
        TextButton(onClick = onBack, enabled = !state.isLoading, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Usa un altro account")
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CareerRow(career: AcademicCareer, index: Int, count: Int, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = rememberGroupedItemShape(index, count),
        color = if (selected) colors.primaryContainer else colors.surfaceContainer,
        contentColor = if (selected) colors.onPrimaryContainer else colors.onSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            ShapeBadge(
                size = 44.dp,
                polygon = if (selected) MaterialShapes.Cookie9Sided else MaterialShapes.Circle,
                containerColor = if (selected) colors.primary else colors.secondaryContainer,
                contentColor = if (selected) colors.onPrimary else colors.onSecondaryContainer
            ) {
                Icon(
                    painterResource(if (selected) R.drawable.check_circle else R.drawable.school),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(career.degreeTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "Matricola ${career.matricola} · ${career.status}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PrimaryAction(text: String, enabled: Boolean, loading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ButtonDefaults.MediumContainerHeight),
        contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight)
    ) {
        if (loading) {
            LoadingIndicator(Modifier.size(28.dp), color = MaterialTheme.colorScheme.onPrimary)
        } else {
            Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(R.drawable.error), contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Start)
        }
    }
}

private fun LoginError.message() = when (this) {
    LoginError.INVALID_CREDENTIALS -> "Codice fiscale o password non corretti."
    LoginError.NETWORK -> "Impossibile raggiungere il portale. Controlla la connessione e riprova."
    LoginError.SERVER -> "Il portale non risponde come previsto. Riprova tra qualche minuto."
}

@Preview(showBackground = true)
@Composable
private fun CredentialsPreview() {
    MaterialTheme {
        LoginScreen(
            LoginUiState(fiscalCode = "RSSMRA00A01H501U", password = "x", error = LoginError.INVALID_CREDENTIALS),
            {}, {}, {}, {}, {}, {}, {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CareerPreview() {
    MaterialTheme {
        LoginScreen(
            LoginUiState(
                step = LoginStep.CAREER,
                careers = listOf(
                    AcademicCareer(1, "Ingegneria Informatica", "234567", "Attiva", true),
                    AcademicCareer(2, "Scienze dei Materiali", "189234", "Titolo conseguito", false)
                ),
                selectedCareerId = 1
            ),
            {}, {}, {}, {}, {}, {}, {}
        )
    }
}
