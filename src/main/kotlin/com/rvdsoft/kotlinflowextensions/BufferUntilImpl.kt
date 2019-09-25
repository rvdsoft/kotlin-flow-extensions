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
 * buffer until [predicate] returns **true** with that element at the first position
 */
suspend fun <T> Flow<T>.bufferUntil(
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
                    emit(it)
                    predicateTrue.set(true)
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
