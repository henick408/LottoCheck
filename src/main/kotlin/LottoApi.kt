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
                winningNumbers.add(WinningNumbers(numbers = numbers.sorted(), numbersHitAmount = numbersHitPlus.toString(), gameType = "LottoPlus"))
            }
        }

        val results = draws.first().results.first().resultsJson
        val numbersHit = numbers.filter { results.contains(it) }.size

        if (numbersHit >= 3) {
            winningNumbers.add(WinningNumbers(numbers = numbers.sorted(), numbersHitAmount = numbersHit.toString(), gameType = "Lotto"))
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

        if (gameType !in games) {
            return null
        }

        val winInfoList: MutableSet<WinInfo?> = mutableSetOf()

        when(gameType) {
            "Lotto" -> {
                numbers.forEach { winInfoList.add(checkLastLotto(it)) }
            }
            "LottoPlus" -> {
                numbers.forEach { winInfoList.add(checkLastLotto(it, true)) }
            }
            "MiniLotto" -> {
                numbers.forEach { winInfoList.add(checkLastMiniLotto(it)) }
            }
            "EuroJackpot" -> {
                if (numbers.size % 2 != 0) {
                    return null
                }
                val setFirst: MutableList<Set<Int>> = mutableListOf()
                val setSecond: MutableList<Set<Int>> = mutableListOf()

                for (i in 0..<numbers.size) {
                    if (i % 2 == 0) {
                        setFirst.add(numbers[i])
                    } else {
                        setSecond.add(numbers[i])
                    }
                }

                for(i in 0..<setFirst.size) {
                    winInfoList.add(checkLastEuroJackpot(setFirst[i], setSecond[i]))
                }
            }
        }

        return CheckResponse(
            winInfoJson = winInfoList.filterNotNull()
        )

    }

    private fun Set<Int>.validateTicketData(gameType: String): WinInfo? {
        if (this.size != numberAmountMap[gameType]) {
            return WinInfo(
                info = "Niepoprawna ilosc liczb"
            )
        }

        if (!this.none { rangeMap[gameType]?.contains(it) != true }) {
            return WinInfo(
                info = "Niepoprawny zakres liczb"
            )
        }

        return null
    }

    private fun validateJackpotData(firstSet: Set<Int>, secondSet: Set<Int>): WinInfo? {
        if (firstSet.size != 5 || secondSet.size != 2) {
            return WinInfo(
                info = "Niepoprawna ilosc liczb"
            )
        }

        if (!firstSet.none { rangeMap["EuroJackpotFirst"]?.contains(it) != true } || !secondSet.none { rangeMap["EuroJackpotSecond"]?.contains(it) != true }) {
            return WinInfo(
                info = "Niepoprawny zakres liczb"
            )
        }

        return null
    }

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

private val games: Set<String> = setOf(
    "Lotto", "LottoPlus", "MiniLotto", "EuroJackpot"
)

private val rangeMap: Map<String, IntRange> = mapOf(
    "MiniLotto" to 1..42,
    "Lotto" to 1..49,
    "EuroJackpotFirst" to 1..50,
    "EuroJackpotSecond" to 1..12,
    "EkstraPensjaFirst" to 1..35,
    "EkstraPensjaSecond" to 1..4
)

private val numberAmountMap: Map<String, Int> = mapOf(
    "MiniLotto" to 5,
    "Lotto" to 6,
    "EuroJackpotFirst" to 5,
    "EuroJackpotSecond" to 2,
    "EkstraPensjaFirst" to 5,
    "EkstraPensjaSecond" to 1
)