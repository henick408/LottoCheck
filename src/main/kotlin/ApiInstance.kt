package org.henick

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiInstance {
    companion object {
        private val retrofitBuilder =
            Retrofit.Builder()
                .baseUrl("https://developers.lotto.pl/api/open/v1/lotteries/")
                .addConverterFactory(GsonConverterFactory.create())


        private val retrofit = retrofitBuilder.build()

        private val httpClient = OkHttpClient.Builder()

        fun <T> createService(serviceClass: Class<T>): T {
            return retrofit.create(serviceClass)
        }

    }

}