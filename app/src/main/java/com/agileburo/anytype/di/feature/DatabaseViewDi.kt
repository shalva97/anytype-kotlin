package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.database.interactor.GetDatabase
import com.agileburo.anytype.presentation.databaseview.DatabaseViewModelFactory
import com.agileburo.anytype.ui.table.DatabaseViewFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent


@Subcomponent(modules = [DatabaseViewModule::class])
@PerScreen
interface DatabaseViewSubComponent {

    @Subcomponent.Builder
    interface Builder {

        fun databaseViewModule(module: DatabaseViewModule): Builder
        fun build(): DatabaseViewSubComponent
    }

    fun inject(fragment: DatabaseViewFragment)
}

@Module
class DatabaseViewModule {

    @Provides
    @PerScreen
    fun provideGetDatabase() = GetDatabase()

    @Provides
    @PerScreen
    fun provideDatabaseViewFactory(getDatabase: GetDatabase) = DatabaseViewModelFactory(getDatabase)
}