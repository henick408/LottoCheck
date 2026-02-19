package io.github.henick408.lottocheck.model

import io.github.henick408.lottocheck.domain.GameType

data class WinningNumbers(
    val numbers: List<Int>,
    val hits: Int,
    val prize: Double,
    val gameType: GameType,
    val specialNumbers: List<Int>? = null,
    val specialHits: Int? = null
)