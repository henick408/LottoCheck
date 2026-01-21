package org.henick.lottolib.model

data class WinningNumbers(
    val numbers: List<Int>,
    val specialNumbers: List<Int> = listOf(),
    val hits: String,
    val gameType: String
)