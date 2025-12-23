package de.mindmarket.core.designsystem.components.dialogs

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import de.mindmarket.core.designsystem.theme.ChirpTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChirpBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier =Modifier,
    content: @Composable () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    LaunchedEffect(sheetState.isVisible) {
        if (sheetState.isVisible) {
            sheetState.expand()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = null,
        contentWindowInsets = { WindowInsets()},
        modifier = modifier
            .statusBarsPadding()
    ) {
        content()
    }
}

@Composable
@Preview
fun ChirpBottomSheetPreview() {
    ChirpTheme {
        ChirpBottomSheet(
            onDismiss = {},
            content = {}
        )
    }
}