package org.henick

import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {

    //println(LottoApi.getLastDraws())
    //println(LottoApi.getLastDrawsPerGame("MiniLotto"))
    println(LottoApi.getDrawsByDate("2026-01-04"))
    //println(LottoApi.getDrawsByDatePerGame(gameType = "MiniLotto", drawDate = "2026-01-04"))



}