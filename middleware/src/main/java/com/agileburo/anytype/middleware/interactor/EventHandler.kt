package com.agileburo.anytype.middleware.interactor

import anytype.Events
import com.agileburo.anytype.middleware.EventProxy
import com.google.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import lib.Lib.setEventHandlerMobile
import timber.log.Timber

class EventHandler(
    private val scope: CoroutineScope = GlobalScope
) : EventProxy {

    // TODO consider using BroadcastChannel in the future.
    private val channel = Channel<Events.Event>()

    init {
        setEventHandlerMobile { bytes ->
            scope.launch {
                handle(bytes)
            }
        }
    }

    private suspend fun handle(bytes: ByteArray) {
        try {
            Events.Event.parseFrom(bytes).let {
                Timber.d("New event from middleware: $it")
                channel.send(it)
            }
        } catch (e: InvalidProtocolBufferException) {
            Timber.e(e, "Error while deserializing message")
        }
    }

    override fun flow(): Flow<Events.Event> = channel.consumeAsFlow()
}