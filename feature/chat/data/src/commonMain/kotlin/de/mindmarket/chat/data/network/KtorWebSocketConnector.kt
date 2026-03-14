package de.mindmarket.chat.data.network

import de.mindmarket.chat.data.dto.websocket.WebSocketMessageDto
import de.mindmarket.chat.data.lifecycle.AppLifecycleObserver
import de.mindmarket.chat.domain.error.ConnectionError
import de.mindmarket.chat.domain.models.ConnectionState
import de.mindmarket.core.data.networking.UrlConstants
import de.mindmarket.core.domain.auth.SessionStorage
import de.mindmarket.core.domain.logging.ChirpLogger
import de.mindmarket.core.domain.util.EmptyResult
import de.mindmarket.core.domain.util.Result
import de.mindmarket.feature.chat.data.BuildKonfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.header
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration.Companion.seconds

class KtorWebSocketConnector(
    private val httpClient: HttpClient,
    private val applicationScope: CoroutineScope,
    private val sessionStorage: SessionStorage,
    private val json: Json,
    private val connectionErrorHandler: ConnectionErrorHandler,
    private val connectionRetryHandler: ConnectionRetryHandler,
    private val appLifecycleObserver: AppLifecycleObserver,
    private val connectivityObserver: ConnectivityObserver,
    private val logger: ChirpLogger
) {
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState = _connectionState.asStateFlow()

    private var currentSession: WebSocketSession? = null

    @OptIn(FlowPreview::class)
    private val isConnected = connectivityObserver
        .isConnected
        .debounce(1.seconds)
        .stateIn(
            applicationScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = false
        )

    private val isInForeground = appLifecycleObserver
        .isInForeground
        .onEach { isInForeground ->
            if (isInForeground) {
                connectionRetryHandler.resetDelay()
            }
        }
        .stateIn(
            applicationScope,
            SharingStarted.WhileSubscribed(5000),
            false
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages = combine(
        sessionStorage.observeAuthInfo(),
        isConnected,
        isInForeground
    ) { authInfo, isConnected, isInForeground ->
        when {
            authInfo == null -> {
                logger.info("No authentication details. Clearing session and disconnecting..")
                _connectionState.value = ConnectionState.DISCONNECTED
                currentSession?.close()
                currentSession = null
                connectionRetryHandler.resetDelay()
                null
            }

            !isInForeground -> {
                logger.info("App in background, disconnecting socket proactively.")
                _connectionState.value = ConnectionState.DISCONNECTED
                currentSession?.close()
                currentSession = null
                null
            }

            !isConnected -> {
                logger.info("Device is disconnected. Closing webSocket connection")
                _connectionState.value = ConnectionState.ERROR_NETWORK
                currentSession?.close()
                currentSession = null
                null
            }

            else -> {
                logger.info("App in foreground and connected. Start webSocket connection")

                if (_connectionState.value !in listOf(
                        ConnectionState.CONNECTING,
                        ConnectionState.CONNECTED
                    )
                ) {
                    _connectionState.value = ConnectionState.CONNECTING
                }

                authInfo
            }
        }
    }.flatMapLatest { authInfo ->
        if (authInfo == null) {
            emptyFlow()
        } else {
            createWebSocketFlow(authInfo.accessToken)
                .catch { exception ->
                    // catch transform exceptions for platform compatibility
                    logger.error("Exception in WebSocket, $exception")
                    currentSession?.close()
                    currentSession = null

                    val transformedException = connectionErrorHandler.transformException(exception)
                    throw transformedException
                }
                .retryWhen { cause, attempt ->
                    logger.info("Connection failed on attempt $attempt")
                    val shouldRetry = connectionRetryHandler.shouldRetry(cause, attempt)

                    if (shouldRetry) {
                        _connectionState.value = ConnectionState.CONNECTING
                        connectionRetryHandler.applyRetryDelay(attempt)
                    }

                    shouldRetry
                }
                .catch { exception ->
                    // catch non retriable errors
                    logger.error("Unhandled WebSocket error. $exception")
                    _connectionState.value =
                        connectionErrorHandler.getConnectionStateForError(exception)
                }
        }
    }

    private fun createWebSocketFlow(accessToken: String) = callbackFlow {
        _connectionState.value = ConnectionState.CONNECTING
        currentSession = httpClient.webSocketSession(
            urlString = "${UrlConstants.BASE_URL_WS}/chat"
        ) {
            header("Authorization", "Bearer $accessToken")
            header("X-API-Key", BuildKonfig.API_KEY)
        }

        currentSession?.let { session ->
            _connectionState.value = ConnectionState.CONNECTED

            session
                .incoming
                .consumeAsFlow()
                .buffer(
                    capacity = 100
                )
                .collect { frame ->
                    when (frame) {
                        is Frame.Ping -> {
                            logger.debug("Received ping from server. Sending pong...")
                            session.send(Frame.Pong(frame.data))
                        }

                        is Frame.Text -> {
                            val text = frame.readText()
                            logger.info("Received raw text frame: $text")

                            val messageDto = json.decodeFromString<WebSocketMessageDto>(text)
                            send(messageDto)
                        }

                        else -> Unit
                    }
                }
        } ?: throw Exception("Failed to establish WebSocket connection")

        awaitClose {
            launch(NonCancellable) {
                logger.info("disconnecting from WebSocket session..")
                _connectionState.value = ConnectionState.DISCONNECTED
                currentSession?.close()
                currentSession = null
            }
        }
    }

    suspend fun sendMessage(message: String): EmptyResult<ConnectionError> {
        val connectionState = connectionState.value

        if (currentSession == null || connectionState != ConnectionState.CONNECTED) {
            return Result.Failure(ConnectionError.NOT_CONNECTED)
        }

        return try {
            currentSession?.send(message)
            Result.Success(Unit)
        } catch (exception: Exception) {
            coroutineContext.ensureActive()
            logger.error("Unable to send webSocket message, $exception")
            Result.Failure(ConnectionError.MESSAGE_SENT_FAILED)
        }
    }
}