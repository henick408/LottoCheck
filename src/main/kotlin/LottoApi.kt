package org.henick

object LottoApi {

    private val service = ApiInstance.createService(LottoService::class.java)


    suspend fun getLastDraws(): List<DrawResponse>? {
        try {

            return service.getLastDrawsInfo()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    suspend fun getLastDrawsPerGame(gameType: String): List<DrawResponse>? {
        try {
            return service.getLastDrawsInfoPerGame(gameType)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    suspend fun getDrawsByDate(drawDate: String): List<DrawResponse>? {
        try {
            return service.getDrawsInfoByDate(drawDate)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    suspend fun getDrawsByDatePerGame(gameType: String, drawDate: String): DrawResponseByDatePerGame? {
        try {
            return service.getDrawsInfoByDatePerGame(
                gameType = gameType,
                drawDate = drawDate
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    suspend fun checkLastMiniLotto(numbers: Set<Int>): WinInfo? {

        val errorWinInfo = numbers.validateTicketData("MiniLotto")
        if (errorWinInfo != null) {
            return errorWinInfo
        }

        val draws = service.getLastDrawsInfoPerGame("MiniLotto")

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

        val errorWinInfo = numbers.validateTicketData("Lotto")
        if (errorWinInfo != null) {
            return errorWinInfo
        }

        val draws = service.getLastDrawsInfoPerGame("Lotto")
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

        val errorWinInfo = validateJackpotData(numbersFirst, numbersSecond)
        if (errorWinInfo != null) {
            return errorWinInfo
        }

        val draws = service.getLastDrawsInfoPerGame("EuroJackpot")

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

    suspend fun checkMultipleTickets(gameType: String, vararg numbers: Set<Int>): CheckResponse? {

        val game: String = gameType.lowercase()

        if (game !in GAME.entries.map { it.name.lowercase() }) {
            return null
        }

        val winInfoList: MutableSet<WinInfo?> = mutableSetOf()

        when(game) {
            "lotto" -> {
                numbers.forEach { winInfoList.add(checkLastLotto(it)) }
            }
            "lottoplus" -> {
                numbers.forEach { winInfoList.add(checkLastLotto(it, true)) }
            }
            "minilotto" -> {
                numbers.forEach { winInfoList.add(checkLastMiniLotto(it)) }
            }
            "eurojackpot" -> {
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

    private fun Set<Int>.validateTicketData(gameType: String): WinInfo? {
        val game: GAME = GAME.valueOf(gameType)
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

    private fun validateJackpotData(firstSet: Set<Int>, secondSet: Set<Int>): WinInfo? {
        val game = GAME.valueOf("EUROJACKPOT")
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

private enum class GAME(
    val range: IntRange,
    val amount: Int,
    val specialRange: IntRange? = null,
    val specialAmount: Int? = null
) {
    LOTTO( range = 1..49, amount = 6),
    MINILOTTO(range = 1..42, amount = 5),
    EUROJACKPOT(range = 1..50, amount = 5, specialRange = 1..12, specialAmount = 2)
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