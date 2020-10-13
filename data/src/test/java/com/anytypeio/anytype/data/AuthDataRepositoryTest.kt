package com.anytypeio.anytype.data

import com.anytypeio.anytype.data.auth.model.AccountEntity
import com.anytypeio.anytype.data.auth.model.WalletEntity
import com.anytypeio.anytype.data.auth.repo.*
import com.anytypeio.anytype.data.auth.repo.config.Configurator
import com.anytypeio.anytype.domain.auth.model.Account
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class AuthDataRepositoryTest {

    @Mock
    lateinit var authRemote: AuthRemote

    @Mock
    lateinit var authCache: AuthCache

    @Mock
    lateinit var configurator: Configurator

    lateinit var repo: AuthDataRepository

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        repo = AuthDataRepository(
            factory = AuthDataStoreFactory(
                cache = AuthCacheDataStore(
                    cache = authCache
                ),
                remote = AuthRemoteDataStore(
                    authRemote = authRemote
                )
            ),
            configurator = configurator
        )
    }

    @Test
    fun `should call only remote in order to select account`() = runBlocking {

        val id = MockDataFactory.randomUuid()

        val path = MockDataFactory.randomString()

        val account = AccountEntity(
            id = id,
            name = MockDataFactory.randomString(),
            color = null
        )

        authRemote.stub {
            onBlocking { startAccount(id = id, path = path) } doReturn account
        }

        repo.startAccount(
            id = id,
            path = path
        )

        verifyZeroInteractions(authCache)

        verify(authRemote, times(1)).startAccount(
            id = id,
            path = path
        )

        verifyNoMoreInteractions(authRemote)
    }

    @Test
    fun `should call only remote in order to create an account`() = runBlocking {

        val path = MockDataFactory.randomString()

        val name = MockDataFactory.randomString()

        val account = AccountEntity(
            id = name,
            name = MockDataFactory.randomString(),
            color = null
        )

        authRemote.stub {
            onBlocking { createAccount(name = name, avatarPath = path, invitationCode = "code") } doReturn account
        }

        repo.createAccount(
            name = name,
            avatarPath = path,
            invitationCode = "code"
        )

        verifyZeroInteractions(authCache)

        verify(authRemote, times(1)).createAccount(
            name = name,
            avatarPath = path,
            invitationCode = "code"
        )

        verifyNoMoreInteractions(authRemote)
    }

    @Test
    fun `should call only remote in order to recover accounts`() = runBlocking {

        authRemote.stub {
            onBlocking { recoverAccount() } doReturn Unit
        }

        repo.startLoadingAccounts()

        verifyZeroInteractions(authCache)
        verify(authRemote, times(1)).recoverAccount()
        verifyNoMoreInteractions(authRemote)
    }

    @Test
    fun `should call only cache in order to save account`() = runBlocking {

        val account = Account(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            avatar = null,
            color = null
        )

        authCache.stub {
            onBlocking { saveAccount(any()) } doReturn Unit
        }

        repo.saveAccount(account)

        verify(authCache, times(1)).saveAccount(any())
        verifyNoMoreInteractions(authCache)
        verifyZeroInteractions(authRemote)
    }

    @Test
    fun `should call only remote in order to create wallet`() = runBlocking {

        val path = MockDataFactory.randomString()

        val wallet = WalletEntity(
            mnemonic = MockDataFactory.randomString()
        )

        authRemote.stub {
            onBlocking { createWallet(path) } doReturn wallet
        }

        repo.createWallet(path)

        verifyZeroInteractions(authCache)
        verify(authRemote, times(1)).createWallet(path)
        verifyNoMoreInteractions(authRemote)
    }

    @Test
    fun `should call only cache in order to get current account`() = runBlocking {

        val account = AccountEntity(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            color = null
        )

        authCache.stub {
            onBlocking { getCurrentAccount() } doReturn account
        }

        repo.getCurrentAccount()

        verifyZeroInteractions(authRemote)
        verify(authCache, times(1)).getCurrentAccount()
        verifyNoMoreInteractions(authCache)
    }

    @Test
    fun `should call only cache in order to save mnemonic`() = runBlocking {

        val mnemonic = MockDataFactory.randomString()

        authCache.stub {
            onBlocking { saveMnemonic(mnemonic) } doReturn Unit
        }

        repo.saveMnemonic(mnemonic)

        verifyZeroInteractions(authRemote)
        verify(authCache, times(1)).saveMnemonic(mnemonic)
        verifyNoMoreInteractions(authCache)
    }

    @Test
    fun `should call only cache in order to get mnemonic`() = runBlocking {

        val mnemonic = MockDataFactory.randomString()

        authCache.stub {
            onBlocking { getMnemonic() } doReturn mnemonic
        }

        repo.getMnemonic()

        verifyZeroInteractions(authRemote)
        verify(authCache, times(1)).getMnemonic()
        verifyNoMoreInteractions(authCache)
    }

    @Test
    fun `should call cache and remote in order to logout`() = runBlocking {

        authCache.stub {
            onBlocking { logout() } doReturn Unit
        }

        repo.logout()

        verify(authCache, times(1)).logout()
        verifyNoMoreInteractions(authCache)
        verify(authRemote, times(1)).logout()
        verifyNoMoreInteractions(authRemote)
        verify(configurator, times(1)).release()
        verifyZeroInteractions(configurator)
    }

    @Test
    fun `should not call logout on cache if remote logout is not succeeded`() {

        authRemote.stub {
            onBlocking { logout() } doThrow IllegalStateException()
        }

        runBlocking {
            try {
                repo.logout()
            } catch (e: Exception) {
                verify(authRemote, times(1)).logout()
                verifyZeroInteractions(authCache)
            }
        }
    }

    @Test
    fun `should call only cache in order to get available accounts`() = runBlocking {

        val account = AccountEntity(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            color = null
        )

        val accounts = listOf(account)

        authCache.stub {
            onBlocking { getAccounts() } doReturn accounts
        }

        repo.getAccounts()

        verifyZeroInteractions(authRemote)
        verify(authCache, times(1)).getAccounts()
        verifyNoMoreInteractions(authCache)
    }
}