package de.mindmarket.auth.presentation.reset_password

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chirp.feature.auth.presentation.generated.resources.Res
import chirp.feature.auth.presentation.generated.resources.error_reset_password_link_expired_or_not_valid
import chirp.feature.auth.presentation.generated.resources.password
import chirp.feature.auth.presentation.generated.resources.password_hint
import chirp.feature.auth.presentation.generated.resources.reset_password_successfully
import chirp.feature.auth.presentation.generated.resources.set_new_password
import chirp.feature.auth.presentation.generated.resources.submit
import de.mindmarket.core.designsystem.components.brand.ChirpBrandLogo
import de.mindmarket.core.designsystem.components.buttons.ChirpButton
import de.mindmarket.core.designsystem.components.layouts.ChirpAdaptiveFormLayout
import de.mindmarket.core.designsystem.components.textfields.ChirpPasswordTextField
import de.mindmarket.core.designsystem.theme.ChirpTheme
import de.mindmarket.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ResetPasswordScreenRoot(
    viewModel: ResetPasswordViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ResetPasswordScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun ResetPasswordScreen(
    state: ResetPasswordState,
    onAction: (ResetPasswordAction) -> Unit,
    modifier: Modifier = Modifier
) {

    ChirpAdaptiveFormLayout(
        headerText = stringResource(Res.string.set_new_password),
        errorText = state.errorText?.asString(),
        logo = {
            ChirpBrandLogo()
        }
    ) {
        ChirpPasswordTextField(
            state = state.passwordTextState,
            modifier = Modifier
                .fillMaxWidth(),
            placeholder = stringResource(Res.string.password),
            title = stringResource(Res.string.password),
            supportingText = stringResource(Res.string.password_hint),
            isPasswordVisible = state.isPasswordVisible,
            onToggleVisibleClick = {
                onAction(ResetPasswordAction.OnTogglePasswordVisibilityClick)
            }
        )
        Spacer(Modifier.height(16.dp))
        ChirpButton(
            text = stringResource(Res.string.submit),
            onClick = {
                onAction(ResetPasswordAction.OnSubmitClick)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading && state.canSubmit,
            isLoading = state.isLoading
        )

        if (state.isResetSuccessful) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.reset_password_successfully),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.extended.success,
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
fun ResetPasswordScreenPreview() {
    ChirpTheme {
        ResetPasswordScreen(
            state = ResetPasswordState(),
            onAction = {}
        )
    }
}
