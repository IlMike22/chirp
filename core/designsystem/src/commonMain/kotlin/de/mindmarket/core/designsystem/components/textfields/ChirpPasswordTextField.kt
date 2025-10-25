package de.mindmarket.core.designsystem.components.textfields

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicSecureTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import chirp.core.designsystem.generated.resources.Res
import chirp.core.designsystem.generated.resources.eye_icon
import chirp.core.designsystem.generated.resources.eye_off_icon
import chirp.core.designsystem.generated.resources.hide_password
import chirp.core.designsystem.generated.resources.show_password
import de.mindmarket.core.designsystem.theme.ChirpTheme
import de.mindmarket.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ChirpPasswordTextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isPasswordVisible: Boolean,
    title: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    onFocusChanged: (Boolean) -> Unit = {},
    onToggleVisibleClick: () -> Unit,
) {
    ChirpTextFieldLayout(
        title = title,
        isError = isError,
        enabled = enabled,
        supportingText = supportingText,
        onFocusChanged = onFocusChanged,
        modifier = modifier
    ) { styleModifier, interactionSource ->
        BasicSecureTextField(
            state = state,
            modifier = styleModifier,
            enabled = enabled,
            textObfuscationMode = if (isPasswordVisible) {
                TextObfuscationMode.Visible
            } else TextObfuscationMode.Hidden,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.extended.textPlaceholder
                }
            ),
            interactionSource = interactionSource,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
            decorator = { innerBox ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (state.text.isEmpty() && placeholder != null) {
                            Text(
                                text = placeholder,
                                color = MaterialTheme.colorScheme.extended.textPlaceholder,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        innerBox()
                    }

                    Icon(
                        imageVector = if (isPasswordVisible) {
                            vectorResource(Res.drawable.eye_off_icon)
                        } else {
                            vectorResource(Res.drawable.eye_icon)
                        },
                        contentDescription = if (isPasswordVisible) {
                            stringResource(Res.string.hide_password)
                        } else {
                            stringResource(Res.string.show_password)
                        },
                        tint = MaterialTheme.colorScheme.extended.textDisabled,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(
                                    bounded = false,
                                    radius = 24.dp
                                ),
                                onClick = onToggleVisibleClick
                            )
                    )
                }
            }
        )
    }
}

@Composable
@Preview(
    showBackground = true
)
fun ChirpTextFieldPasswordEmptyPreview() {
    ChirpTheme {
        ChirpPasswordTextField(
            state = rememberTextFieldState(),
            modifier = Modifier
                .width(300.dp),
            placeholder = "Password",
            supportingText = "Use 9+ characters and more.",
            isError = false,
            title = "Email",
            isPasswordVisible = true,
            onToggleVisibleClick = {}
        )
    }
}

@Composable
@Preview(
    showBackground = true
)
fun ChirpPasswordFilledPreview() {
    ChirpTheme {
        ChirpPasswordTextField(
            state = rememberTextFieldState("password123"),
            modifier = Modifier
                .width(300.dp),
            placeholder = "Password",
            supportingText = "Use 9+ characters and more.",
            isError = false,
            title = "Password",
            isPasswordVisible = false,
            onToggleVisibleClick = {}
        )
    }
}

@Composable
@Preview(
    showBackground = true
)
fun ChirpPasswordErrorPreview() {
    ChirpTheme {
        ChirpPasswordTextField(
            state = rememberTextFieldState("password123"),
            modifier = Modifier
                .width(300.dp),
            placeholder = "Password",
            supportingText = "Doesnt match the conditions!",
            isError = true,
            title = "Password",
            isPasswordVisible = true,
            onToggleVisibleClick = {}
        )
    }
}