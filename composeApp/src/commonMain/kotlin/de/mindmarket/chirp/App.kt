package de.mindmarket.chirp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import de.mindmarket.auth.presentation.register_success.RegisterSuccessRoot
import de.mindmarket.chirp.navigation.DeepLinkListener
import de.mindmarket.chirp.navigation.NavigationRoot
import de.mindmarket.core.designsystem.theme.ChirpTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val navController = rememberNavController()
    DeepLinkListener(navController)

    ChirpTheme {
        NavigationRoot(navController)
    }
}