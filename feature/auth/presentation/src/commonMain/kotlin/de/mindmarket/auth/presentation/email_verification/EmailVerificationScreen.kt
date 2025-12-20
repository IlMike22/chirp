package de.mindmarket.auth.presentation.email_verification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chirp.feature.auth.presentation.generated.resources.Res
import chirp.feature.auth.presentation.generated.resources.close
import chirp.feature.auth.presentation.generated.resources.email_verified_failed
import chirp.feature.auth.presentation.generated.resources.email_verified_failed_description
import chirp.feature.auth.presentation.generated.resources.email_verified_successfully
import chirp.feature.auth.presentation.generated.resources.email_verified_successfully_description
import chirp.feature.auth.presentation.generated.resources.login
import chirp.feature.auth.presentation.generated.resources.verifying_account
import de.mindmarket.core.designsystem.components.brand.ChirpFailureIcon
import de.mindmarket.core.designsystem.components.brand.ChirpSuccessIcon
import de.mindmarket.core.designsystem.components.buttons.ChirpButton
import de.mindmarket.core.designsystem.components.buttons.ChirpButtonStyle
import de.mindmarket.core.designsystem.components.layouts.ChirpAdaptiveResultLayout
import de.mindmarket.core.designsystem.components.layouts.ChirpSimpleResultLayout
import de.mindmarket.core.designsystem.theme.ChirpTheme
import de.mindmarket.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EmailVerificationScreenRoot(
    viewModel: EmailVerificationViewModel = koinViewModel(),
    onLoginClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    EmailVerificationScreen(
        state = state,
        onAction = { action ->
            when (action) {
                EmailVerificationAction.OnLoginClick -> onLoginClick()
                EmailVerificationAction.OnCloseClick -> onCloseClick()
            }

            viewModel.onAction(action)
        }
    )
}

@Composable
fun EmailVerificationScreen(
    state: EmailVerificationState,
    onAction: (EmailVerificationAction) -> Unit
) {
    ChirpAdaptiveResultLayout {
        when {
            state.isVerifying -> {
                VerifyingContent(
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }

            state.isVerified -> {
                ChirpSimpleResultLayout(
                    title = stringResource(Res.string.email_verified_successfully),
                    description = stringResource(Res.string.email_verified_successfully_description),
                    icon = {
                        ChirpSuccessIcon()
                    },
                    primaryButton = {
                        ChirpButton(
                            text = stringResource(Res.string.login),
                            onClick = { onAction(EmailVerificationAction.OnLoginClick) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                )
            }

            else -> {
                ChirpSimpleResultLayout(
                    title = stringResource(Res.string.email_verified_failed),
                    description = stringResource(Res.string.email_verified_failed_description),
                    icon = {
                        Spacer(Modifier.height(32.dp))
                        ChirpFailureIcon(
                            modifier = Modifier
                                .size(80.dp)
                        )
                        Spacer(Modifier.height(32.dp))
                    },
                    primaryButton = {
                        ChirpButton(
                            text = stringResource(Res.string.close),
                            onClick = { onAction(EmailVerificationAction.OnCloseClick) },
                            modifier = Modifier.fillMaxWidth(),
                            style = ChirpButtonStyle.SECONDARY
                        )
                    }
                )
            }
        }
    }

}

@Composable
private fun VerifyingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .heightIn(min = 200.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(64.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(Res.string.verifying_account),
            color = MaterialTheme.colorScheme.extended.textSecondary,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Preview
@Composable
fun EmailVerificationScreenErrorPreview() {
    ChirpTheme {
        EmailVerificationScreen(
            state = EmailVerificationState(
                isVerifying = false,
                isVerified = false
            ),
            onAction = {}
        )
    }
}

@Preview
@Composable
fun EmailVerificationScreenSuccessPreview() {
    ChirpTheme {
        EmailVerificationScreen(
            state = EmailVerificationState(
                isVerifying = false,
                isVerified = true
            ),
            onAction = {}
        )
    }
}

@Preview
@Composable
fun EmailVerificationScreenVerifyingPreview() {
    ChirpTheme {
        EmailVerificationScreen(
            state = EmailVerificationState(
                isVerifying = true,
                isVerified = false
            ),
            onAction = {}
        )
    }
}
