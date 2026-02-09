package io.github.henick408.lottocheck.network

import io.github.henick408.lottocheck.network.dto.results.DrawResponse
import io.github.henick408.lottocheck.network.dto.prizes.PrizeEuroJackpotResponse
import io.github.henick408.lottocheck.network.dto.prizes.PrizeResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
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

    @GET("draw-prizes/{drawType}/{drawSystemId}")
    suspend fun getPrizesInfoByGame(
        @Path("drawType") gameType: String,
        @Path("drawSystemId") drawSystemId: Int
    ): Response<List<PrizeResponse>>

    @GET("draw-prizes/eurojackpot/{drawSystemId}")
    suspend fun getPrizesInfoEuroJackpot(
        @Path("drawSystemId") drawSystemId: Int
    ): Response<List<PrizeEuroJackpotResponse>>

}