package com.anytypeio.anytype.ui_settings.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.domain.auth.interactor.Logout
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.domain.device.ClearFileCache
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class AccountAndDataViewModel(
    private val clearFileCache: ClearFileCache,
    private val logout: Logout,
) : ViewModel() {

    val commands = MutableSharedFlow<Command>(replay = 0)

    val isClearFileCacheInProgress = MutableStateFlow(false)
    val isLoggingOut = MutableStateFlow(false)

    fun onClearFileCacheAccepted() {
        viewModelScope.launch {
            clearFileCache(BaseUseCase.None).collect { status ->
                when (status) {
                    is Interactor.Status.Started -> {
                        isClearFileCacheInProgress.value = true
                    }
                    is Interactor.Status.Error -> {
                        isClearFileCacheInProgress.value = false
                        val msg = "Error while clearing file cache: ${status.throwable.message}"
                        Timber.e(status.throwable, "Error while clearing file cache")
                        // TODO send toast
                    }
                    Interactor.Status.Success -> {
                        isClearFileCacheInProgress.value = false
                    }
                }
            }
        }
    }

    fun onLogoutClicked() {
        viewModelScope.launch {
            logout(params = BaseUseCase.None).collect { status ->
                when (status) {
                    is Interactor.Status.Started -> {
                        isLoggingOut.value = true
                    }
                    is Interactor.Status.Success -> {
                        isLoggingOut.value = false
                        commands.emit(Command.Logout)
                    }
                    is Interactor.Status.Error -> {
                        isLoggingOut.value = true
                        Timber.e(status.throwable, "Error while logging out")
                    }
                }
            }
        }
    }

    class Factory(
        private val clearFileCache: ClearFileCache,
        private val logout: Logout,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AccountAndDataViewModel(
                clearFileCache = clearFileCache,
                logout = logout
            ) as T
        }
    }

    sealed class Command {
        object Logout : Command()
    }
}