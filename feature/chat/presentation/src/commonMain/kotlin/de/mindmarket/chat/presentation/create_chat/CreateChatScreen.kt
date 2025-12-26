package de.mindmarket.chat.presentation.create_chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chirp.feature.chat.presentation.generated.resources.Res
import chirp.feature.chat.presentation.generated.resources.cancel
import chirp.feature.chat.presentation.generated.resources.create_chat
import de.mindmarket.chat.presentation.components.ChatParticipantSearchTextSection
import de.mindmarket.chat.presentation.components.ChatParticipantsSelectionSection
import de.mindmarket.chat.presentation.components.ManageChatButtonSection
import de.mindmarket.chat.presentation.components.ManageChatHeaderRow
import de.mindmarket.core.designsystem.components.brand.ChirpHorizontalDivider
import de.mindmarket.core.designsystem.components.buttons.ChirpButton
import de.mindmarket.core.designsystem.components.buttons.ChirpButtonStyle
import de.mindmarket.core.designsystem.components.dialogs.ChirpAdaptiveDialogSheetLayout
import de.mindmarket.core.designsystem.theme.ChirpTheme
import de.mindmarket.core.presentation.util.DeviceConfiguration
import de.mindmarket.core.presentation.util.clearFocusOnTap
import de.mindmarket.core.presentation.util.currentDeviceConfiguration
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateChatScreenRoot(
    viewModel: CreateChatViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ChirpAdaptiveDialogSheetLayout(
        onDismiss = {
            viewModel.onAction(CreateChatAction.OnDismissDialog)
        }
    ) {
        CreateChatScreen(
            state = state,
            onAction = viewModel::onAction
        )
    }
}

@Composable
fun CreateChatScreen(
    state: CreateChatState,
    onAction: (CreateChatAction) -> Unit
) {
    var isTextFieldFocused by remember { mutableStateOf(false) }
    val imeHeight = WindowInsets.ime.getBottom(LocalDensity.current)
    val isKeyboardVisible = imeHeight > 0
    val configuration = currentDeviceConfiguration()

    val shouldHideHeader = configuration == DeviceConfiguration.MOBILE_LANDSCAPE
            || (isKeyboardVisible && configuration != DeviceConfiguration.DESKTOP)
            || isTextFieldFocused

    Column(
        modifier = Modifier
            .clearFocusOnTap()
            .fillMaxWidth()
            .wrapContentHeight()
            .imePadding()
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
    ) {
        AnimatedVisibility(
            visible = !shouldHideHeader
        ) {
            Column {
                ManageChatHeaderRow(
                    title = stringResource(Res.string.create_chat),
                    onCloseClick = {
                        onAction(CreateChatAction.OnDismissDialog)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                ChirpHorizontalDivider()

                ChatParticipantSearchTextSection(
                    queryState = state.queryTextState,
                    onAddClick = {
                        onAction(CreateChatAction.OnAddClick)
                    },
                    isSearchEnabled = state.canAddParticipant,
                    isLoading = state.isAddingParticipant,
                    modifier = Modifier
                        .fillMaxWidth(),
                    error = state.searchError,
                    onFocusChanged = {
                        isTextFieldFocused = it
                    }
                )

                ChirpHorizontalDivider()

                ChatParticipantsSelectionSection(
                    selectedChatParticipants = state.selectedChatParticipants,
                    modifier = Modifier.fillMaxWidth(),
                    searchResult = state.currentSearchResult
                )

                ChirpHorizontalDivider()

                ManageChatButtonSection(
                    primaryButton = {
                        ChirpButton(
                            text = stringResource(Res.string.create_chat),
                            style = ChirpButtonStyle.PRIMARY,
                            onClick = {
                                onAction(CreateChatAction.OnCreateChatClick)
                            },
                            enabled = state.selectedChatParticipants.isNotEmpty(),
                            isLoading = state.isCreatingChat
                        )
                    },
                    secondaryButton = {
                        ChirpButton(
                            text = stringResource(Res.string.cancel),
                            style = ChirpButtonStyle.SECONDARY,
                            onClick = {
                                onAction(CreateChatAction.OnDismissDialog)
                            },
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
@Preview
fun CreateChatScreenPreview() {
    ChirpTheme {
        CreateChatScreen(
            state = CreateChatState(),
            onAction = {}
        )
    }
}
