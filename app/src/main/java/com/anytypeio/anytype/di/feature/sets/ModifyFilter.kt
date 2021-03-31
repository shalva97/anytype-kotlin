package com.anytypeio.anytype.di.feature.sets;

import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.filter.FilterViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.sets.modals.filter.ModifyFilterFromInputFieldValueFragment
import com.anytypeio.anytype.ui.sets.modals.filter.ModifyFilterFromSelectedValueFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.StateFlow

@Subcomponent(modules = [ModifyFilterModule::class])
@PerModal
interface ModifyFilterSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: ModifyFilterModule): Builder
        fun build(): ModifyFilterSubComponent
    }

    fun inject(fragment: ModifyFilterFromInputFieldValueFragment)
    fun inject(fragment: ModifyFilterFromSelectedValueFragment)
    fun createPickConditionComponent(): PickFilterConditionSubComponent.Builder
}

@Module
object ModifyFilterModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideViewModelFactory(
        state: StateFlow<ObjectSet>,
        session: ObjectSetSession,
        dispatcher: Dispatcher<Payload>,
        updateDataViewViewer: UpdateDataViewViewer,
        searchObjects: SearchObjects,
        urlBuilder: UrlBuilder
    ): FilterViewModel.Factory = FilterViewModel.Factory(
        objectSetState = state,
        session = session,
        dispatcher = dispatcher,
        updateDataViewViewer = updateDataViewViewer,
        searchObjects = searchObjects,
        urlBuilder = urlBuilder
    )

    @JvmStatic
    @Provides
    @PerModal
    fun provideSearchObjectsUseCase(
        repo: BlockRepository
    ): SearchObjects = SearchObjects(repo = repo)
}