package org.henick

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiInstance(
    apiKeyProvider: () -> String
) {

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(apiKeyProvider))
        .build()

    private val retrofit =
        Retrofit.Builder()
            .baseUrl("https://developers.lotto.pl/api/open/v1/lotteries/draw-results/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()


    fun createService(): LottoService =
        retrofit.create(LottoService::class.java)


}