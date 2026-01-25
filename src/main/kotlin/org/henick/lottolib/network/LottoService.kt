package org.henick.lottolib.network

import org.henick.lottolib.network.dto.DrawResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LottoService {

    @GET("draw-results/last-results")
    suspend fun getLastDrawsInfo(): Response<List<DrawResponse>>

    @GET("draw-results/last-results-per-game")
    suspend fun getLastDrawsInfoPerGame(
        @Query("gameType") gameType: String
    ): Response<List<DrawResponse>>

    @GET("draw-results/by-date?")
    suspend fun getDrawsInfoByDate(
        @Query("drawDate") drawDate: String
    ): Response<List<DrawResponse>>

}