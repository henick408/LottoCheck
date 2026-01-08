package org.henick

import retrofit2.http.GET
import retrofit2.http.Query

interface LottoService {
    
    @GET("last-results")
    suspend fun getLastDrawsInfo(): List<DrawResponse>

    @GET("last-results-per-game")
    suspend fun getLastDrawsInfoPerGame(
        @Query("gameType") gameType: String
    ): List<DrawResponse>

    @GET("by-date?")
    suspend fun getDrawsInfoByDate(
        @Query("drawDate") drawDate: String
    ): List<DrawResponse>

    @GET("by-date-per-game?")
    suspend fun getDrawsInfoByDatePerGame(
        @Query("gameType") gameType: String,
        @Query("drawDate") drawDate: String,
        @Query("index") index: Int = 1,
        @Query("size") size: Int = 1,
        @Query("sort") sort: String = "drawSystemId",
        @Query("order") order: String = "ASC"
    ): DrawResponseByDatePerGame

}