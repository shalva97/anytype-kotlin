package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.dataview.interactor.AddDataViewRelationOption
import com.anytypeio.anytype.domain.dataview.interactor.AddTagToDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.RemoveTagFromDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewRecord
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectTypeProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.sets.ObjectObjectRelationValueViewModel
import com.anytypeio.anytype.presentation.sets.ObjectSetObjectRelationValueViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.database.modals.ObjectObjectRelationValueFragment
import com.anytypeio.anytype.ui.database.modals.ObjectSetObjectRelationValueFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [ObjectRelationValueModule::class, ObjectSetObjectRelationValueModule::class])
@PerModal
interface ObjectSetObjectRelationValueSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: ObjectRelationValueModule): Builder
        fun build(): ObjectSetObjectRelationValueSubComponent
    }

    fun inject(fragment: ObjectSetObjectRelationValueFragment)

    fun addObjectRelationValueComponent(): AddObjectRelationValueSubComponent.Builder
    fun addObjectRelationObjectValueComponent(): AddObjectRelationObjectValueSubComponent.Builder
    fun addRelationFileValueAddComponent() : RelationFileValueAddSubComponent.Builder
}

@Subcomponent(modules = [ObjectRelationValueModule::class, ObjectObjectRelationValueModule::class])
@PerModal
interface ObjectObjectRelationValueSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: ObjectRelationValueModule): Builder
        fun build(): ObjectObjectRelationValueSubComponent
    }

    fun inject(fragment: ObjectObjectRelationValueFragment)

    fun addObjectRelationValueComponent(): AddObjectRelationValueSubComponent.Builder
    fun addObjectRelationObjectValueComponent(): AddObjectRelationObjectValueSubComponent.Builder
    fun addRelationFileValueAddComponent() : RelationFileValueAddSubComponent.Builder
}

@Module
object ObjectRelationValueModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideAddRelationOptionUseCase(
        repo: BlockRepository
    ): AddDataViewRelationOption = AddDataViewRelationOption(repo = repo)

    @JvmStatic
    @Provides
    @PerModal
    fun provideAddTagToDataViewRecordUseCase(
        repo: BlockRepository
    ): AddTagToDataViewRecord = AddTagToDataViewRecord(repo = repo)

    @JvmStatic
    @Provides
    @PerModal
    fun provideRemoveTagFromDataViewRecordUseCase(
        repo: BlockRepository
    ): RemoveTagFromDataViewRecord = RemoveTagFromDataViewRecord(repo = repo)
}

@Module
object ObjectSetObjectRelationValueModule {
    @JvmStatic
    @Provides
    @PerModal
    fun provideViewModelFactoryForDataView(
        relations: ObjectRelationProvider,
        values: ObjectValueProvider,
        details: ObjectDetailProvider,
        types: ObjectTypeProvider,
        removeTagFromDataViewRecord: RemoveTagFromDataViewRecord,
        urlBuilder: UrlBuilder,
        dispatcher: Dispatcher<Payload>,
        updateDataViewRecord: UpdateDataViewRecord
    ): ObjectSetObjectRelationValueViewModel.Factory = ObjectSetObjectRelationValueViewModel.Factory(
        relations = relations,
        values = values,
        details = details,
        types = types,
        removeTagFromRecord = removeTagFromDataViewRecord,
        urlBuilder = urlBuilder,
        dispatcher = dispatcher,
        updateDataViewRecord = updateDataViewRecord
    )
}

@Module
object ObjectObjectRelationValueModule {
    @JvmStatic
    @Provides
    @PerModal
    fun provideViewModelFactoryForObject(
        relations: ObjectRelationProvider,
        values: ObjectValueProvider,
        details: ObjectDetailProvider,
        types: ObjectTypeProvider,
        urlBuilder: UrlBuilder,
        dispatcher: Dispatcher<Payload>,
        updateDetail: UpdateDetail,
    ): ObjectObjectRelationValueViewModel.Factory = ObjectObjectRelationValueViewModel.Factory(
        relations = relations,
        values = values,
        details = details,
        types = types,
        urlBuilder = urlBuilder,
        dispatcher = dispatcher,
        updateDetail = updateDetail
    )
}