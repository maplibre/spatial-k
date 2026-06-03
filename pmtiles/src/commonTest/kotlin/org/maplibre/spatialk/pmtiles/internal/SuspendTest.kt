package org.maplibre.spatialk.pmtiles.internal

import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

internal fun runSuspending(block: suspend () -> Unit) {
    var completed = false
    var failure: Throwable? = null

    block.startCoroutine(
        object : Continuation<Unit> {
            override val context = EmptyCoroutineContext

            override fun resumeWith(result: Result<Unit>) {
                completed = true
                failure = result.exceptionOrNull()
            }
        }
    )

    check(completed) { "Test coroutine suspended unexpectedly." }
    failure?.let { throw it }
}
