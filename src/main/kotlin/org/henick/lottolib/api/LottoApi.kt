package org.henick.lottolib.api

import org.henick.lottolib.domain.GameType
import org.henick.lottolib.domain.Ticket
import org.henick.lottolib.domain.TicketNumbers
import org.henick.lottolib.model.CheckResponse
import org.henick.lottolib.model.WinInfo
import org.henick.lottolib.model.WinningNumbers
import org.henick.lottolib.network.ApiInstance
import org.henick.lottolib.network.LottoService
import org.henick.lottolib.network.dto.results.DrawResponse
import java.time.LocalDate

class LottoApi private constructor(
    val service: LottoService
) {

    companion object {
        suspend fun init(apiKey: String): LottoApi {
            val lottoService: LottoService = ApiInstance { apiKey }.createService()
            val code = lottoService.getLastDrawsInfo().code()
            if (code == 401) {
                throw LottoInvalidApiTokenException("Wprowadzony token jest niepoprawnym tokenem LottoApi")
            }
            return LottoApi(lottoService)
        }
        private val invalidSizeInfo = WinInfo(
            info = "Niepoprawna ilosc liczb"
        )
        private val invalidRangeInfo = WinInfo(
            info = "Niepoprawny zakres liczb"
        )
        private val noHitsWinInfo = WinInfo(
            info = "Nie trafiono nic"
        )
        private val specialsNeededInfo = WinInfo(
            info = "Ta gra wymaga dwoch zestawow liczb"
        )
    }

    suspend fun getLastDraws(): List<DrawResponse> {
        return service.getLastDrawsInfo().body()!!
    }

    suspend fun getLastDrawsPerGame(gameType: GameType): List<DrawResponse> {
        return service.getLastDrawsInfoPerGame(gameType.gameName).body()!!
    }

    suspend fun getDrawsByDate(drawDate: LocalDate): List<DrawResponse> {
        return service.getDrawsInfoByDate(drawDate.toString()).body()!!
    }

    suspend fun getDrawsByDatePerGame(gameType: GameType, drawDate: LocalDate): List<DrawResponse> {
        return service.getDrawsInfoByDate(drawDate.toString()).body()!!.filter { it.gameType == gameType.gameName }
    }

    suspend fun checkTicket(ticket: Ticket): CheckResponse {
        if (ticket.ticketNumbers.isEmpty()) {
            throw LottoInvalidTicketException("Ticket musi zawierac elementy TicketNumbers")
        }
        val draws: List<DrawResponse> = if (ticket.gameType.nonSpecialGame != null) {
            getDrawsByDatePerGame(gameType = ticket.gameType.nonSpecialGame, drawDate = ticket.drawDate)
        } else {
            getDrawsByDatePerGame(gameType = ticket.gameType, drawDate = ticket.drawDate)
        }

        if (!ticket.ticketNumbers.all { it.gameType == ticket.gameType}) {
            throw LottoInvalidTicketException("GameType poszczegolnych ticketNumbers jest inny niz gameType Ticketa")
        }
        if (draws.isEmpty()) {
            return CheckResponse(
                gameType = ticket.gameType,
                info = "Nie ma wynikow losowania z danego dnia")
        }
        val winInfoList: MutableList<WinInfo> = mutableListOf()

        ticket.ticketNumbers.forEach { winInfoList.add(it.checkTicketNumbers(draws)) }

        var specialResults: List<Int>? = draws.getSpecialResults()

        if (specialResults?.size == draws.getResults().size) {
            if(ticket.gameType.nonSpecialGame == null) {
                specialResults = null
            }
        }

        return CheckResponse(
            drawSystemId = draws.getDrawSystemId(),
            gameType = ticket.gameType,
            results = draws.getResults(),
            specialResults = specialResults,
            winInfoJson = winInfoList
        )
    }

    private fun TicketNumbers.getWinInfoFromResults(results: List<Int>, specialResults: List<Int>?): WinInfo {
        val winningNumbers: MutableList<WinningNumbers> = mutableListOf()
        val hits = this.numbers.filter { results.contains(it) }.size
        var specialHits: Int?

        if (this.specialNumbers != null && specialResults != null) {
            specialHits = this.specialNumbers.filter { specialResults.contains(it) }.size
            if(!(hits >= 3 || (hits == 2 && specialHits >= 1) || (hits == 1 && specialHits == 2))) {
                return noHitsWinInfo
            }
            return WinInfo(
                winningNumbers = listOf(
                    WinningNumbers(
                        numbers = this.numbers.sorted(),
                        specialNumbers = this.specialNumbers.sorted(),
                        hits = hits,
                        specialHits = specialHits,
                        gameType = this.gameType
                    )
                )
            )
        }

        if (specialResults != null) {
            specialHits = this.numbers.filter { specialResults.contains(it) }.size
            if (specialHits >= 3) {
                winningNumbers.add(
                    WinningNumbers(
                        numbers = this.numbers.sorted(),
                        hits = specialHits,
                        gameType = this.gameType
                    )
                )
            }
        }

        if (hits >= 3) {
            val gameType = this.gameType.nonSpecialGame ?: this.gameType
            winningNumbers.add(
                WinningNumbers(
                    numbers = this.numbers.sorted(),
                    hits = hits,
                    gameType = gameType
                )
            )
        }

        if (winningNumbers.isEmpty()) {
            return noHitsWinInfo
        }

        return WinInfo(
            winningNumbers = winningNumbers.sortedBy { it.gameType }
        )
    }

    private fun TicketNumbers.checkTicketNumbers(draws: List<DrawResponse>): WinInfo {
        if (this.specialNumbers == null && (this.gameType.specialRange != null || this.gameType.specialAmount != null)) {
            return specialsNeededInfo
        }
        if (!this.isValidSize()) {
            return invalidSizeInfo
        }
        if (!this.isValidRange()) {
            return invalidRangeInfo
        }

        val results: List<Int> = draws.getResults()
        var specialResults: List<Int>? = draws.getSpecialResults()

        if (specialResults?.size == draws.getResults().size) {
            if(this.gameType.nonSpecialGame == null) {
                specialResults = null
            }
        }

        return this.getWinInfoFromResults(results, specialResults)
    }

    private fun List<DrawResponse>.getResults(): List<Int> {
        return this.first().results.first().resultsJson.sorted()
    }

    private fun List<DrawResponse>.getSpecialResults(): List<Int>? {
        if (this.first().results.first().specialResults.isEmpty() && this.first().results.size == 1) {
            return null
        }

        if (this.first().results.first().specialResults.isEmpty()) {
            return this.first().results[1].resultsJson.sorted()
        }

        return this.first().results.first().specialResults.sorted()
    }

    private fun List<DrawResponse>.getDrawSystemId(): Int {
        return this.first().drawSystemId
    }

}