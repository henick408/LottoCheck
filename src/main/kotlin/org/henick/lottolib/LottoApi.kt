package org.henick.lottolib

class LottoApi private constructor(
    val service: LottoService
) {

    //private var service: LottoService? = ApiInstance { apiKey }.createService()

    companion object {
        suspend fun init(apiKey: String): LottoApi {
            val lottoService: LottoService = ApiInstance { apiKey }.createService()
            val code = lottoService.getLastDrawsInfo().code()
            if (code == 401) {
                throw LottoInvalidTokenException("Entered token is an invalid LottoApi token")
            }
            return LottoApi(lottoService)
        }
    }

    suspend fun getLastDraws(): List<DrawResponse> {
        return service.getLastDrawsInfo().body()!!
    }

    suspend fun getLastDrawsPerGame(gameType: Game): List<DrawResponse> {
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

        val errorWinInfo = numbers.validateTicketData(Game.MINILOTTO)
        if (errorWinInfo != null) {
            return errorWinInfo
        }

        val draws = getLastDrawsPerGame(Game.MINILOTTO)

        val results = draws.first().results.first().resultsJson

        val numbersHit = numbers.filter { results.contains(it) }.size

        if (numbersHit < 3) {
            return null
        }

        return WinInfo(

            winningNumbers = listOf(WinningNumbers(
                numbers = numbers.sorted(),
                numbersHitAmount = numbersHit.toString(),
                gameType = "MiniLotto",
            ))
        )
    }

    suspend fun checkLastLotto(numbers: Set<Int>, isPlus: Boolean = false): WinInfo? {

        val errorWinInfo = numbers.validateTicketData(Game.LOTTO)
        if (errorWinInfo != null) {
            return errorWinInfo
        }

        // zmień żeby impla używało lol
        val draws = getLastDrawsPerGame(Game.LOTTO)
        val winningNumbers: MutableList<WinningNumbers> = mutableListOf()

        if(isPlus) {
            val resultsPlus = draws[1].results.first().resultsJson
            val numbersHitPlus = numbers.filter { resultsPlus.contains(it) }.size
            if (numbersHitPlus >= 3) {
                winningNumbers.add(WinningNumbers(
                    numbers = numbers.sorted(),
                    numbersHitAmount = numbersHitPlus.toString(),
                    gameType = "LottoPlus"
                ))
            }
        }

        val results = draws.first().results.first().resultsJson
        val numbersHit = numbers.filter { results.contains(it) }.size

        if (numbersHit >= 3) {
            winningNumbers.add(WinningNumbers(
                numbers = numbers.sorted(),
                numbersHitAmount = numbersHit.toString(),
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

        val errorWinInfo = validateJackpotTicketData(numbersFirst, numbersSecond)
        if (errorWinInfo != null) {
            return errorWinInfo
        }

        val draws = getLastDrawsPerGame(Game.EUROJACKPOT)

        val resultsFirst = draws.first().results.first().resultsJson
        val resultsSecond = draws.first().results.first().specialResults

        val numbersHitFirst = numbersFirst.filter { resultsFirst.contains(it) }.size
        val numbersHitSecond = numbersSecond.filter { resultsSecond.contains(it) }.size

        val winInfo = WinInfo(
            winningNumbers = listOf(WinningNumbers(
                numbers = numbersFirst.sorted(),
                specialNumbers = numbersSecond.sorted(),
                numbersHitAmount = "$numbersHitFirst+$numbersHitSecond",
                gameType = "EuroJackpot"
            ))
        )

        if (!(numbersHitFirst >= 3 || (numbersHitFirst == 2 && numbersHitSecond >= 1) || (numbersHitFirst == 1 && numbersHitSecond == 2))) {
            return null
        }

        return winInfo

    }

    suspend fun checkMultipleTickets(gameType: Game, vararg numbers: Set<Int>): CheckResponse? {

        if (gameType !in Game.entries) {
            return null
        }

        val winInfoList: MutableSet<WinInfo?> = mutableSetOf()

        when(gameType) {
            Game.LOTTO -> {
                numbers.forEach { winInfoList.add(checkLastLotto(it)) }
            }
            Game.LOTTOPLUS -> {
                numbers.forEach { winInfoList.add(checkLastLotto(it, true)) }
            }
            Game.MINILOTTO -> {
                numbers.forEach { winInfoList.add(checkLastMiniLotto(it)) }
            }
            Game.EUROJACKPOT -> {
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

    private fun Set<Int>.validateTicketData(gameType: Game): WinInfo? {
        val game: Game = gameType
        if (this.size != game.amount) {
            return WinInfo(
                info = "Niepoprawna ilosc liczb"
            )
        }

        if (!this.none { !game.range.contains(it) }) {
            return WinInfo(
                info = "Niepoprawny zakres liczb"
            )
        }

        return null
    }

    private fun validateJackpotTicketData(firstSet: Set<Int>, secondSet: Set<Int>): WinInfo? {
        val game = Game.EUROJACKPOT
        if (firstSet.size != game.amount || secondSet.size != game.specialAmount) {
            return WinInfo(
                info = "Niepoprawna ilosc liczb"
            )
        }

        if (!firstSet.none { !game.range.contains(it) } || !secondSet.none { !game.specialRange!!.contains(it) }) {
            return WinInfo(
                info = "Niepoprawny zakres liczb"
            )
        }

        return null
    }

}

enum class Game(
    val gameName: String,
    val range: IntRange,
    val amount: Int,
    val specialRange: IntRange? = null,
    val specialAmount: Int? = null
) {
    LOTTO( gameName = "Lotto", range = 1..49, amount = 6),
    LOTTOPLUS( gameName = "LottoPlus", range = 1..49, amount = 6),
    MINILOTTO( gameName = "MiniLotto", range = 1..42, amount = 5),
    EUROJACKPOT( gameName = "EuroJackpot", range = 1..50, amount = 5, specialRange = 1..12, specialAmount = 2)
}

data class CheckResponse(
    val winInfoJson: List<WinInfo> = listOf()
)

data class WinInfo(
    val winningNumbers: List<WinningNumbers> = listOf(),
    val info: String = ""
)

data class WinningNumbers(
    val numbers: List<Int>,
    val specialNumbers: List<Int> = listOf(),
    val numbersHitAmount: String = "",
    val gameType: String = ""
)