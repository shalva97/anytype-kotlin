package com.agileburo.anytype.data.auth.repo.block

import com.agileburo.anytype.data.auth.model.BlockEntity
import com.agileburo.anytype.data.auth.model.CommandEntity
import com.agileburo.anytype.data.auth.model.ConfigEntity
import com.agileburo.anytype.data.auth.model.EventEntity
import kotlinx.coroutines.flow.Flow

interface BlockDataStore {
    suspend fun create(command: CommandEntity.Create)
    suspend fun update(update: CommandEntity.Update)
    suspend fun getConfig(): ConfigEntity
    suspend fun createPage(parentId: String): String
    suspend fun openPage(id: String)
    suspend fun closePage(id: String)
    suspend fun openDashboard(contextId: String, id: String)
    suspend fun closeDashboard(id: String)

    suspend fun observeBlocks(): Flow<List<BlockEntity>>

    suspend fun observeEvents(): Flow<EventEntity>

    suspend fun observePages(): Flow<List<BlockEntity>>
}