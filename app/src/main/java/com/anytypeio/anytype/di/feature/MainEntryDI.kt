package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.auth.interactor.LaunchAccount
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.presentation.main.MainViewModelFactory
import com.anytypeio.anytype.ui.main.MainActivity
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.Dispatchers

@Subcomponent(
    modules = [MainEntryModule::class]
)
@PerScreen
interface MainEntrySubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): MainEntrySubComponent
        fun module(module: MainEntryModule): Builder
    }

    fun inject(activity: MainActivity)
}

@Module
object MainEntryModule {
    @JvmStatic
    @PerScreen
    @Provides
    fun provideMainViewModelFactory(
        launchAccount: LaunchAccount
    ): MainViewModelFactory = MainViewModelFactory(launchAccount)

    @JvmStatic
    @PerScreen
    @Provides
    fun provideLaunchAccountUseCase(
        authRepository: AuthRepository,
        pathProvider: PathProvider
    ): LaunchAccount = LaunchAccount(
        repository = authRepository,
        pathProvider = pathProvider,
        context = Dispatchers.Main
    )
}