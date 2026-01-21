package org.henick.lottolib.model

data class WinInfo(
    val winningNumbers: List<WinningNumbers> = listOf(),
    val info: String = ""
)