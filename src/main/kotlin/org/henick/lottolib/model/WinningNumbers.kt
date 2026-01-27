package org.henick.lottolib.model

import org.henick.lottolib.domain.GameType

data class WinningNumbers(
    val numbers: List<Int>,
    val specialNumbers: List<Int>? = null,
    val hits: Int,
    val prize: Double,
    val specialHits: Int? = null,
    val gameType: GameType
)