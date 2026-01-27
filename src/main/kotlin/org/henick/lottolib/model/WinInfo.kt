package org.henick.lottolib.model

data class WinInfo(
    val winningNumbers: List<WinningNumbers>? = null,
    val info: String? = null
) {
    fun getPrizes(): List<Double> {
        if (winningNumbers == null) {
            return listOf()
        }
        return this.winningNumbers.map { it.prize }
    }
}