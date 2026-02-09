package io.github.henick408.lottocheck.network

import okhttp3.Interceptor
import okhttp3.Response

internal class AuthInterceptor(
    private val apiKeyProvider: () -> String
): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .addHeader("secret", apiKeyProvider())
            .build()

        return chain.proceed(request)
    }
}