package com.anytypeio.anytype.data.auth.repo

import com.anytypeio.anytype.data.auth.model.AccountEntity
import com.anytypeio.anytype.data.auth.model.WalletEntity
import kotlinx.coroutines.flow.Flow

interface AuthDataStore {

    suspend fun startAccount(id: String, path: String): AccountEntity

    suspend fun createAccount(name: String, avatarPath: String?, invitationCode: String): AccountEntity

    suspend fun recoverAccount()

    suspend fun saveAccount(account: AccountEntity)

    suspend fun updateAccount(account: AccountEntity)

    fun observeAccounts(): Flow<AccountEntity>

    suspend fun getCurrentAccount(): AccountEntity
    suspend fun getCurrentAccountId(): String

    suspend fun createWallet(path: String): WalletEntity
    suspend fun recoverWallet(path: String, mnemonic: String)
    suspend fun saveMnemonic(mnemonic: String)
    suspend fun getMnemonic(): String

    suspend fun logout()
    suspend fun getAccounts(): List<AccountEntity>
    suspend fun setCurrentAccount(id: String)
}