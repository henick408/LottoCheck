package org.henick

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiInstance {
    companion object {
        private val retrofitBuilder =
            Retrofit.Builder()
                .baseUrl("https://developers.lotto.pl/api/open/v1/lotteries/draw-results/")
                .addConverterFactory(GsonConverterFactory.create())


        private val httpClient = OkHttpClient.Builder().build()

        private val retrofit = retrofitBuilder.client(httpClient).build()


        fun <T> createService(serviceClass: Class<T>): T {
            return retrofit.create(serviceClass)
        }

    }

}