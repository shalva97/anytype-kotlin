package com.anytypeio.anytype.di.feature.relations

import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.dataview.interactor.AddNewRelationToDataView
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.relations.AddNewRelationToObject
import com.anytypeio.anytype.presentation.relations.RelationCreateFromScratchForDataViewViewModel
import com.anytypeio.anytype.presentation.relations.RelationCreateFromScratchForObjectViewModel
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.relations.RelationCreateFromScratchForDataViewFragment
import com.anytypeio.anytype.ui.relations.RelationCreateFromScratchForObjectFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.StateFlow

@Subcomponent(modules = [RelationCreateFromScratchForObjectModule::class])
@PerDialog
interface RelationCreateFromScratchForObjectSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: RelationCreateFromScratchForObjectModule): Builder
        fun build(): RelationCreateFromScratchForObjectSubComponent
    }

    fun inject(fragment: RelationCreateFromScratchForObjectFragment)
}

@Module
object RelationCreateFromScratchForObjectModule {
    @JvmStatic
    @Provides
    @PerDialog
    fun provideViewModelFactory(
        addNewRelationToObject: AddNewRelationToObject,
        dispatcher: Dispatcher<Payload>
    ): RelationCreateFromScratchForObjectViewModel.Factory =
        RelationCreateFromScratchForObjectViewModel.Factory(
            addNewRelationToObject = addNewRelationToObject,
            dispatcher = dispatcher
        )

    @JvmStatic
    @Provides
    @PerDialog
    fun provideAddNewRelationToObjectUseCase(
        repo: BlockRepository
    ): AddNewRelationToObject = AddNewRelationToObject(repo)
}

@Subcomponent(modules = [RelationCreateFromScratchForDataViewModule::class])
@PerDialog
interface RelationCreateFromScratchForDataViewSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: RelationCreateFromScratchForDataViewModule): Builder
        fun build(): RelationCreateFromScratchForDataViewSubComponent
    }

    fun inject(fragment: RelationCreateFromScratchForDataViewFragment)
}

@Module
object RelationCreateFromScratchForDataViewModule {
    @JvmStatic
    @Provides
    @PerDialog
    fun provideViewModelFactory(
        state: StateFlow<ObjectSet>,
        session: ObjectSetSession,
        updateDataViewViewer: UpdateDataViewViewer,
        addNewRelationToDataView: AddNewRelationToDataView,
        dispatcher: Dispatcher<Payload>
    ) : RelationCreateFromScratchForDataViewViewModel.Factory = RelationCreateFromScratchForDataViewViewModel.Factory(
        addNewRelationToDataView = addNewRelationToDataView,
        dispatcher = dispatcher,
        state = state,
        session = session,
        updateDataViewViewer = updateDataViewViewer
    )
}