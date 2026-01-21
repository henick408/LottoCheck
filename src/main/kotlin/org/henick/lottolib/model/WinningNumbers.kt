package org.henick.lottolib.model

data class WinningNumbers(
    val numbers: List<Int>,
    val specialNumbers: List<Int> = listOf(),
    val numbersHitAmount: String = "",
    val gameType: String = ""
)