package com.anytypeio.anytype.middleware.converters

import anytype.clipboard.ClipboardOuterClass.Clipboard
import com.anytypeio.anytype.data.auth.mapper.Serializer
import com.anytypeio.anytype.data.auth.model.BlockEntity

class ClipboardSerializer : Serializer {

    override fun serialize(blocks: List<BlockEntity>): ByteArray {
        val models = blocks.map { it.block() }
        val clipboard = Clipboard.newBuilder().addAllBlocks(models).build()
        return clipboard.toByteArray()
    }

    override fun deserialize(blob: ByteArray): List<BlockEntity> {
        return Clipboard.parseFrom(blob).blocksList.blocks()
    }
}