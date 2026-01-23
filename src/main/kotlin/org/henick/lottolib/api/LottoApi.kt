package org.henick.lottolib.api

import org.henick.lottolib.domain.GameType
import org.henick.lottolib.model.CheckResponse
import org.henick.lottolib.model.WinInfo
import org.henick.lottolib.model.WinningNumbers
import org.henick.lottolib.network.ApiInstance
import org.henick.lottolib.network.LottoService
import org.henick.lottolib.network.dto.DrawResponse
import org.henick.lottolib.network.dto.DrawResponseByDatePerGame

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
    }

    suspend fun getLastDraws(): List<DrawResponse> {
        return service.getLastDrawsInfo().body()!!
    }

    suspend fun getLastDrawsPerGame(gameType: GameType): List<DrawResponse> {
        return service.getLastDrawsInfoPerGame(gameType.gameName).body()!!
    }

    suspend fun getDrawsByDate(drawDate: String): List<DrawResponse> {
        return service.getDrawsInfoByDate(drawDate).body()!!
    }

    suspend fun getDrawsByDatePerGame(gameType: String, drawDate: String): DrawResponseByDatePerGame {
        return service.getDrawsInfoByDatePerGame(
            gameType = gameType,
            drawDate = drawDate
        ).body()!!
    }

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

        if (isEuroJackpotWinCondition(numbersHitFirst, numbersHitSecond)) {
            return null
        }

        return winInfo

    }

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
            winInfoJson = winInfoList.filterNotNull()
        )

    }

    private fun isValidTicketSize(set: Set<Int>, gameType: GameType): Boolean {
        return set.size == gameType.amount
    }

    private fun isValidTicketRange(set: Set<Int>, gameType: GameType): Boolean {
        return set.all { it in gameType.range }
    }

    private fun isValidJackpotTicketSize(firstSet: Set<Int>, secondSet: Set<Int>): Boolean {
        return firstSet.size == GameType.EUROJACKPOT.amount && secondSet.size == GameType.EUROJACKPOT.specialAmount
    }

    private fun isValidJackpotTicketRange(firstSet: Set<Int>, secondSet: Set<Int>): Boolean {
        return firstSet.all { GameType.EUROJACKPOT.range.contains(it) } && secondSet.all { GameType.EUROJACKPOT.specialRange!!.contains(it) }
    }


    private fun isEuroJackpotWinCondition(numbersFirst: Int, numbersSecond: Int): Boolean {
        return !(numbersFirst >= 3 || (numbersFirst == 2 && numbersSecond >= 1) || (numbersFirst == 1 && numbersSecond == 2))
    }

}