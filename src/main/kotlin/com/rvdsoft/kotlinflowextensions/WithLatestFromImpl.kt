package com.rvdsoft.kotlinflowextensions

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import java.util.concurrent.atomic.AtomicReference


@ExperimentalCoroutinesApi
@Suppress("UNCHECKED_CAST")
/**
 * @see <a href="https://github.com/Kotlin/kotlinx.coroutines/issues/1498">issue 1498 @ kotlinx coroutines</>
 */
suspend fun <T, R> FlowCollector<R>.withLatestFromInternal(
    source: Flow<T>,
    flows: Array<out Flow<T>>,
    transform: suspend FlowCollector<R>.(Array<T>) -> Unit
) {
    coroutineScope {
        val size = flows.size
        val latestValues = MutableList(size) { AtomicReference<T?>() }
        val outerScope = this
        for (i in 0..size) {
            launch {
                try {
                    flows[i].collect { latestValues.get(i).set(it) }
                } catch (e: CancellationException) {
                    outerScope.cancel(e) // cancel outer scope on cancellation exception, too
                }
            }

        }
        source.collect { a ->
            if (latestValues.all { it.get() != null }) {
                val args = (listOf(a!!) + latestValues.map { it.get()!! }.toList()).toTypedArray<Any?>()
                transform(args as Array<T>)
            }
        }
    }
}
