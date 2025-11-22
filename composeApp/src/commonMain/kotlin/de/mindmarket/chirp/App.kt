package de.mindmarket.chirp

import androidx.compose.runtime.Composable
import de.mindmarket.auth.presentation.register_success.RegisterSuccessRoot
import de.mindmarket.chirp.navigation.NavigationRoot
import de.mindmarket.core.designsystem.theme.ChirpTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    ChirpTheme {
        NavigationRoot()
    }
}