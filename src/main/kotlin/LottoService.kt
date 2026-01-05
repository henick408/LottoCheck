package org.henick

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.io.FileInputStream
import java.time.LocalDate
import java.util.Properties

interface LottoService {

    val token: () -> String
        get() = {
            val props = Properties()
            val inputStream = Thread.currentThread()
                .contextClassLoader
                .getResourceAsStream("apikey.properties")
            props.load(inputStream)
            props.getProperty("token")
        }

    @GET("last-results")
    suspend fun getLastDraws(
        @Header("secret") secret: String = token()
    )

    @GET("last-results-per-game")
    suspend fun getLastDrawsPerGame(
        @Header("secret") secret: String = token(),
        @Query("gameType") gameType: String
    )

    @GET("by-date?")
    suspend fun getDrawsByDate(
        @Header("secret") secret: String = token(),
        @Query("drawDate") drawDate: LocalDate
    )

    @GET("by-date-per-game?")
    suspend fun getDrawsByDatePerGame(
        @Header("secret") secret: String = token(),
        @Query("gameType") gameType: String,
        @Query("drawDate") drawDate: LocalDate,
        @Query("index") index: Int = 1,
        @Query("size") size: Int = 1,
        @Query("sort") sort: String = "drawSystemId",
        @Query("order") order: String = "ASC"
    )

}