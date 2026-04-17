package de.mindmarket.core.domain.util

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

class Paginator<Key, Item>(
    private val initialKey: Key,
    private val onLoadUpdated: (Boolean) -> Unit,
    private val onRequest: suspend (nextKey: Key) -> Result<List<Item>, DataError>,
    private val getNextKey: suspend (List<Item>) -> Key,
    private val onError: suspend (Throwable?) -> Unit,
    private val onSuccess: suspend (items: List<Item>, newKey: Key) -> Unit
) {
    private var currentKey = initialKey
    private var isMakingRequest = false
    private var lastRequestKey: Key? = null

    suspend fun loadNextItems() {
        if (isMakingRequest) {
            return
        }

        if (currentKey != null && currentKey == lastRequestKey) {
            return
        }

        isMakingRequest = true
        lastRequestKey = currentKey
        onLoadUpdated(true)

        try {
            onRequest(currentKey)
                .onSuccess { items ->
                    val newKey = getNextKey(items)
                    onSuccess(items, newKey)
                    currentKey = newKey
                }
                .onFailure { error ->
                    onError(DataErrorException(error))
                }
        } catch (exception: Exception) {
            currentCoroutineContext().ensureActive()
            onError(exception)
        } finally {
            onLoadUpdated(false)
            isMakingRequest = false
        }
    }

    fun reset() {
        currentKey = initialKey
        lastRequestKey = null
    }
}