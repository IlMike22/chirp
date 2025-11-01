package de.mindmarket.chirp

import androidx.compose.runtime.Composable
import de.mindmarket.auth.presentation.register.RegisterRoot
import de.mindmarket.core.designsystem.theme.ChirpTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    ChirpTheme {
        RegisterRoot()
    }
}