package org.henick.lottolib.network

import org.henick.lottolib.network.dto.DrawResponse
import org.henick.lottolib.network.dto.DrawResponseByDatePerGame
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

    @GET("draw-results/by-date-per-game?")
    suspend fun getDrawsInfoByDatePerGame(
        @Query("gameType") gameType: String,
        @Query("drawDate") drawDate: String,
        @Query("index") index: Int = 1,
        @Query("size") size: Int = 1,
        @Query("sort") sort: String = "drawSystemId",
        @Query("order") order: String = "ASC"
    ): Response<DrawResponseByDatePerGame>

}