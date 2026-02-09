package io.github.henick408.lottocheck.model

import io.github.henick408.lottocheck.domain.GameType

data class WinningNumbers(
    val numbers: List<Int>,
    val specialNumbers: List<Int>? = null,
    val hits: Int,
    val specialHits: Int? = null,
    val prize: Double,
    val gameType: GameType
)