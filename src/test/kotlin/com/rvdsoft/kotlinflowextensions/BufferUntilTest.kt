package com.rvdsoft.kotlinflowextensions

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import org.junit.Test
import kotlin.test.assertEquals

class BufferUntilTest {

    sealed class Intent {
        object InitialIntent : Intent()
        object Save : Intent()
        object Delete : Intent()
        object Update : Intent()
    }

    val channel = Channel<Intent>(Channel.RENDEZVOUS)

    @ExperimentalCoroutinesApi
    @Test
    fun bufferUntilTest() = runBlocking {
        val intents = ArrayList<Intent>()
        val job = launch(Dispatchers.IO) {
            channel.asFlow().bufferUntilReorder {
                it is Intent.InitialIntent
            }.collect {
                intents += it
                cancel()
            }
        }
        channel.send(Intent.Save)
        assert(intents.isEmpty())
        launch(Dispatchers.IO) {
            channel.send(Intent.Delete)
        }
        launch(Dispatchers.IO) {
            channel.send(Intent.InitialIntent)
        }

        launch(Dispatchers.IO) {
            //should not get added to intents
            channel.send(Intent.Update)
        }
        job.join()
        assert(intents.isNotEmpty())
        assertEquals(Intent.InitialIntent, intents[0])
        assertEquals(3, intents.size)
        assertEquals(Intent.Save, intents[1])
    }

    private fun <T> Channel<T>.asFlow(): Flow<T> {
        val channel = this
        return flow {
            for (x in channel) {
                yield()
                emit(x)
            }
        }
    }
}