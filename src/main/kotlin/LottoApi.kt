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

    suspend fun checkLastMiniLotto(numbers: List<Int>): WinInfo? {

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
                numbers = numbers,
                numbersHitAmount = numbersHit,
                gameType = "MiniLotto",
            ))
        )
    }

    suspend fun checkLastLotto(numbers: List<Int>): WinInfo? {

        val errorWinInfo = numbers.validateTicketData("Lotto")
        if (errorWinInfo != null) {
            return errorWinInfo
        }

        val draws = service.getLastDrawsInfoPerGame("Lotto")

        val results = draws.first().results.first().resultsJson
        val resultsPlus = draws[1].results.first().resultsJson

        val numbersHit = numbers.filter { results.contains(it) }.size
        val numbersHitPlus = numbers.filter { resultsPlus.contains(it) }.size

        val winningNumbers: MutableList<WinningNumbers> = mutableListOf()

        if (numbersHit >= 2) {
            winningNumbers.add(WinningNumbers(numbers = numbers, numbersHitAmount = numbersHit, gameType = "Lotto"))
        }
        if (numbersHitPlus >= 2) {
            winningNumbers.add(WinningNumbers(numbers = numbers, numbersHitAmount = numbersHitPlus, gameType = "LottoPlus"))
        }
        if (winningNumbers.isEmpty()) {
            return null
        }

        return WinInfo(
            winningNumbers = winningNumbers
        )

    }


    private fun List<Int>.validateTicketData(gameType: String): WinInfo? {
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

    private fun List<DrawResponse>.getResults(): List<Int> =
        this.first().results.first().resultsJson


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
    val numbersHitAmount: Int = 0,
    val gameType: String = ""
)

private val games: Set<String> = setOf(
    "Lotto", "LottoPlus", "MiniLotto", "EuroJackpot", "EkstraPensja"
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