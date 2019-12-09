package com.agileburo.anytype.domain.event.model

import com.agileburo.anytype.domain.block.model.Block

sealed class Event {
    sealed class Command : Event() {
        data class ShowBlock(
            val rootId: String,
            val blocks: List<Block>
        ) : Command()

        data class AddBlock(
            val blocks: List<Block>
        ) : Command()

        data class UpdateBlockText(
            val id: String,
            val text: String
        ) : Command()
    }
}