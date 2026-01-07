package org.henick

import com.google.gson.Gson

suspend fun main() {

    //println(LottoApi.getLastDraws())
    println(LottoApi.getLastDrawsPerGame("MiniLotto")!!.first().drawDate)
    println(LottoApi.getLastDrawsPerGame("MiniLotto")!!.first().results.first().resultsJson.sorted())
    //println(LottoApi.getDrawsByDate("2026-01-04"))
    //println(LottoApi.getDrawsByDatePerGame(gameType = "MiniLotto", drawDate = "2026-01-04"))

    val gson = Gson()

    //println(gson.toJson(LottoApi.checkLastMiniLotto(listOf(1, 29, 30, 33, 42))))

    //println(gson.toJson(LottoApi.checkLastEuroJackpot(listOf(10, 15, 37, 37, 37, 2, 9))))

    val ticket1 = listOf(11, 23, 36, 37, 39)
    val ticket2 = listOf(23, 31, 33, 36, 40)
    val ticket3 = listOf(23, 31, 33, 36, 40)

    println(gson.toJson(LottoApi.checkMultipleTickets("MiniLotto", ticket1, ticket2, ticket3)))

}