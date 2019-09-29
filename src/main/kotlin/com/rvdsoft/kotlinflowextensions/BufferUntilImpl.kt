package com.rvdsoft.kotlinflowextensions

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicBoolean

@ExperimentalCoroutinesApi
/**
 * buffer until [predicate] returns **true**. The elment that returns true will go first.
 */
suspend fun <T> Flow<T>.bufferUntil(
    predicate: suspend (T) -> Boolean
) = bufferUntil(false, predicate)


@ExperimentalCoroutinesApi
/**
 * buffer until [predicate] returns **true**
 */
suspend fun <T> Flow<T>.bufferUntilKeepOrder(
    predicate: suspend (T) -> Boolean
) = bufferUntil(true, predicate)


private suspend fun <T> Flow<T>.bufferUntil(
    keepOrder: Boolean,
    predicate: suspend (T) -> Boolean
) = coroutineScope {
    val buffer = ArrayList<T>()
    val predicateTrue = AtomicBoolean()
    val mutex = Mutex()
    flow {
        collect {
            when {
                predicateTrue.get() -> {
                    emit(it)
                }
                predicate(it) -> {
                    if (keepOrder) {
                        predicateTrue.set(true)
                        buffer += it
                    } else {
                        emit(it)
                        predicateTrue.set(true)
                    }
                    mutex.withLock {
                        buffer.forEach { elem -> emit(elem) }
                        buffer.clear()
                    }
                }
                else -> {
                    mutex.withLock {
                        buffer += it
                    }
                }
            }
        }
    }
}
