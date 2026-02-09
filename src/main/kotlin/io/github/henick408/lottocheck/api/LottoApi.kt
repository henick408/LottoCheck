package io.github.henick408.lottocheck.api

import io.github.henick408.lottocheck.domain.EuroJackpotPrize
import io.github.henick408.lottocheck.domain.GameType
import io.github.henick408.lottocheck.domain.Ticket
import io.github.henick408.lottocheck.domain.TicketNumbers
import io.github.henick408.lottocheck.model.CheckResponse
import io.github.henick408.lottocheck.model.WinInfo
import io.github.henick408.lottocheck.model.WinningNumbers
import io.github.henick408.lottocheck.network.ApiInstance
import io.github.henick408.lottocheck.network.LottoService
import io.github.henick408.lottocheck.network.dto.prizes.PrizeDto
import io.github.henick408.lottocheck.network.dto.prizes.PrizeEuroJackpotResponse
import io.github.henick408.lottocheck.network.dto.prizes.PrizeResponse
import io.github.henick408.lottocheck.network.dto.results.DrawResponse
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
            if (code == 429) {
                throw LottoTooManyApiRequestsException("Przeslano za duzo zapytan do lotto api w krotkim czasie")
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

    internal suspend fun getLastDraws(): List<DrawResponse> {
        return service.getLastDrawsInfo().body() ?: listOf()
    }

    internal suspend fun getLastDrawsPerGame(gameType: GameType): List<DrawResponse> {
        return service.getLastDrawsInfoPerGame(gameType.gameName).body() ?: listOf()
    }

    internal suspend fun getDrawsByDate(drawDate: LocalDate): List<DrawResponse> {
        return service.getDrawsInfoByDate(drawDate.toString()).body() ?: listOf()
    }

    internal suspend fun getDrawsByDatePerGame(gameType: GameType, drawDate: LocalDate): List<DrawResponse> {
        return service.getDrawsInfoByDate(drawDate.toString()).body()?.filter { it.gameType == gameType.gameName } ?: listOf()
    }

    internal suspend fun getPrizesPerGame(gameType: GameType, drawSystemId: Int): List<PrizeResponse> {
        return service.getPrizesInfoByGame(gameType.gameName, drawSystemId).body()?.filterNot { it.gameType == "SuperSzansa" } ?: listOf()
    }

    internal suspend fun getPrizesEuroJackpot(drawSystemId: Int): List<PrizeEuroJackpotResponse> {
        return service.getPrizesInfoEuroJackpot(drawSystemId).body() ?: listOf()
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

    private suspend fun TicketNumbers.getPrizeInfo(drawSystemId: Int, hits: Int, specialHits: Int? = null, isSpecial: Boolean = false): Double {
        val prizesMap: Map<String, PrizeDto>
        val prizeElement: Int?
        val prizeValue: Double

        if (specialHits != null) {
            prizesMap = getPrizesEuroJackpot(drawSystemId).first().prizes
            prizeElement = EuroJackpotPrize[hits, specialHits]
            prizeValue = prizesMap[prizeElement.toString()]?.prizeValue ?: return 0.0
            return prizeValue
        }

        prizesMap = if(this.gameType.nonSpecialGame != null && !isSpecial) {
            getPrizesPerGame(this.gameType, drawSystemId).last().prizes
        } else {
            getPrizesPerGame(this.gameType, drawSystemId).first().prizes
        }
        prizeElement = this.gameType.amount - hits + 1
        prizeValue = prizesMap[prizeElement.toString()]?.prizeValue ?: return 0.0
        return prizeValue

    }

    private suspend fun TicketNumbers.getWinInfoFromResults(draws: List<DrawResponse>): WinInfo {
        val results = draws.getResults()
        var specialResults = draws.getSpecialResults()
        if (specialResults?.size == draws.getResults().size) {
            if(this.gameType.nonSpecialGame == null) {
                specialResults = null
            }
        }
        val drawSystemId = draws.getDrawSystemId()
        val winningNumbers: MutableList<WinningNumbers> = mutableListOf()
        val hits = this.numbers.filter { results.contains(it) }.size
        var specialHits: Int?

        if (this.specialNumbers != null && specialResults != null) {
            specialHits = this.specialNumbers.filter { specialResults.contains(it) }.size
            if(hits + specialHits < 3) {
                return noHitsWinInfo
            }
            return WinInfo(
                winningNumbers = listOf(
                    WinningNumbers(
                        numbers = this.numbers.sorted(),
                        specialNumbers = this.specialNumbers.sorted(),
                        hits = hits,
                        specialHits = specialHits,
                        prize = getPrizeInfo(drawSystemId = drawSystemId, hits = hits, specialHits = specialHits),
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
                        prize = getPrizeInfo(drawSystemId = drawSystemId, hits = specialHits, isSpecial = true),
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
                    prize = getPrizeInfo(drawSystemId, hits),
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

    private suspend fun TicketNumbers.checkTicketNumbers(draws: List<DrawResponse>): WinInfo {
        if (this.specialNumbers == null && (this.gameType.specialRange != null || this.gameType.specialAmount != null)) {
            return specialsNeededInfo
        }
        if (!this.isValidSize()) {
            return invalidSizeInfo
        }
        if (!this.isValidRange()) {
            return invalidRangeInfo
        }

        return this.getWinInfoFromResults(draws)
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