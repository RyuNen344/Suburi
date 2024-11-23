package io.github.ryunen344.suburi.data

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.time.Duration
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class OkHttpModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val timeout = Duration.ofMillis(TIMEOUT_MILLS)
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor(TimberHttpLoggingInterceptorLogger()))
            .connectTimeout(timeout)
            .readTimeout(timeout)
            .writeTimeout(timeout)
            .build()
    }

    private companion object {
        const val TIMEOUT_MILLS = 180000L
    }
}
