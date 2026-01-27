package org.henick.lottolib.model

data class WinInfo(
    val winningNumbers: List<WinningNumbers>? = null,
    val prize: Double? = null,
    val info: String? = null
)