package com.rvdsoft.kotlinflowextensions

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.selects.SelectBuilder
import kotlinx.coroutines.selects.select


@ExperimentalCoroutinesApi
@Suppress("UNCHECKED_CAST")
suspend fun <T, R> FlowCollector<R>.withLatestFromInternal(
    source: Flow<T>,
    flowzs: Array<out Flow<T>>,
    arrayFactory: () -> Array<T?>,
    transform: suspend FlowCollector<R>.(Array<T>) -> Unit
) {
    coroutineScope {
        val flows = arrayOf(source) + flowzs
        val size = flows.size
        val channels =
            Array(size) { asFairChannel(flows[it]) }
        val latestValues = arrayOfNulls<Any?>(size)
        val isClosed = Array(size) { false }

        // See flow.combine(other) for explanation.
        while (!isClosed.all { it }) {
            select<Unit> {
                for (i in 0 until size) {
                    onReceive(isClosed[i], channels[i], { isClosed[i] = true }) { value ->
                        if (i > 0)
                            latestValues[i] = value
                        if (i == 0 || latestValues.all { it !== null }) {
                            val arguments = (arrayOf(value).toList() + arrayFactory().toList()).toTypedArray()
                            for (index in 0 until size) {
                                if (index == 0) {
                                    arguments[index] = NULL.unbox(value)
                                } else {
                                    arguments[index] = NULL.unbox(latestValues[index])
                                }
                            }
                            transform(arguments as Array<T>)
                        }
                    }
                }
            }
        }

    }
}

private object NULL {

}

@Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
private inline fun <T> NULL.unbox(value: Any?): T = if (value === this) null as T else value as T

@ExperimentalCoroutinesApi
private fun CoroutineScope.asFairChannel(flow: Flow<*>): ReceiveChannel<Any> = produce {
    flow.collect { value ->
        val x = value ?: NULL
        if (offer(x)) {
            yield() // Works only on fast path to properly work in sequential use-cases
        } else
            send(x)
    }
}


@UseExperimental(ObsoleteCoroutinesApi::class)
private inline fun SelectBuilder<Unit>.onReceive(
    isClosed: Boolean,
    channel: ReceiveChannel<Any>,
    crossinline onClosed: () -> Unit,
    noinline onReceive: suspend (value: Any) -> Unit
) {
    if (isClosed) return
    channel.onReceiveOrNull() {
        // TODO onReceiveOrClosed when boxing issues are fixed
        if (it === null) onClosed()
        else onReceive(it)
    }
}