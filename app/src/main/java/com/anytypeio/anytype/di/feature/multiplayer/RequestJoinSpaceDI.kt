package com.anytypeio.anytype.di.feature.multiplayer

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.multiplayer.RequestJoinSpaceViewModel
import com.anytypeio.anytype.ui.multiplayer.RequestJoinSpaceFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module

@Component(
    dependencies = [RequestJoinSpaceDependencies::class],
    modules = [
        RequestJoinSpaceModule::class,
        RequestJoinSpaceModule.Declarations::class
    ]
)
@PerDialog
interface RequestJoinSpaceComponent {

    @Component.Builder
    interface Builder {
        fun withDependencies(dependencies: RequestJoinSpaceDependencies): Builder
        @BindsInstance
        fun withParams(params: RequestJoinSpaceViewModel.Params): Builder
        fun build(): RequestJoinSpaceComponent
    }

    fun inject(fragment: RequestJoinSpaceFragment)
}

@Module
object RequestJoinSpaceModule {
    @Module
    interface Declarations {
        @PerScreen
        @Binds
        fun bindViewModelFactory(factory: RequestJoinSpaceViewModel.Factory): ViewModelProvider.Factory
    }
}

interface RequestJoinSpaceDependencies : ComponentDependencies {
    fun blockRepository(): BlockRepository
    fun urlBuilder(): UrlBuilder
    fun dispatchers(): AppCoroutineDispatchers
    fun spaceManager(): SpaceManager
}