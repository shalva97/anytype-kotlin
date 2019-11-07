package com.agileburo.anytype.di.main

import android.content.Context
import android.content.SharedPreferences
import com.agileburo.anytype.data.auth.repo.*
import com.agileburo.anytype.db.AnytypeDatabase
import com.agileburo.anytype.device.DefaultPathProvider
import com.agileburo.anytype.domain.auth.repo.AuthRepository
import com.agileburo.anytype.domain.auth.repo.PathProvider
import com.agileburo.anytype.middleware.EventProxy
import com.agileburo.anytype.middleware.auth.AuthMiddleware
import com.agileburo.anytype.middleware.interactor.EventHandler
import com.agileburo.anytype.middleware.interactor.Middleware
import com.agileburo.anytype.repo.DefaultAuthCache
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataModule {

    @Provides
    fun providePathProvider(context: Context): PathProvider {
        return DefaultPathProvider(context)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        factory: AuthDataStoreFactory
    ): AuthRepository {
        return AuthDataRepository(
            factory = factory
        )
    }

    @Provides
    @Singleton
    fun provideAuthDataStoreFactory(
        authCacheDataStore: AuthCacheDataStore,
        authRemoteDataStore: AuthRemoteDataStore
    ): AuthDataStoreFactory {
        return AuthDataStoreFactory(
            cache = authCacheDataStore,
            remote = authRemoteDataStore
        )
    }

    @Provides
    @Singleton
    fun provideAuthCacheDataStore(
        authCache: AuthCache
    ): AuthCacheDataStore {
        return AuthCacheDataStore(
            cache = authCache
        )
    }

    @Provides
    @Singleton
    fun provideAuthCache(
        db: AnytypeDatabase,
        prefs: SharedPreferences
    ): AuthCache {
        return DefaultAuthCache(
            db = db,
            prefs = prefs
        )
    }

    @Provides
    @Singleton
    fun provideAnytypeDatabase(context: Context): AnytypeDatabase {
        return AnytypeDatabase.get(context)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideAuthRemoteDataStore(
        authRemote: AuthRemote
    ): AuthRemoteDataStore {
        return AuthRemoteDataStore(
            authRemote = authRemote
        )
    }

    @Provides
    @Singleton
    fun provideAuthRemote(
        middleware: Middleware,
        proxy: EventProxy
    ): AuthRemote {
        return AuthMiddleware(
            middleware = middleware,
            events = proxy
        )
    }

    @Provides
    @Singleton
    fun provideEventProxy(): EventProxy {
        return EventHandler()
    }

    @Provides
    @Singleton
    fun provideMiddleware(): Middleware {
        return Middleware()
    }
}