@file:Suppress("UNCHECKED_CAST")

package com.rvdsoft.kotlinflowextensions

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take


class AbortCollectException : CancellationException()

fun <T> Flow<T>.repeat(): Flow<T> = flow {
    try {
        collect {
            emit(it)
            throw AbortCollectException()
        }
    } catch (e: AbortCollectException) {
        repeat().collect {
            emit(it)
        }
    }
}

fun <T> Flow<T>.zipLatest(): Flow<T> {
    return take(1).repeat()
}

@ExperimentalCoroutinesApi
inline fun <reified T, R> Flow<T>.withLatestFrom(
    vararg flows: Flow<T>,
    crossinline transform: suspend (Array<T>) -> R
): Flow<R> {
    val t = this
    return flow {
        withLatestFromInternal(t, flows) { emit(transform(it)) }
    }
}

@ExperimentalCoroutinesApi
inline fun <T, T1, R> Flow<T>.withLatestFrom(
    flow1: Flow<T1>,
    crossinline transform: suspend (T, T1) -> R
): Flow<R> = withLatestFrom(flow1) { args: Array<*> ->
    transform(
        args[0] as T,
        args[1] as T1
    )
}


@ExperimentalCoroutinesApi
inline fun <T, T1, T2, R> Flow<T>.withLatestFrom(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    crossinline transform: suspend (T, T1, T2) -> R
): Flow<R> = withLatestFrom(flow1, flow2) { args: Array<*> ->
    transform(
        args[0] as T,
        args[1] as T1,
        args[2] as T2
    )
}

@ExperimentalCoroutinesApi
inline fun <T, T1, T2, T3, R> Flow<T>.withLatestFrom(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    crossinline transform: suspend (T, T1, T2, T3) -> R
): Flow<R> = withLatestFrom(flow1, flow2, flow3) { args: Array<*> ->
    transform(
        args[0] as T,
        args[1] as T1,
        args[2] as T2,
        args[3] as T3
    )
}


@ExperimentalCoroutinesApi
inline fun <T, T1, T2, T3, T4, R> Flow<T>.withLatestFrom(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    crossinline transform: suspend (T, T1, T2, T3, T4) -> R
): Flow<R> = withLatestFrom(flow1, flow2, flow3, flow4) { args: Array<*> ->
    transform(
        args[0] as T,
        args[1] as T1,
        args[2] as T2,
        args[3] as T3,
        args[4] as T4
    )
}

@ExperimentalCoroutinesApi
inline fun <T, T1, T2, T3, T4, T5, R> Flow<T>.withLatestFrom(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    crossinline transform: suspend (T, T1, T2, T3, T4, T5) -> R
): Flow<R> = withLatestFrom(flow1, flow2, flow3, flow4, flow5) { args: Array<*> ->
    transform(
        args[0] as T,
        args[1] as T1,
        args[2] as T2,
        args[3] as T3,
        args[4] as T4,
        args[5] as T5
    )
}

@ExperimentalCoroutinesApi
inline fun <T, T1, T2, T3, T4, T5, T6, R> Flow<T>.withLatestFrom(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    crossinline transform: suspend (T, T1, T2, T3, T4, T5, T6) -> R
): Flow<R> = withLatestFrom(flow1, flow2, flow3, flow4, flow5, flow6) { args: Array<*> ->
    transform(
        args[0] as T,
        args[1] as T1,
        args[2] as T2,
        args[3] as T3,
        args[4] as T4,
        args[5] as T5,
        args[6] as T6
    )
}

