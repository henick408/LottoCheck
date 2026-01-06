package org.henick

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.util.Properties

interface LottoService {

    private val token: () -> String
        get() = {
            val props = Properties()
            val inputStream = Thread.currentThread()
                .contextClassLoader
                .getResourceAsStream("apikey.properties")
            props.load(inputStream)
            props.getProperty("token")
        }

    @GET("last-results")
    suspend fun getLastDrawsInfo(
        @Header("secret") secret: String = token()
    ): List<DrawResponse>

    @GET("last-results-per-game")
    suspend fun getLastDrawsInfoPerGame(
        @Query("gameType") gameType: String,
        @Header("secret") secret: String = token()
    ): List<DrawResponse>

    @GET("by-date?")
    suspend fun getDrawsInfoByDate(
        @Query("drawDate") drawDate: String,
        @Header("secret") secret: String = token()
    ): List<DrawResponse>

    @GET("by-date-per-game?")
    suspend fun getDrawsInfoByDatePerGame(
        @Query("gameType") gameType: String,
        @Query("drawDate") drawDate: String,
        @Query("index") index: Int = 1,
        @Query("size") size: Int = 1,
        @Query("sort") sort: String = "drawSystemId",
        @Query("order") order: String = "ASC",
        @Header("secret") secret: String = token()
    ): DrawResponseByDatePerGame

}