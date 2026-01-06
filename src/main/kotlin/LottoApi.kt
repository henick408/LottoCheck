package org.henick

object LottoApi {

    private val service = ApiInstance.createService(LottoService::class.java)

    private val rangeMap: Map<String, IntRange> = mapOf(
        "miniLotto" to 1..42,
        "lotto" to 1..49,
        "euroJackpotFirst" to 1..50,
        "euroJackpotSecond" to 1..12,
        "ekstraPensjaFirst" to 1..35,
        "ekstraPensjaSecond" to 1..4
    )

    private val numberAmountMap: Map<String, Int> = mapOf(
        "miniLotto" to 5,
        "lotto" to 6,
        "euroJackpotFirst" to 5,
        "euroJackpotSecond" to 2,
        "ekstraPensjaFirst" to 5,
        "ekstraPensjaSecond" to 1
    )

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
        if (numbers.size != numberAmountMap["miniLotto"]) {
            return WinInfo(
                info = "Niepoprawna ilość liczb"
            )
        }

        if (!numbers.none { rangeMap["miniLotto"]?.contains(it) != true  }) {
            return WinInfo(
                info = "Niepoprawny zakres liczb"
            )
        }

        val draws = service.getLastDrawsInfoPerGame("MiniLotto")

        val results = draws.first().results.first().resultsJson

        val numbersHit = numbers.filter { results.contains(it) }.size

        if (numbersHit < 3) {
            return null
        }

        return WinInfo(
            winningNumbers = numbers,
            numbersHitAmount = numbersHit,
            isWon = true
        )
    }



}

data class CheckResponse(
    val winInfoJson: List<WinInfo> = listOf()
)

data class WinInfo(
    val winningNumbers: List<Int> = listOf(),
    val numbersHitAmount: Int = 0,
    val info: String = "",
    val isWon: Boolean = false
)