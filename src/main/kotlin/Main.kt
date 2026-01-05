package org.henick

import kotlinx.coroutines.runBlocking
import java.time.LocalDate

fun main() = runBlocking {

    val service = ApiInstance.createService(LottoService::class.java)

    try {
        val drawResponse: List<DrawResponse> = service.getDrawsByDate("2025-01-04")
        println(
            drawResponse.filter { it.gameType == "MiniLotto" }.map { it.results }
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}