package org.henick.lottolib.api

import org.henick.lottolib.domain.GameType
import org.henick.lottolib.domain.Ticket
import org.henick.lottolib.domain.TicketNumbers
import org.henick.lottolib.model.CheckResponse
import org.henick.lottolib.model.WinInfo
import org.henick.lottolib.model.WinningNumbers
import org.henick.lottolib.network.ApiInstance
import org.henick.lottolib.network.LottoService
import org.henick.lottolib.network.dto.DrawResponse
import java.time.LocalDate

class LottoApi private constructor(
    val service: LottoService
) {

    companion object {
        suspend fun init(apiKey: String): LottoApi {
            val lottoService: LottoService = ApiInstance { apiKey }.createService()
            val code = lottoService.getLastDrawsInfo().code()
            if (code == 401) {
                throw LottoInvalidApiTokenException("Entered token is an invalid LottoApi token")
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
                        hits = "$hits+$specialHits",
                        gameType = this.gameType.gameName
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
                        hits = specialHits.toString(),
                        gameType = this.gameType.gameName
                    )
                )
            }
        }

        if (hits >= 3) {
            val gameType = this.gameType.nonSpecialGame ?: this.gameType
            winningNumbers.add(
                WinningNumbers(
                    numbers = this.numbers.sorted(),
                    hits = hits.toString(),
                    gameType = gameType.gameName
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


    @Deprecated("Nalezy zywac metod klasy TicketNumbers")
    suspend fun checkLastMiniLotto(numbers: Set<Int>): WinInfo? {

        if (!isValidTicketSize(numbers, GameType.MINILOTTO)) {
            return invalidSizeInfo
        }

        if (!isValidTicketRange(numbers, GameType.MINILOTTO)) {
            return invalidRangeInfo
        }

        val draws = getLastDrawsPerGame(GameType.MINILOTTO)

        val results = draws.first().results.first().resultsJson

        val numbersHit = numbers.filter { results.contains(it) }.size

        if (numbersHit < 3) {
            return null
        }

        return WinInfo(

            winningNumbers = listOf(
                WinningNumbers(
                    numbers = numbers.sorted(),
                    hits = numbersHit.toString(),
                    gameType = "MiniLotto",
                ))
        )
    }

    @Deprecated("Nalezy zywac metod klasy TicketNumbers")
    suspend fun checkLastLotto(numbers: Set<Int>, isPlus: Boolean = false): WinInfo? {

        if (!isValidTicketSize(numbers, GameType.LOTTO)) {
            return invalidSizeInfo
        }

        if (!isValidTicketRange(numbers, GameType.LOTTO)) {
            return invalidRangeInfo
        }

        val draws = getLastDrawsPerGame(GameType.LOTTO)
        val winningNumbers: MutableList<WinningNumbers> = mutableListOf()

        if(isPlus) {
            val resultsPlus = draws[1].results.first().resultsJson
            val numbersHitPlus = numbers.filter { resultsPlus.contains(it) }.size
            if (numbersHitPlus >= 3) {
                winningNumbers.add(
                    WinningNumbers(
                        numbers = numbers.sorted(),
                        hits = numbersHitPlus.toString(),
                        gameType = "LottoPlus"
                    ))
            }
        }

        val results = draws.first().results.first().resultsJson
        val numbersHit = numbers.filter { results.contains(it) }.size

        if (numbersHit >= 3) {
            winningNumbers.add(
                WinningNumbers(
                    numbers = numbers.sorted(),
                    hits = numbersHit.toString(),
                    gameType = "Lotto"
                ))
        }

        if (winningNumbers.isEmpty()) {
            return null
        }

        return WinInfo(
            winningNumbers = winningNumbers
        )

    }

    @Deprecated("Nalezy zywac metod klasy TicketNumbers")
    suspend fun checkLastEuroJackpot(numbersFirst: Set<Int>, numbersSecond: Set<Int>): WinInfo? {

        if (!isValidJackpotTicketSize(numbersFirst, numbersSecond)) {
            return invalidSizeInfo
        }
        if (!isValidJackpotTicketRange(numbersFirst, numbersSecond)) {
            return invalidRangeInfo
        }

        val draws = getLastDrawsPerGame(GameType.EUROJACKPOT)

        val resultsFirst = draws.first().results.first().resultsJson
        val resultsSecond = draws.first().results.first().specialResults

        val numbersHitFirst = numbersFirst.filter { resultsFirst.contains(it) }.size
        val numbersHitSecond = numbersSecond.filter { resultsSecond.contains(it) }.size

        if (!isEuroJackpotWinCondition(numbersHitFirst, numbersHitSecond)) {
            return null
        }

        val winInfo = WinInfo(
            winningNumbers = listOf(
                WinningNumbers(
                    numbers = numbersFirst.sorted(),
                    specialNumbers = numbersSecond.sorted(),
                    hits = "$numbersHitFirst+$numbersHitSecond",
                    gameType = "EuroJackpot"
                )
            )
        )

        return winInfo

    }

    @Deprecated("Nalezy zywac metod klasy TicketNumbers")
    suspend fun checkMultipleTickets(gameType: GameType, vararg numbers: Set<Int>): CheckResponse? {

        if (gameType !in GameType.entries) {
            return null
        }

        val winInfoList: MutableList<WinInfo?> = mutableListOf()

        when(gameType) {
            GameType.LOTTO -> {
                numbers.forEach { winInfoList.add(checkLastLotto(it)) }
            }
            GameType.LOTTOPLUS -> {
                numbers.forEach { winInfoList.add(checkLastLotto(it, true)) }
            }
            GameType.MINILOTTO -> {
                numbers.forEach { winInfoList.add(checkLastMiniLotto(it)) }
            }
            GameType.EUROJACKPOT -> {
                if (numbers.size % 2 != 0) {
                    return null
                }

                for (i in 0..<numbers.size step 2) {
                    winInfoList.add(checkLastEuroJackpot(numbers[i], numbers[i+1]))
                }

            }
        }

        return CheckResponse(
            drawSystemId = 0,
            results = listOf(),
            specialResults = null,
            winInfoJson = winInfoList.filterNotNull()
        )

    }

    @Deprecated("Nalezy zywac metod klasy TicketNumbers")
    private fun isValidTicketSize(set: Set<Int>, gameType: GameType): Boolean {
        return set.size == gameType.amount
    }

    @Deprecated("Nalezy zywac metod klasy TicketNumbers")
    private fun isValidTicketRange(set: Set<Int>, gameType: GameType): Boolean {
        return set.all { it in gameType.range }
    }

    @Deprecated("Nalezy zywac metod klasy TicketNumbers")
    private fun isValidJackpotTicketSize(firstSet: Set<Int>, secondSet: Set<Int>): Boolean {
        return firstSet.size == GameType.EUROJACKPOT.amount && secondSet.size == GameType.EUROJACKPOT.specialAmount
    }

    @Deprecated("Nalezy zywac metod klasy TicketNumbers")
    private fun isValidJackpotTicketRange(firstSet: Set<Int>, secondSet: Set<Int>): Boolean {
        return firstSet.all { GameType.EUROJACKPOT.range.contains(it) } && secondSet.all { GameType.EUROJACKPOT.specialRange!!.contains(it) }
    }

    @Deprecated("Nalezy zywac metod klasy TicketNumbers")
    private fun isEuroJackpotWinCondition(numbersFirst: Int, numbersSecond: Int): Boolean {
        return (numbersFirst >= 3 || (numbersFirst == 2 && numbersSecond >= 1) || (numbersFirst == 1 && numbersSecond == 2))
    }

}