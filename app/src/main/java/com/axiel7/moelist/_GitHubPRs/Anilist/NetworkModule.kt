package com.axiel7.moelist._GitHubPRs.Anilist
//package com.axiel7.anihyou.di


//import com.apollographql.apollo.ApolloClient
//import com.apollographql.apollo.cache.normalized.api.MemoryCacheFactory
//import com.apollographql.apollo.cache.normalized.normalizedCache
//import com.apollographql.apollo.network.okHttpClient
//import com.axiel7.moelist.BuildConfig
//import com.axiel7.moelist.common.GlobalVariables
//import com.axiel7.moelist.Anilist.ANILIST_GRAPHQL_URL
//import okhttp3.Interceptor
//import okhttp3.OkHttpClient
//import okhttp3.Response
////import dagger.Module
////import dagger.Provides
////import dagger.hilt.InstallIn
////import dagger.hilt.components.SingletonComponent
////import javax.inject.Singleton
//
//
////NetworkModule NetworkModuleInstance  = new NetworkModule();
//
//object NetworkModule {
//
//    fun provideApolloClient(
//        authorizationInterceptor: AuthorizationInterceptor
//    ): ApolloClient {
//        val cacheFactory = MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024)
//
//        val okHttpClient = OkHttpClient.Builder()
//            .addInterceptor(authorizationInterceptor)
//            .build()
//
//        return ApolloClient.Builder()
//            .serverUrl(ANILIST_GRAPHQL_URL)
//            .okHttpClient(okHttpClient)
//            .normalizedCache(cacheFactory)
//            .httpExposeErrorBody(true)
//            .build()
//    }
//
//    class AuthorizationInterceptor(
//        private val globalVariables: GlobalVariables,
//    ) : Interceptor {
//        override fun intercept(chain: Interceptor.Chain): Response {
//            val request = chain.request().newBuilder()
//                .apply {
//                    globalVariables.accessToken?.let {
//                        addHeader("Authorization", "Bearer $it")
//                    }
//                }
//                .build()
//            return chain.proceed(request)
//        }
//    }
//
//    fun provideAuthorizationInterceptor(
//        globalVariables: GlobalVariables
//    ): AuthorizationInterceptor {
//        return AuthorizationInterceptor(globalVariables)
//    }
//
//    fun provideOkHttpClient(): OkHttpClient {
//        return OkHttpClient()
//            .newBuilder()
//            .addInterceptor {
//                it.proceed(
//                    it.request().newBuilder()
//                        //.addHeader("X-MAL-CLIENT-ID", BuildConfig.MAL_CLIENT_ID)
//                        .build()
//                )
//            }
//            .build()
//    }
//}